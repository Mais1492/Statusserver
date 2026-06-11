package at.hochschule_burgenland.statusserver.service;

import at.hochschule_burgenland.statusserver.model.UserStatus;
import at.hochschule_burgenland.statusserver.repository.UserStatusRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UserStatusService {

    private final UserStatusRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    public UserStatusService(
        UserStatusRepository repository,
        RabbitTemplate rabbitTemplate,
        SimpMessagingTemplate messagingTemplate) {

        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    public UserStatus create(UserStatus status) {
        status.setId(UUID.randomUUID().toString());
        status.setTimestamp(Instant.now());
        status.setUpdatedAt(Instant.now());
        status.setDeleted(false);

        UserStatus saved = repository.save(status);

        broadcast(saved);
        replicate(saved);

        return saved;
    }

    public UserStatus update(String id, UserStatus changes) {
        UserStatus existing = repository.findById(id).orElse(null);

        if (existing == null || existing.isDeleted()) {
            return  null;
        }

        existing.setUsername(changes.getUsername());
        existing.setStatusText(changes.getStatusText());
        existing.setLatitude(changes.getLatitude());
        existing.setLongitude(changes.getLongitude());
        existing.setUpdatedAt(Instant.now());

        UserStatus saved = repository.save(existing);

        broadcast(saved);
        replicate(saved);

        return saved;
    }


    public boolean delete(String id) {
        UserStatus existing = repository.findById(id).orElse(null);

        if (existing == null || existing.isDeleted()) {
            return  false;
        }

        existing.setDeleted(true);
        existing.setUpdatedAt(Instant.now());

        UserStatus saved = repository.save(existing);

        broadcast(saved);
        replicate(saved);

        return true;
    }



    @Transactional
    public void applyFromRemote(UserStatus incoming) {
        UserStatus existing = repository.findById(incoming.getId()).orElse(null);

        if (existing == null) {
            UserStatus saved = repository.save(incoming);
            broadcast(saved);
            return;
        }

        if (incoming.getUpdatedAt().isAfter(existing.getUpdatedAt())) {
            existing.setUsername(incoming.getUsername());
            existing.setStatusText(incoming.getStatusText());
            existing.setTimestamp(incoming.getTimestamp());
            existing.setLatitude(incoming.getLatitude());
            existing.setLongitude(incoming.getLongitude());
            existing.setUpdatedAt(incoming.getUpdatedAt());
            existing.setDeleted(incoming.isDeleted());

            UserStatus saved = repository.save(existing);
            broadcast(saved);
        }
    }

    public List<UserStatus> findAll() {
        return repository.findByDeletedFalse();
    }

    public List<UserStatus> findAllIncludingDeleted() {
        return repository.findAll();
    }

    public void synchronizeFromCluster() {
        try {
            UserStatus[] statuses = restTemplate.getForObject(
                    "http://loadbalancer:8080/messages/sync", UserStatus[].class);

            if (statuses == null) {
                return;
            }

            for (UserStatus status : statuses) {
                applyFromRemote(status);
            }
        } catch (Exception e) {
            System.out.println("User sync failed: " + e.getMessage());
        }
    }

    private void broadcast(UserStatus status) {
        messagingTemplate.convertAndSend("/topic/userstatus", status);
    }

    private void replicate(UserStatus status) {
        try {
            rabbitTemplate.convertAndSend("userStatusExchange", "", status);
        } catch (Exception e) {
            System.out.println("Replicate via RabbitMQ failed (will heal via sync): " +
                    e.getMessage());
        }
    }
}
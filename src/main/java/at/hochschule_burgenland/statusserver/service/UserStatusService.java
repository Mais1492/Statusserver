package at.hochschule_burgenland.statusserver.service;

import at.hochschule_burgenland.statusserver.model.UserStatus;
import at.hochschule_burgenland.statusserver.repository.UserStatusRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private final RestTemplate restTemplate = new RestTemplate();

    public UserStatusService(
        UserStatusRepository repository,
        RabbitTemplate rabbitTemplate) {

        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public UserStatus create(UserStatus status) {
        status.setId(UUID.randomUUID().toString());
        status.setTimestamp(Instant.now());
        status.setUpdatedAt(Instant.now());
        status.setDeleted(false);

        UserStatus saved = repository.save(status);

        rabbitTemplate.convertAndSend("userStatusExchange", saved);

        return saved;
    }

    @Transactional
    public void applyFromRemote(UserStatus incoming) {
        UserStatus existing = repository.findById(incoming.getId()).orElse(null);


        if (existing == null) {
            repository.save(incoming);
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

            repository.save(existing);
        }
    }

    public List<UserStatus> findAll() {
        return repository.findAll();
    }

    public void synchronizeFromCluster() {

        try {
            UserStatus[] statuses = restTemplate.getForObject("http://haproxy:8080/userstatus", UserStatus[].class);

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
}
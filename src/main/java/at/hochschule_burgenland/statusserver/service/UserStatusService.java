package at.hochschule_burgenland.statusserver.service;

import at.hochschule_burgenland.statusserver.model.UserStatus;
import at.hochschule_burgenland.statusserver.repository.UserStatusRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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

    public UserStatus save(UserStatus status) {

        UserStatus saved = repository.save(status);

        if (!status.isFromQueue()) {

            rabbitTemplate.convertAndSend(
                "userStatusExchange",
                "",
                saved
            );
        }

        return saved;
    }

    public List<UserStatus> findAll() {
        return repository.findAll();
    }

    public void synchronizeFromCluster() {

        try {

            UserStatus[] statuses =
                restTemplate.getForObject(
                    "http://haproxy:8080/userstatus",
                    UserStatus[].class);

            if (statuses == null) {
                return;
            }

            for (UserStatus status : statuses) {

                status.setFromQueue(true);

                save(status);
            }

        } catch (Exception e) {

            System.out.println(
                "UserStatus sync failed: " + e.getMessage());
        }
    }
}
package at.hochschule_burgenland.statusserver.messaging;

import at.hochschule_burgenland.statusserver.model.UserStatus;
import at.hochschule_burgenland.statusserver.service.UserStatusService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserStatusListener {

    private final UserStatusService service;

    public UserStatusListener(UserStatusService service) {
        this.service = service;
    }

    @RabbitListener(queues = "#{userStatusQueue.name}")
    public void receive(UserStatus status) {
        service.applyFromRemote(status);
    }
}

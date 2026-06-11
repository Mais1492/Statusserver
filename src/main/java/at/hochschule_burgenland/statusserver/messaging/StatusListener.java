package at.hochschule_burgenland.statusserver.messaging;

import at.hochschule_burgenland.statusserver.model.Status;
import at.hochschule_burgenland.statusserver.service.StatusService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class StatusListener {

  private final StatusService statusService;

  public StatusListener(StatusService statusService) {
    this.statusService = statusService;
  }

  @RabbitListener(queues = "#{statusQueue.name}")
  public void receive(Status status) {
    status.setFromQueue(true);
    statusService.updateStatusFromQueue(status);
  }
}
package at.hochschule_burgenland.statusserver.messaging;

import at.hochschule_burgenland.statusserver.model.Status;
import at.hochschule_burgenland.statusserver.service.StatusService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class StatusListener {

  private final StatusService statusService;
  private final String instanceId;

  public StatusListener(StatusService statusService,
                        @Value("${status.instance-id}") String instanceId) {
    this.statusService = statusService;
    this.instanceId = instanceId;
  }

  @RabbitListener(queues = "#{statusQueue.name}")
  public void receive(Status status) {
    status.setFromQueue(true);

    if (("statusserver:" + instanceId).equals(status.getNodeId())) {
      return;
    }

    status.setFromQueue(true);
    statusService.updateStatusFromQueue(status);
  }
}
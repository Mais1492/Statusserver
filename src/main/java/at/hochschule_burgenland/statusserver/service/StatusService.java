package at.hochschule_burgenland.statusserver.service;

import at.hochschule_burgenland.statusserver.model.Status;
import at.hochschule_burgenland.statusserver.model.StatusState;
import at.hochschule_burgenland.statusserver.repository.StatusRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class StatusService {

  private final SimpMessagingTemplate messagingTemplate;
  private final StatusRepository statusRepository;
  private final RabbitTemplate rabbitTemplate;

  public StatusService(SimpMessagingTemplate messagingTemplate,
                       StatusRepository statusRepository,
                       RabbitTemplate rabbitTemplate) {
    this.messagingTemplate = messagingTemplate;
    this.statusRepository = statusRepository;
    this.rabbitTemplate = rabbitTemplate;
  }

  public Status updateStatus(Status status) {

    Status existing = statusRepository.findByNodeId(status.getNodeId())
        .orElse(null);

    Status saved;

    if (existing != null) {
      existing.setState(status.getState());
      existing.setUpdatedAt(Instant.now());
      saved = statusRepository.save(existing);
    } else {
      saved = statusRepository.save(status);
    }

    messagingTemplate.convertAndSend("/topic/status", saved);

    if (!status.isFromQueue()) {
      rabbitTemplate.convertAndSend(
          "statusExchange",
          "",
          saved
      );
    }

    return saved;
  }

  @Transactional
  public void updateStatusFromQueue(Status status) {

    Status existing = statusRepository.findByNodeId(status.getNodeId())
        .orElse(null);

    Status saved;
    if (existing == null) {
      Status newStatus = new Status();
      newStatus.setNodeId(status.getNodeId());
      newStatus.setState(status.getState());
      newStatus.setUpdatedAt(Instant.now());
      saved = safeUpsert(newStatus);
    } else {
      existing.setState(status.getState());
      existing.setUpdatedAt(Instant.now());

      saved = safeUpsert(existing);
    }

    messagingTemplate.convertAndSend("/topic/status", saved);
  }

  public List<Status> getAllStatus() {
    return statusRepository.findAll();
  }

  @Transactional
  public Status safeUpsert(Status status) {
    try {
      return statusRepository.save(status);
    } catch (ConstraintViolationException e) {
      Status existing = statusRepository.findByNodeId(status.getNodeId()).orElseThrow();
      existing.setState(status.getState());
      return statusRepository.save(existing);
    }
  }

  @Transactional
  public void markStaleInstancesOffline(Instant threshold) {

    List<Status> all = statusRepository.findAll();

    for (Status s : all) {

      if (s.getUpdatedAt() != null &&
          s.getUpdatedAt().isBefore(threshold) &&
          s.getState() == StatusState.ONLINE) {

        s.setState(StatusState.OFFLINE);
        statusRepository.save(s);
      }
    }
  }
}

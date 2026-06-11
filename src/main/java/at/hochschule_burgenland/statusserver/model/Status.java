package at.hochschule_burgenland.statusserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class Status {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String nodeId;

  @Enumerated(EnumType.STRING)
  private StatusState state;

  private Instant updatedAt;

  @JsonIgnore
  private boolean fromQueue = false;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public StatusState getState() {
    return state;
  }

  public void setState(StatusState state) {
    this.state = state;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public boolean isFromQueue() {
    return fromQueue;
  }

  public void setFromQueue(boolean fromQueue) {
    this.fromQueue = fromQueue;
  }
}
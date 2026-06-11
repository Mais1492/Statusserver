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
}
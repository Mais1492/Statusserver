package at.hochschule_burgenland.statusserver.repository;

import at.hochschule_burgenland.statusserver.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusRepository extends JpaRepository<Status, Long> {

  Optional<Status> findByNodeId(String nodeId);
}

package at.hochschule_burgenland.statusserver.repository;

import at.hochschule_burgenland.statusserver.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatusRepository
    extends JpaRepository<UserStatus, Long> {
}

package at.hochschule_burgenland.statusserver.repository;

import at.hochschule_burgenland.statusserver.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserStatusRepository
    extends JpaRepository<UserStatus, String> {

    List<UserStatus> findByDeletedFalse();
}

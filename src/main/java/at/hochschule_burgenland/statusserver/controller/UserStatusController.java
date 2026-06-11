package at.hochschule_burgenland.statusserver.controller;

import at.hochschule_burgenland.statusserver.model.UserStatus;
import at.hochschule_burgenland.statusserver.service.UserStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class UserStatusController {

    private final UserStatusService service;

    public UserStatusController(UserStatusService service) {
        this.service = service;
    }

    @PostMapping
    public UserStatus create(
        @RequestBody UserStatus status) {

        return service.create(status);
    }

    @GetMapping
    public List<UserStatus> all() {
        return service.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserStatus> update(@PathVariable String id, @RequestBody UserStatus changes) {
        UserStatus updated = service.update(id, changes);

        if (updated == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean removed = service.delete(id);

        if (!removed) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
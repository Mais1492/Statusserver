package at.hochschule_burgenland.statusserver.controller;

import at.hochschule_burgenland.statusserver.model.UserStatus;
import at.hochschule_burgenland.statusserver.service.UserStatusService;
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
}
package at.hochschule_burgenland.statusserver.controller;

import at.hochschule_burgenland.statusserver.model.Status;
import at.hochschule_burgenland.statusserver.service.StatusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/status")
public class StatusController {

  private final StatusService statusService;

  public StatusController(StatusService statusService) {
    this.statusService = statusService;
  }

  @PostMapping
  public Status updateStatus(@RequestBody Status status) {
    return statusService.updateStatus(status);
  }

  @GetMapping
  public List<Status> getAllStatus() {
    return statusService.getAllStatus();
  }
}
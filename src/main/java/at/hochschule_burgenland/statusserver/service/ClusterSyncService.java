package at.hochschule_burgenland.statusserver.service;

import at.hochschule_burgenland.statusserver.model.Status;
import at.hochschule_burgenland.statusserver.model.StatusState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ClusterSyncService {

    private final StatusService statusService;
    private final String instanceId;

    public ClusterSyncService(StatusService statusService,
                              @Value("${status.instance-id}") String instanceId) {
        this.statusService = statusService;
        this.instanceId = instanceId;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        publishOnline();
    }

    @Scheduled(fixedDelayString = "5000")
    public void heartbeat() {
        publishOnline();
    }

    @Scheduled(fixedDelay = 5000)
    public void detectFailures() {

        Instant threshold = Instant.now().minusSeconds(10);

        statusService.markStaleInstancesOffline(threshold);
    }

    private void publishOnline() {
        Status status = new Status();
        status.setNodeId("statusserver:" + instanceId);
        status.setState(StatusState.ONLINE);
        status.setUpdatedAt(Instant.now());

        System.out.println("HEARTBEAT: " + instanceId + " at " + Instant.now());

        statusService.updateStatus(status);
    }
}
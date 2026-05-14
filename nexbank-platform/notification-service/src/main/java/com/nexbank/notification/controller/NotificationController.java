package com.nexbank.notification.controller;

import com.nexbank.notification.document.NotificationLog;
import com.nexbank.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/v1/notifications/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationLog>> getByUserId(@PathVariable Long userId) {
        log.info("GET /api/v1/notifications/user/{}", userId);
        return ResponseEntity.ok(notificationService.getByUserId(userId));
    }

    // GET /api/v1/notifications/reference?id=xxx&type=TRANSACTION
    @GetMapping("/reference")
    public ResponseEntity<List<NotificationLog>> getByReference(
            @RequestParam String id,
            @RequestParam String type) {
        log.info("GET /api/v1/notifications/reference - id: {} type: {}", id, type);
        return ResponseEntity.ok(notificationService.getByReference(id, type));
    }
}

package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.request.RestoreBackupRequest;
import com.pfa.tracabilite_ia.dto.response.BackupJobResponse;
import com.pfa.tracabilite_ia.dto.response.BackupRestoreResponse;
import com.pfa.tracabilite_ia.dto.response.BackupVerifyResponse;
import com.pfa.tracabilite_ia.service.AuthService;
import com.pfa.tracabilite_ia.service.BackupRestoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/backup")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBackupController {

    private final BackupRestoreService backupRestoreService;
    private final AuthService authService;

    public AdminBackupController(BackupRestoreService backupRestoreService, AuthService authService) {
        this.backupRestoreService = backupRestoreService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<BackupJobResponse>> list() {
        return ResponseEntity.ok(backupRestoreService.list());
    }

    @PostMapping
    public ResponseEntity<BackupJobResponse> create() {
        return ResponseEntity.ok(backupRestoreService.create(authService.getCurrentUser()));
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<BackupVerifyResponse> verify(@PathVariable UUID id) {
        return ResponseEntity.ok(backupRestoreService.verify(id, authService.getCurrentUser()));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {
        byte[] body = backupRestoreService.readFile(id);
        String filename = backupRestoreService.downloadFilename(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<BackupRestoreResponse> restore(
            @PathVariable UUID id,
            @Valid @RequestBody RestoreBackupRequest request
    ) {
        return ResponseEntity.ok(backupRestoreService.restore(
                id,
                Boolean.TRUE.equals(request.getConfirm()),
                authService.getCurrentUser()
        ));
    }
}

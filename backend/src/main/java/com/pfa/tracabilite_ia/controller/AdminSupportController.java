package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.request.UpdateSupportMessageStatusRequest;
import com.pfa.tracabilite_ia.dto.response.SupportMessagePageResponse;
import com.pfa.tracabilite_ia.dto.response.SupportMessageResponse;
import com.pfa.tracabilite_ia.enumeration.SupportMessageStatus;
import com.pfa.tracabilite_ia.service.SupportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/support/messages")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSupportController {

    private final SupportService supportService;

    public AdminSupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @GetMapping
    public ResponseEntity<SupportMessagePageResponse> list(
            @RequestParam(required = false) SupportMessageStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        SupportMessagePageResponse response = supportService.listMessages(
                status,
                q,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupportMessageResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(supportService.getMessage(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SupportMessageResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSupportMessageStatusRequest request
    ) {
        return ResponseEntity.ok(supportService.updateStatus(id, request));
    }
}

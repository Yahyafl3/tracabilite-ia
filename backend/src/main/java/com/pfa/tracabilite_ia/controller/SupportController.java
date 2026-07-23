package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.request.CreateSupportMessageRequest;
import com.pfa.tracabilite_ia.dto.response.MessageResponse;
import com.pfa.tracabilite_ia.service.SupportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> createMessage(
            @Valid @RequestBody CreateSupportMessageRequest request,
            HttpServletRequest httpRequest
    ) {
        MessageResponse response = supportService.createMessage(request, resolveClientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

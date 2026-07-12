package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.request.LoginRequest;
import com.pfa.tracabilite_ia.dto.response.JwtResponse;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.jwt.JwtProvider;
import com.pfa.tracabilite_ia.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    public AuthController(AuthService authService, JwtProvider jwtProvider) {
        this.authService = authService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {
        Utilisateur utilisateur = authService.login(request.getEmail(), request.getMotDePasse());
        String token = jwtProvider.generateToken(utilisateur);
        return new JwtResponse(
                token,
                utilisateur.getId().toString(),
                utilisateur.getNom(),
                utilisateur.getEmail(),
                utilisateur.getRole().name());
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Utilisateur me() {
        return authService.getCurrentUser();
    }
}

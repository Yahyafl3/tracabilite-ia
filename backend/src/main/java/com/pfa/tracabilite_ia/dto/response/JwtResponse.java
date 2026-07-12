package com.pfa.tracabilite_ia.dto.response;

import lombok.Data;

@Data
public class JwtResponse {

    private String token;
    private String type = "Bearer";
    private String id;
    private String nom;
    private String email;
    private String role;

    public JwtResponse(String token, String id, String nom, String email, String role) {
        this.token = token;
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.role = role;
    }
}

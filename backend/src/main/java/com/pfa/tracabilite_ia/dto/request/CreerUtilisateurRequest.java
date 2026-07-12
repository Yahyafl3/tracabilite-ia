package com.pfa.tracabilite_ia.dto.request;

import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreerUtilisateurRequest {

    @NotBlank(message = "Le nom est requis")
    private String nom;

    @NotBlank(message = "L'email est requis")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caracteres")
    private String motDePasse;

    @NotNull(message = "Le role est requis")
    private RoleEnum role;
}

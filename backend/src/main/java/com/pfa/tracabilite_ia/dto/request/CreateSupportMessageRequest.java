package com.pfa.tracabilite_ia.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSupportMessageRequest {

    @NotBlank(message = "Le nom est requis")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    @NotBlank(message = "L'email est requis")
    @Email(message = "Email invalide")
    @Size(max = 255, message = "L'email est trop long")
    private String email;

    @NotBlank(message = "Le sujet est requis")
    @Size(min = 3, max = 120, message = "Le sujet doit contenir entre 3 et 120 caractères")
    private String subject;

    @NotBlank(message = "Le message est requis")
    @Size(min = 10, max = 2000, message = "Le message doit contenir entre 10 et 2000 caractères")
    private String message;
}

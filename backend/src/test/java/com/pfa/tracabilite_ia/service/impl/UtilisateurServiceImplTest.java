package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.request.CreerUtilisateurRequest;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.exception.UnauthorizedActionException;
import com.pfa.tracabilite_ia.mapper.UtilisateurMapper;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import com.pfa.tracabilite_ia.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthService authService;

    private UtilisateurServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UtilisateurServiceImpl(
                utilisateurRepository,
                passwordEncoder,
                new UtilisateurMapper(),
                authService
        );
    }

    @Test
    void creer_rejectsUtilisateurRole() {
        CreerUtilisateurRequest request = new CreerUtilisateurRequest();
        request.setNom("Operateur");
        request.setEmail("op@tracabilite.ia");
        request.setMotDePasse("secret123");
        request.setRole(RoleEnum.UTILISATEUR);

        assertThatThrownBy(() -> service.creer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role non autorise");
    }

    @Test
    void creer_rejectsMissingRole() {
        CreerUtilisateurRequest request = new CreerUtilisateurRequest();
        request.setNom("Validateur");
        request.setEmail("val@tracabilite.ia");
        request.setMotDePasse("secret123");
        request.setRole(null);

        assertThatThrownBy(() -> service.creer(request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.request.CreerUtilisateurRequest;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.mapper.UtilisateurMapper;
import com.pfa.tracabilite_ia.repository.AppelIARepository;
import com.pfa.tracabilite_ia.repository.PasswordResetTokenRepository;
import com.pfa.tracabilite_ia.repository.SupportMessageRepository;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import com.pfa.tracabilite_ia.repository.ValidationActionRepository;
import com.pfa.tracabilite_ia.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthService authService;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private SupportMessageRepository supportMessageRepository;
    @Mock
    private AppelIARepository appelIARepository;
    @Mock
    private ValidationActionRepository validationActionRepository;

    private UtilisateurServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UtilisateurServiceImpl(
                utilisateurRepository,
                passwordEncoder,
                new UtilisateurMapper(),
                authService,
                passwordResetTokenRepository,
                supportMessageRepository,
                appelIARepository,
                validationActionRepository
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

    @Test
    void supprimer_allowsOperatorAccount() {
        UUID adminId = UUID.randomUUID();
        UUID operatorId = UUID.randomUUID();

        Utilisateur admin = new Utilisateur();
        admin.setId(adminId);
        admin.setRole(RoleEnum.ADMINISTRATEUR);

        Utilisateur operator = new Utilisateur();
        operator.setId(operatorId);
        operator.setEmail("user@tracabilite.ia");
        operator.setRole(RoleEnum.UTILISATEUR);

        when(utilisateurRepository.findById(operatorId)).thenReturn(Optional.of(operator));
        when(authService.getCurrentUser()).thenReturn(admin);
        when(validationActionRepository.existsByValidateur(operator)).thenReturn(false);

        service.supprimer(operatorId);

        verify(passwordResetTokenRepository).deleteByUtilisateur(operator);
        verify(supportMessageRepository).clearProcessedBy(operator);
        verify(appelIARepository).clearUtilisateur(operator);
        verify(utilisateurRepository).delete(operator);
    }
}

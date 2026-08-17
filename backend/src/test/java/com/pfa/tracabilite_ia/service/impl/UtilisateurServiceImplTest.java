package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.request.CreerUtilisateurRequest;
import com.pfa.tracabilite_ia.dto.response.UtilisateurResponse;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.exception.UnauthorizedActionException;
import com.pfa.tracabilite_ia.mapper.UtilisateurMapper;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import com.pfa.tracabilite_ia.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    void creer_acceptsUtilisateurRole() {
        CreerUtilisateurRequest request = new CreerUtilisateurRequest();
        request.setNom("Operateur");
        request.setEmail("op@tracabilite.ia");
        request.setMotDePasse("secret123");
        request.setRole(RoleEnum.AGENT_CREDIT);

        when(utilisateurRepository.existsByEmail("op@tracabilite.ia")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hash");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        UtilisateurResponse response = service.creer(request);

        assertThat(response.getRole()).isEqualTo(RoleEnum.AGENT_CREDIT);
        assertThat(response.isActif()).isTrue();

        ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(captor.capture());
        assertThat(captor.getValue().getMotDePasseHash()).isEqualTo("hash");
        assertThat(captor.getValue().isActif()).isTrue();
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
    void desactiver_softDisablesWithoutDeleting() {
        UUID adminId = UUID.randomUUID();
        UUID operatorId = UUID.randomUUID();

        Utilisateur admin = new Utilisateur();
        admin.setId(adminId);
        admin.setRole(RoleEnum.ADMINISTRATEUR);
        admin.setActif(true);

        Utilisateur operator = new Utilisateur();
        operator.setId(operatorId);
        operator.setEmail("agent.credit@tracabilite.ia");
        operator.setRole(RoleEnum.AGENT_CREDIT);
        operator.setActif(true);

        when(utilisateurRepository.findById(operatorId)).thenReturn(Optional.of(operator));
        when(authService.getCurrentUser()).thenReturn(admin);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UtilisateurResponse response = service.desactiver(operatorId);

        assertThat(response.isActif()).isFalse();
        assertThat(operator.isActif()).isFalse();
        verify(utilisateurRepository).save(operator);
    }

    @Test
    void desactiver_rejectsSelfDeactivation() {
        UUID adminId = UUID.randomUUID();
        Utilisateur admin = new Utilisateur();
        admin.setId(adminId);
        admin.setRole(RoleEnum.ADMINISTRATEUR);
        admin.setActif(true);

        when(utilisateurRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(authService.getCurrentUser()).thenReturn(admin);

        assertThatThrownBy(() -> service.desactiver(adminId))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessageContaining("propre compte");
    }

    @Test
    void reactiver_setsActifTrue() {
        UUID id = UUID.randomUUID();
        Utilisateur user = new Utilisateur();
        user.setId(id);
        user.setRole(RoleEnum.AGENT_CREDIT);
        user.setActif(false);

        when(utilisateurRepository.findById(id)).thenReturn(Optional.of(user));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.reactiver(id).isActif()).isTrue();
    }

    @Test
    void desactiver_rejectsLastActiveAdmin() {
        UUID adminId = UUID.randomUUID();
        UUID otherAdminId = UUID.randomUUID();

        Utilisateur current = new Utilisateur();
        current.setId(otherAdminId);
        current.setRole(RoleEnum.ADMINISTRATEUR);
        current.setActif(true);

        Utilisateur lastAdmin = new Utilisateur();
        lastAdmin.setId(adminId);
        lastAdmin.setRole(RoleEnum.ADMINISTRATEUR);
        lastAdmin.setActif(true);

        when(utilisateurRepository.findById(adminId)).thenReturn(Optional.of(lastAdmin));
        when(authService.getCurrentUser()).thenReturn(current);
        when(utilisateurRepository.findAll()).thenReturn(List.of(lastAdmin, current));
        // current is also admin → not last; make current a non-admin for last-admin case
        current.setRole(RoleEnum.RESPONSABLE_CREDIT);

        assertThatThrownBy(() -> service.desactiver(adminId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dernier administrateur");
    }
}

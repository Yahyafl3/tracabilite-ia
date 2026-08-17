package com.pfa.tracabilite_ia.config;

import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDemoAccountSynchronizerTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    private AdminDemoAccountSynchronizer synchronizer;

    @BeforeEach
    void setUp() {
        synchronizer = new AdminDemoAccountSynchronizer(utilisateurRepository);
    }

    @Test
    void syncAdminEmail_updatesLegacyAdminEmail() {
        Utilisateur admin = admin(AdminDemoAccountSynchronizer.LEGACY_ADMIN_EMAIL, "hash-admin");
        when(utilisateurRepository.findByEmailIgnoreCase(AdminDemoAccountSynchronizer.TARGET_ADMIN_EMAIL))
                .thenReturn(Optional.empty());
        when(utilisateurRepository.findByEmailIgnoreCase(AdminDemoAccountSynchronizer.LEGACY_ADMIN_EMAIL))
                .thenReturn(Optional.of(admin));
        when(utilisateurRepository.existsByEmailIgnoreCaseAndIdNot(
                AdminDemoAccountSynchronizer.TARGET_ADMIN_EMAIL, admin.getId()
        )).thenReturn(false);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));

        synchronizer.syncAdminEmail();

        ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo(AdminDemoAccountSynchronizer.TARGET_ADMIN_EMAIL);
        assertThat(captor.getValue().getMotDePasseHash()).isEqualTo("hash-admin");
        assertThat(captor.getValue().getRole()).isEqualTo(RoleEnum.ADMINISTRATEUR);
        assertThat(captor.getValue().getNom()).isEqualTo("Administrateur");
    }

    @Test
    void syncAdminEmail_doesNotCreateDuplicateWhenTargetAlreadyAdmin() {
        Utilisateur admin = admin(AdminDemoAccountSynchronizer.TARGET_ADMIN_EMAIL, "hash");
        when(utilisateurRepository.findByEmailIgnoreCase(AdminDemoAccountSynchronizer.TARGET_ADMIN_EMAIL))
                .thenReturn(Optional.of(admin));

        synchronizer.syncAdminEmail();

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void syncAdminEmail_skipsWhenTargetEmailUsedByNonAdmin() {
        Utilisateur other = new Utilisateur();
        other.setId(UUID.randomUUID());
        other.setEmail(AdminDemoAccountSynchronizer.TARGET_ADMIN_EMAIL);
        other.setRole(RoleEnum.AGENT_CREDIT);
        when(utilisateurRepository.findByEmailIgnoreCase(AdminDemoAccountSynchronizer.TARGET_ADMIN_EMAIL))
                .thenReturn(Optional.of(other));

        synchronizer.syncAdminEmail();

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void syncAdminEmail_logsWhenAdminMissing() {
        when(utilisateurRepository.findByEmailIgnoreCase(AdminDemoAccountSynchronizer.TARGET_ADMIN_EMAIL))
                .thenReturn(Optional.empty());
        when(utilisateurRepository.findByEmailIgnoreCase(AdminDemoAccountSynchronizer.LEGACY_ADMIN_EMAIL))
                .thenReturn(Optional.empty());
        when(utilisateurRepository.findAll()).thenReturn(List.of());

        synchronizer.syncAdminEmail();

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void normalizeEmail_trimsAndLowercases() {
        assertThat(AdminDemoAccountSynchronizer.normalizeEmail("  0629378510A@Gmail.com "))
                .isEqualTo("0629378510a@gmail.com");
    }

    private Utilisateur admin(String email, String hash) {
        Utilisateur admin = new Utilisateur();
        admin.setId(UUID.randomUUID());
        admin.setNom("Administrateur");
        admin.setEmail(email);
        admin.setMotDePasseHash(hash);
        admin.setRole(RoleEnum.ADMINISTRATEUR);
        return admin;
    }
}

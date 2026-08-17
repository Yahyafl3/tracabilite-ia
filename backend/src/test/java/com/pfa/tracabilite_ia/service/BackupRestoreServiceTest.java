package com.pfa.tracabilite_ia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.response.BackupJobResponse;
import com.pfa.tracabilite_ia.dto.response.BackupVerifyResponse;
import com.pfa.tracabilite_ia.entities.BackupJob;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.BackupJobStatus;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.repository.BackupJobRepository;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import com.pfa.tracabilite_ia.util.HashUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackupRestoreServiceTest {

    @Mock
    private BackupJobRepository backupJobRepository;
    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @TempDir
    Path tempDir;

    private BackupRestoreService service;
    private Utilisateur admin;

    @BeforeEach
    void setUp() {
        service = new BackupRestoreService(
                backupJobRepository,
                decisionRepository,
                utilisateurRepository,
                auditLogService,
                new ObjectMapper().findAndRegisterModules(),
                passwordEncoder,
                tempDir.toString()
        );
        admin = new Utilisateur();
        admin.setId(UUID.randomUUID());
        admin.setNom("Admin");
        admin.setEmail("admin@test.fr");
        admin.setRole(RoleEnum.ADMINISTRATEUR);
        admin.setMotDePasseHash("should-never-appear");
        admin.setActif(true);
    }

    @Test
    void create_writesPackWithoutPasswordAndStoresSha256() throws Exception {
        when(utilisateurRepository.findAll()).thenReturn(List.of(admin));
        when(decisionRepository.findAllByOrderByTimestampAsc()).thenReturn(List.of());
        when(backupJobRepository.save(any(BackupJob.class))).thenAnswer(invocation -> {
            BackupJob job = invocation.getArgument(0);
            if (job.getId() == null) {
                job.setId(UUID.randomUUID());
            }
            return job;
        });

        BackupJobResponse response = service.create(admin);

        String json = Files.readString(tempDir.resolve(response.getId() + ".json"), StandardCharsets.UTF_8);
        assertThat(json).doesNotContain("should-never-appear");
        assertThat(json).doesNotContain("motDePasse");
        assertThat(json).contains("admin@test.fr");
        assertThat(response.getPackSha256()).isEqualTo(HashUtils.sha256(json.getBytes(StandardCharsets.UTF_8)));
        assertThat(response.getUserCount()).isEqualTo(1);
        assertThat(response.getDecisionCount()).isZero();
    }

    @Test
    void verify_detectsTamperedFile() throws Exception {
        when(utilisateurRepository.findAll()).thenReturn(List.of(admin));
        when(decisionRepository.findAllByOrderByTimestampAsc()).thenReturn(List.of());
        when(backupJobRepository.save(any(BackupJob.class))).thenAnswer(invocation -> {
            BackupJob job = invocation.getArgument(0);
            if (job.getId() == null) {
                job.setId(UUID.randomUUID());
            }
            return job;
        });

        BackupJobResponse created = service.create(admin);
        Path file = tempDir.resolve(created.getId() + ".json");
        Files.writeString(file, Files.readString(file) + " ", StandardCharsets.UTF_8);

        BackupJob stored = new BackupJob();
        stored.setId(created.getId());
        stored.setPackSha256(created.getPackSha256());
        stored.setFilename(created.getFilename());
        stored.setStatus(BackupJobStatus.CREATED);
        when(backupJobRepository.findById(created.getId())).thenReturn(Optional.of(stored));

        BackupVerifyResponse verify = service.verify(created.getId(), admin);

        assertThat(verify.isValid()).isFalse();
        assertThat(verify.getStatus()).isEqualTo(BackupJobStatus.VERIFIED_TAMPERED);
        assertThat(verify.getActualSha256()).isNotEqualTo(created.getPackSha256());
    }

    @Test
    void restore_skipsExistingUsersAndNeverCopiesPassword() {
        UUID jobId = UUID.randomUUID();
        when(backupJobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.restore(jobId, true, admin))
                .isInstanceOf(com.pfa.tracabilite_ia.exception.ResourceNotFoundException.class);

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void restore_requiresConfirmation() {
        UUID jobId = UUID.randomUUID();
        assertThatThrownBy(() -> service.restore(jobId, false, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("confirmation");
    }
}

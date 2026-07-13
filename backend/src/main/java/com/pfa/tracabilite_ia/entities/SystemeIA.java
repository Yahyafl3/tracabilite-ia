package com.pfa.tracabilite_ia.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "systeme_ia", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"nom", "fournisseur"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemeIA {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID systemeIaId;

	@Column(nullable = false)
	private String nom;

	@Column(nullable = false)
	private String fournisseur;

	@Column(nullable = false)
	private String modele;

	private String versionModele;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private Boolean actif = Boolean.TRUE;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime dateCreation;

	@UpdateTimestamp
	private LocalDateTime dateModification;
}

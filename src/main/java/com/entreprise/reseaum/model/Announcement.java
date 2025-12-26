package com.entreprise.reseaum.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Announcement {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String titre;
	private String contenu;
	private LocalDateTime datePublication = LocalDateTime.now();
	private String auteur; // "RH Team"

	@ManyToOne
	private Department department; // Filtré par département

	// Getters/Setters
	public Long getId() {
		return id;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getTitre() {
		return titre;
	}

	public void setContenu(String contenu) {
		this.contenu = contenu;
	}

	public String getContenu() {
		return contenu;
	}

	public LocalDateTime getDatePublication() {
		return datePublication;
	}

	public void setAuteur(String auteur) {
		this.auteur = auteur;
	}

	public String getAuteur() {
		return auteur;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}
}

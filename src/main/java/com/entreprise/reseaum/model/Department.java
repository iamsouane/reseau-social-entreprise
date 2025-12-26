package com.entreprise.reseaum.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "department") // Minuscule pour correspondre à votre table
public class Department {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nom;

	private String description;

	@OneToMany(mappedBy = "department")
	private List<User> employees;

	// ✅ CONSTRUCTEUR PAR DÉFAUT OBLIGATOIRE pour JPA
	public Department() {
	}

	// ✅ Constructeur pratique
	public Department(String nom) {
		this.nom = nom;
	}

	// Getters/Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}

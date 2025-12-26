package com.entreprise.reseaum.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String nom;

	private String prenom;
	private String poste;
	private String departement;

	@Column(length = 1000)
	private String bio;

	private LocalDateTime createdAt = LocalDateTime.now();

	private String role = "EMPLOYEE"; // EMPLOYEE, ADMIN
	private boolean actif = false; // PENDING jusqu'à validation admin

	@Column(nullable = false)
	private String password; // ← AJOUTÉ

	@ManyToOne
	private Department department;

	// GETTERS
	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getNom() {
		return nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public String getPoste() {
		return poste;
	}

	public String getDepartement() {
		return departement;
	}

	public String getBio() {
		return bio;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public boolean isActif() {
		return actif;
	}

	// SETTERS
	public void setId(Long id) {
		this.id = id;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public void setPoste(String poste) {
		this.poste = poste;
	}

	public void setDepartement(String departement) {
		this.departement = departement;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}
}

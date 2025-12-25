package com.entreprise.reseaum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegister {

	@Email(message = "Email invalide")
	@NotBlank
	private String email;

	@NotBlank(message = "Nom obligatoire")
	@Size(min = 2, max = 50)
	private String nom;

	@Size(min = 2, max = 50)
	private String prenom;

	@Size(max = 100)
	private String poste;

	@Size(max = 1000)
	private String bio;

	@NotBlank(message = "Mot de passe obligatoire")
	@Size(min = 6, message = "Minimum 6 caractères")
	private String password; // ← AJOUTÉ !

	// GETTERS
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

	public String getBio() {
		return bio;
	}

	public String getPassword() {
		return password;
	} // ← AJOUTÉ !

	// SETTERS
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

	public void setBio(String bio) {
		this.bio = bio;
	}

	public void setPassword(String password) {
		this.password = password;
	} // ← AJOUTÉ !
}

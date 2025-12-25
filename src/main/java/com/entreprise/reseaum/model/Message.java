package com.entreprise.reseaum.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Message {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private User sender;

	@ManyToOne
	private User receiver;

	private String contenu;
	private LocalDateTime date = LocalDateTime.now();
	private boolean lu = false;

	// SETTERS
	public void setSender(User sender) {
		this.sender = sender;
	}

	public void setReceiver(User receiver) {
		this.receiver = receiver;
	}

	public void setContenu(String contenu) {
		this.contenu = contenu;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public void setLu(boolean lu) {
		this.lu = lu;
	}

	// GETTERS
	public Long getId() {
		return id;
	}

	public User getSender() {
		return sender;
	}

	public User getReceiver() {
		return receiver;
	}

	public String getContenu() {
		return contenu;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public boolean isLu() {
		return lu;
	}
}

package com.entreprise.reseaum.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.entreprise.reseaum.model.User;
import com.entreprise.reseaum.repository.UserRepository;

@Component
public class DataLoader implements CommandLineRunner {
	@Autowired
	private UserRepository userRepository;

	@Override
	public void run(String... args) {
		if (!userRepository.existsByEmail("admin@entreprise.com")) {
			User admin = new User();
			admin.setEmail("admin@gmail.com");
			admin.setNom("Admin");
			admin.setPrenom("RH");
			admin.setPoste("Administrateur");
			admin.setRole("ADMIN");
			admin.setActif(true);
			admin.setPassword("admin123"); // ← PLAIN TEXT (PROD = BCrypt)
			userRepository.save(admin);
		}
	}
}

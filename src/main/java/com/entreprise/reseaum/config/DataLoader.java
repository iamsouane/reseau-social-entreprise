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
		// ✅ ADMIN (1 seul email cohérent)
		if (!userRepository.existsByEmail("admin@gmail.com")) {
			User admin = new User();
			admin.setEmail("admin@gmail.com");
			admin.setNom("Admin");
			admin.setPrenom("RH");
			admin.setPoste("Administrateur");
			admin.setRole("ADMIN");
			admin.setActif(true);
			admin.setPassword("admin123");
			userRepository.save(admin);
		}

		// ✅ EMPLOYÉ 1
		if (!userRepository.existsByEmail("employe1@gmail.com")) {
			User emp1 = new User();
			emp1.setEmail("employe1@gmail.com");
			emp1.setNom("DUPONT");
			emp1.setPrenom("Jean");
			emp1.setPoste("Développeur");
			emp1.setRole("EMPLOYEE");
			emp1.setActif(true);
			emp1.setPassword("123");
			userRepository.save(emp1);
		}

		// ✅ EMPLOYÉ 2
		if (!userRepository.existsByEmail("employe2@entreprise.com")) {
			User emp2 = new User();
			emp2.setEmail("employe2@entreprise.com");
			emp2.setNom("MARTIN");
			emp2.setPrenom("Marie");
			emp2.setPoste("Designer");
			emp2.setRole("EMPLOYEE");
			emp2.setActif(true);
			emp2.setPassword("123");
			userRepository.save(emp2);
		}
	}
}

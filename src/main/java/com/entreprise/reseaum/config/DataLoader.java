package com.entreprise.reseaum.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.entreprise.reseaum.model.User;
import com.entreprise.reseaum.model.Department;
import com.entreprise.reseaum.model.Announcement;
import com.entreprise.reseaum.repository.UserRepository;
import com.entreprise.reseaum.repository.DepartmentRepository;
import com.entreprise.reseaum.repository.AnnouncementRepository;
import java.util.Optional;

@Component
public class DataLoader implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private AnnouncementRepository announcementRepository;

	@Override
	public void run(String... args) throws Exception {
		// ✅ DÉPARTEMENTS (sécurisé)
		if (departmentRepository.count() == 0) {
			Department dev = new Department("Développement");
			departmentRepository.save(dev);

			Department marketing = new Department("Marketing");
			departmentRepository.save(marketing);

			Department rh = new Department("RH");
			departmentRepository.save(rh);

			Department compta = new Department("Comptabilité");
			departmentRepository.save(compta);

			System.out.println("✅ 4 départements créés");
		}

		// ✅ ADMIN RH
		if (!userRepository.existsByEmail("admin@rh.com")) {
			User admin = new User();
			admin.setEmail("admin@rh.com");
			admin.setNom("Admin");
			admin.setPrenom("RH");
			admin.setPoste("Administrateur RH");
			admin.setRole("ADMIN");
			admin.setActif(true);
			admin.setPassword("admin123");
			userRepository.save(admin);
			System.out.println("✅ Admin RH : admin@rh.com / admin123");
		}

		// ✅ EMPLOYÉ 1
		Optional<Department> devDept = departmentRepository.findByNom("Développement");
		if (!userRepository.existsByEmail("employe1@entreprise.com") && devDept.isPresent()) {
			User emp1 = new User();
			emp1.setEmail("employe1@entreprise.com");
			emp1.setNom("DUPONT");
			emp1.setPrenom("Jean");
			emp1.setPoste("Développeur Senior");
			emp1.setRole("EMPLOYEE");
			emp1.setActif(true);
			emp1.setPassword("123");
			emp1.setBio("Développeur Java/Spring Boot");
			emp1.setDepartment(devDept.get());
			userRepository.save(emp1);
			System.out.println("✅ Employé 1 : employe1@entreprise.com / 123");
		}

		// ✅ EMPLOYÉ 2
		Optional<Department> marketingDept = departmentRepository.findByNom("Marketing");
		if (!userRepository.existsByEmail("employe2@entreprise.com") && marketingDept.isPresent()) {
			User emp2 = new User();
			emp2.setEmail("employe2@entreprise.com");
			emp2.setNom("MARTIN");
			emp2.setPrenom("Marie");
			emp2.setPoste("Designer UI/UX");
			emp2.setRole("EMPLOYEE");
			emp2.setActif(true);
			emp2.setPassword("123");
			emp2.setBio("Spécialiste design interfaces");
			emp2.setDepartment(marketingDept.get());
			userRepository.save(emp2);
			System.out.println("✅ Employé 2 : employe2@entreprise.com / 123");
		}

		// ✅ ANNONCES
		if (announcementRepository.count() == 0) {
			Optional<Department> rhDept = departmentRepository.findByNom("RH");
			if (rhDept.isPresent()) {
				Announcement ann1 = new Announcement();
				ann1.setTitre("🎄 Joyeux Noël 2025 !");
				ann1.setContenu("Joyeux Noël à toute l'équipe ! Repos mérité pour tous.");
				ann1.setAuteur("RH Team");
				ann1.setDepartment(rhDept.get());
				announcementRepository.save(ann1);
			}

			System.out.println("✅ 1 annonce RH créée");
		}

		System.out.println("🎉 DataLoader terminé - Réseau entreprise prêt !");
	}
}

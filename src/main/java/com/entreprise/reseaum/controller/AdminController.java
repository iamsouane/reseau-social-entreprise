package com.entreprise.reseaum.controller; // ← controller PAS config

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // ✅ SPRING Model
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.entreprise.reseaum.dto.UserLogin;
import com.entreprise.reseaum.model.User;
import com.entreprise.reseaum.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/login")
	public String adminLogin(Model model) {
		model.addAttribute("login", new UserLogin());
		return "admin-login";
	}

	@PostMapping("/login")
	public String processLogin(@ModelAttribute UserLogin login, Model model, HttpSession session) {
		User admin = userRepository.findByEmail(login.getEmail()).orElse(null);
		if (admin != null && admin.getPassword().equals(login.getPassword()) && "ADMIN".equals(admin.getRole())) {
			session.setAttribute("admin", admin); // ← SESSION !
			return "redirect:/admin/users";
		}
		model.addAttribute("error", "Identifiants incorrects");
		return "admin-login";
	}

}

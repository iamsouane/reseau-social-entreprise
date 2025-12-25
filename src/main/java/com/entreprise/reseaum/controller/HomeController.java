package com.entreprise.reseaum.controller;

import com.entreprise.reseaum.dto.UserLogin;
import com.entreprise.reseaum.dto.UserRegister;
import com.entreprise.reseaum.model.Message;
import com.entreprise.reseaum.model.User;
import com.entreprise.reseaum.repository.MessageRepository;
import com.entreprise.reseaum.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MessageRepository messageRepository;

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Réseau Social Entreprise");
		model.addAttribute("userCount", userRepository.count());
		return "index";
	}

	@GetMapping("/register")
	public String showRegisterForm(Model model) {
		model.addAttribute("userRegister", new UserRegister());
		return "register";
	}

	@PostMapping("/register")
	public String register(@Valid @ModelAttribute UserRegister userRegister, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return "register";
		}

		if (userRepository.existsByEmail(userRegister.getEmail())) {
			model.addAttribute("error", "Email déjà utilisé");
			return "register";
		}

		User user = new User();
		user.setEmail(userRegister.getEmail());
		user.setNom(userRegister.getNom());
		user.setPrenom(userRegister.getPrenom());
		user.setPoste(userRegister.getPoste());
		user.setBio(userRegister.getBio());
		user.setPassword(userRegister.getPassword());
		user.setRole("EMPLOYEE");
		user.setActif(false);

		userRepository.save(user);
		model.addAttribute("success", "Compte créé ! En attente validation admin RH.");
		return "register";
	}

	@GetMapping("/users")
	public String listUsers(Model model, HttpServletRequest request) {
		HttpSession session = request.getSession();
		User admin = (User) session.getAttribute("admin");
		if (admin == null) {
			return "redirect:/admin/login";
		}
		model.addAttribute("users", userRepository.findAll());
		return "users";
	}

	@GetMapping("/admin/users")
	public String adminUsers(Model model, HttpServletRequest request) {
		HttpSession session = request.getSession();
		User admin = (User) session.getAttribute("admin");
		if (admin == null) {
			return "redirect:/admin/login";
		}
		model.addAttribute("pendingUsers", userRepository.findAllByActifFalse());
		model.addAttribute("allUsers", userRepository.findAll());
		return "admin-users";
	}

	@PostMapping("/admin/approve/{id}")
	public String approveUser(@PathVariable Long id) {
		User user = userRepository.findById(id).orElse(null);
		if (user != null) {
			user.setActif(true);
			userRepository.save(user);
		}
		return "redirect:/admin/users";
	}

	@GetMapping("/employee/login")
	public String employeeLogin(Model model) {
		model.addAttribute("login", new UserLogin());
		return "employee-login";
	}

	@PostMapping("/employee/login")
	public String processEmployeeLogin(@ModelAttribute UserLogin login, Model model, HttpSession session) {

		// Recherche utilisateur
		User user = userRepository.findByEmail(login.getEmail()).orElse(null);

		// Email inexistant
		if (user == null) {
			model.addAttribute("error", "Email inconnu");
			model.addAttribute("login", login); // Garde champs remplis
			return "employee-login";
		}

		// Mot de passe incorrect
		if (!user.getPassword().equals(login.getPassword())) {
			model.addAttribute("error", "Mot de passe incorrect");
			model.addAttribute("login", login);
			return "employee-login";
		}

		// Compte en attente validation
		if (!user.isActif()) {
			model.addAttribute("pending", true);
			model.addAttribute("error", "Compte en attente de validation admin RH");
			model.addAttribute("login", login);
			return "employee-login";
		}

		// Pas un employé
		if (!"EMPLOYEE".equals(user.getRole())) {
			model.addAttribute("error", "Accès réservé aux employés");
			model.addAttribute("login", login);
			return "employee-login";
		}

		// ✅ CONNEXION RÉUSSIE
		session.setAttribute("employee", user);
		return "redirect:/employee/profile";
	}

	@GetMapping("/employee/profile")
	public String employeeProfile(Model model, HttpServletRequest request) {
		HttpSession session = request.getSession();
		User employee = (User) session.getAttribute("employee");
		if (employee == null) {
			return "redirect:/employee/login";
		}
		model.addAttribute("user", employee);
		return "employee-profile";
	}

	@GetMapping("/employee/logout")
	public String employeeLogout(HttpSession session) {
		session.removeAttribute("employee");
		return "redirect:/";
	}

	@GetMapping("/admin/logout")
	public String adminLogout(HttpSession session) {
		session.removeAttribute("admin");
		return "redirect:/";
	}

	@PostMapping("/employee/profile/update")
	public String updateProfile(@Valid @ModelAttribute User user, BindingResult result, HttpSession session,
			Model model) {

		if (result.hasErrors()) {
			model.addAttribute("user", user);
			return "employee-profile"; // Retourne même page avec erreurs
		}

		// Vérifier utilisateur connecté
		User currentUser = (User) session.getAttribute("employee");
		if (currentUser == null || !currentUser.getId().equals(user.getId())) {
			return "redirect:/employee/login";
		}

		// Mettre à jour champs
		currentUser.setNom(user.getNom());
		currentUser.setPrenom(user.getPrenom());
		currentUser.setPoste(user.getPoste());
		currentUser.setBio(user.getBio());

		// Sauvegarder en base
		userRepository.save(currentUser);

		// Mettre à jour session
		session.setAttribute("employee", currentUser);

		return "redirect:/employee/profile"; // Refresh page
	}

	@GetMapping("/employee/messages")
	public String employeeMessages(HttpSession session, Model model) {
		User employee = (User) session.getAttribute("employee");
		if (employee == null)
			return "redirect:/employee/login";

		// Messages non lus
		List<Message> nonLus = messageRepository.findByReceiverAndLuFalseOrderByDateDesc(employee);
		// Tous messages reçus
		List<Message> recus = messageRepository.findByReceiverOrderByDateDesc(employee);
		// Messages envoyés
		List<Message> envoyes = messageRepository.findBySenderOrderByDateDesc(employee);

		model.addAttribute("nonLus", nonLus);
		model.addAttribute("recus", recus);
		model.addAttribute("envoyes", envoyes);
		model.addAttribute("user", employee);
		model.addAttribute("users", userRepository.findAll());

		return "employee-messages";
	}

	@PostMapping("/employee/send")
	public String sendMessage(@RequestParam Long receiverId, @RequestParam String contenu, HttpSession session,
			RedirectAttributes redirectAttributes) {
		User sender = (User) session.getAttribute("employee");
		if (sender == null)
			return "redirect:/employee/login";

		User receiver = userRepository.findById(receiverId).orElse(null);
		if (receiver != null && !sender.getId().equals(receiverId)) {
			Message message = new Message();
			message.setSender(sender);
			message.setReceiver(receiver);
			message.setContenu(contenu);
			messageRepository.save(message);

			redirectAttributes.addFlashAttribute("success", "Message envoyé !");
		}
		return "redirect:/employee/messages";
	}

}

package com.entreprise.reseaum.controller;

import com.entreprise.reseaum.dto.UserLogin;
import com.entreprise.reseaum.dto.UserRegister;
import com.entreprise.reseaum.model.Message;
import com.entreprise.reseaum.model.User;
import com.entreprise.reseaum.model.Department;
import com.entreprise.reseaum.model.Announcement;
import com.entreprise.reseaum.repository.AnnouncementRepository;
import com.entreprise.reseaum.repository.DepartmentRepository;
import com.entreprise.reseaum.repository.MessageRepository;
import com.entreprise.reseaum.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private AnnouncementRepository announcementRepository;

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
		if (result.hasErrors())
			return "register";
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

	// ========== ADMIN RH ==========
	@GetMapping("/admin/login")
	public String adminLogin(Model model) {
		model.addAttribute("login", new UserLogin());
		return "admin-login";
	}

	@PostMapping("/admin/login")
	public String processAdminLogin(@ModelAttribute UserLogin login, Model model, HttpSession session) {
		// admin@rh.com / admin123
		if ("admin@rh.com".equals(login.getEmail()) && "admin123".equals(login.getPassword())) {
			User adminUser = new User();
			adminUser.setId(0L);
			adminUser.setPrenom("Admin");
			adminUser.setNom("RH");
			adminUser.setEmail("admin@rh.com");
			adminUser.setRole("ADMIN");
			session.setAttribute("admin", adminUser);
			return "redirect:/admin/dashboard";
		}
		model.addAttribute("error", "Identifiants incorrects (admin@rh.com / admin123)");
		model.addAttribute("login", login);
		return "admin-login";
	}

	@GetMapping("/admin/dashboard")
	@Transactional
	public String adminDashboard(HttpSession session, Model model) {
		User admin = (User) session.getAttribute("admin");
		if (admin == null)
			return "redirect:/admin/login";

		// Employés en attente validation
		List<User> pendingUsers = userRepository.findAll().stream().filter(u -> !u.isActif())
				.collect(Collectors.toList());

		List<Department> departments = departmentRepository.findAll();
		List<Announcement> recentAnnonces = announcementRepository.findTop5ByOrderByDatePublicationDesc();

		model.addAttribute("pendingUsers", pendingUsers);
		model.addAttribute("departments", departments);
		model.addAttribute("annonces", recentAnnonces);
		model.addAttribute("admin", admin);
		return "admin-dashboard";
	}

	@PostMapping("/admin/validate/{userId}/{departmentId}")
	@Transactional
	public String validateUser(@PathVariable Long userId, @PathVariable Long departmentId, HttpSession session) {
		User user = userRepository.findById(userId).orElse(null);
		Department dept = departmentRepository.findById(departmentId).orElse(null);
		if (user != null && !user.isActif() && dept != null) {
			user.setActif(true);
			user.setDepartment(dept);
			userRepository.save(user);
		}
		return "redirect:/admin/dashboard";
	}

	@PostMapping("/admin/announce")
	@Transactional
	public String createAnnouncement(@RequestParam String titre, @RequestParam String contenu,
			@RequestParam(required = false) Long departmentId, HttpSession session) {
		Announcement annonce = new Announcement();
		annonce.setTitre(titre);
		annonce.setContenu(contenu);
		if (departmentId != null) {
			Department dept = departmentRepository.findById(departmentId).orElse(null);
			annonce.setDepartment(dept);
		}
		announcementRepository.save(annonce);
		return "redirect:/admin/dashboard";
	}

	@GetMapping("/admin/logout")
	public String adminLogout(HttpSession session) {
		session.removeAttribute("admin");
		return "redirect:/";
	}

	// ========== EMPLOYÉ ==========
	@GetMapping("/employee/login")
	public String employeeLogin(Model model) {
		model.addAttribute("login", new UserLogin());
		return "employee-login";
	}

	@PostMapping("/employee/login")
	public String processEmployeeLogin(@ModelAttribute UserLogin login, Model model, HttpSession session) {
		User user = userRepository.findByEmail(login.getEmail()).orElse(null);
		if (user == null || !user.getPassword().equals(login.getPassword()) || !user.isActif()
				|| !"EMPLOYEE".equals(user.getRole())) {
			model.addAttribute("error", "Identifiants incorrects");
			model.addAttribute("login", login);
			return "employee-login";
		}
		session.setAttribute("employee", user);
		return "redirect:/employee/dashboard";
	}

	@GetMapping("/employee/profile")
	public String employeeProfile(Model model, HttpServletRequest request) {
		User employee = (User) request.getSession().getAttribute("employee");
		if (employee == null)
			return "redirect:/employee/login";
		model.addAttribute("user", employee);
		return "employee-profile";
	}

	@PostMapping("/employee/profile/update")
	@Transactional
	public String updateProfile(@Valid @ModelAttribute User user, BindingResult result, HttpSession session,
			Model model, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			User sessionUser = (User) session.getAttribute("employee");
			model.addAttribute("user", sessionUser);
			return "employee-profile";
		}

		User sessionUser = (User) session.getAttribute("employee");
		if (sessionUser != null && sessionUser.getId().equals(user.getId())) {
			// ✅ Mise à jour autorisée (même utilisateur)
			user.setId(sessionUser.getId());
			user.setEmail(sessionUser.getEmail()); // Email fixe
			user.setPassword(sessionUser.getPassword()); // Password fixe
			user.setRole(sessionUser.getRole()); // Role fixe
			user.setActif(sessionUser.isActif()); // Statut fixe

			userRepository.save(user);
			session.setAttribute("employee", user); // ✅ Refresh session
			redirectAttributes.addFlashAttribute("success", "✅ Profil mis à jour !");
		}

		return "redirect:/employee/profile";
	}

	@GetMapping("/employee/dashboard")
	@Transactional(readOnly = true)
	public String employeeDashboard(HttpSession session, Model model) {
		User employee = (User) session.getAttribute("employee");
		if (employee == null)
			return "redirect:/employee/login";

		int nonLusCount = messageRepository.findByReceiverAndLuFalseOrderByDateDesc(employee).size();
		List<Announcement> annonces = announcementRepository.findTop5ByOrderByDatePublicationDesc();

		// ✅ DÉPARTEMENTS + COMPTAGE RÉEL
		List<Department> departments = departmentRepository.findAll();
		Map<Long, Long> deptEmployeeCount = new HashMap<>();

		for (Department dept : departments) {
			long count = userRepository.countByDepartmentId(dept.getId());
			deptEmployeeCount.put(dept.getId(), count);
		}

		model.addAttribute("user", employee);
		model.addAttribute("nonLusCount", nonLusCount);
		model.addAttribute("annonces", annonces);
		model.addAttribute("departments", departments);
		model.addAttribute("deptEmployeeCount", deptEmployeeCount); // ✅ Pour Thymeleaf
		return "employee-dashboard";
	}

	@GetMapping("/employee/logout")
	public String employeeLogout(HttpSession session) {
		session.removeAttribute("employee");
		return "redirect:/";
	}

	@GetMapping("/employee/messages")
	@Transactional(readOnly = true)
	public String employeeMessages(HttpSession session, Model model) {
		User employee = (User) session.getAttribute("employee");
		if (employee == null)
			return "redirect:/employee/login";

		List<User> conversations = getConversationsWithMessages(employee);
		Map<Long, List<Message>> nonLusParContact = new HashMap<>();
		List<Message> tousNonLus = messageRepository.findByReceiverAndLuFalseOrderByDateDesc(employee);

		for (Message msg : tousNonLus) {
			Long senderId = msg.getSender().getId();
			nonLusParContact.computeIfAbsent(senderId, k -> new ArrayList<>()).add(msg);
		}

		int nonLusCount = tousNonLus.size();
		List<User> allEmployees = userRepository.findAll().stream().filter(u -> "EMPLOYEE".equals(u.getRole()))
				.filter(User::isActif).filter(u -> !u.getId().equals(employee.getId())).collect(Collectors.toList());

		model.addAttribute("nonLusCount", nonLusCount);
		model.addAttribute("nonLusParContact", nonLusParContact);
		model.addAttribute("employees", conversations);
		model.addAttribute("allEmployees", allEmployees);
		model.addAttribute("user", employee);
		return "employee-messages";
	}

	@PostMapping("/employee/send")
	@Transactional
	public String sendMessage(@RequestParam Long receiverId, @RequestParam String contenu, HttpSession session) {
		User sender = (User) session.getAttribute("employee");
		if (sender == null)
			return "redirect:/employee/login";

		User receiver = userRepository.findById(receiverId).orElse(null);
		if (receiver != null && !sender.getId().equals(receiverId) && contenu.trim().length() > 0) {
			Message message = new Message();
			message.setSender(sender);
			message.setReceiver(receiver);
			message.setContenu(contenu.trim());
			message.setDate(LocalDateTime.now());
			message.setLu(false);
			messageRepository.save(message);
		}
		return "redirect:/employee/conversation/" + receiverId;
	}

	@GetMapping("/employee/conversation/{userId}")
	@Transactional
	public String conversation(@PathVariable Long userId, HttpSession session, Model model) {
		User employee = (User) session.getAttribute("employee");
		if (employee == null)
			return "redirect:/employee/login";

		User interlocuteur = userRepository.findById(userId).orElse(null);
		if (interlocuteur == null)
			return "redirect:/employee/messages";

		messageRepository.markAsReadBetween(employee.getId(), interlocuteur.getId());
		List<Message> messages = messageRepository.findConversationBetween(employee.getId(), interlocuteur.getId());

		model.addAttribute("user", employee);
		model.addAttribute("interlocuteur", interlocuteur);
		model.addAttribute("messages", messages != null ? messages : new ArrayList<>());
		return "employee-conversation";
	}

	private List<User> getConversationsWithMessages(User employee) {
		List<User> conversations = new ArrayList<>();
		List<Message> mesMessages = messageRepository.findBySenderOrderByDateDesc(employee);
		mesMessages.addAll(messageRepository.findByReceiverOrderByDateDesc(employee));

		for (Message msg : mesMessages) {
			User contact = msg.getSender().getId().equals(employee.getId()) ? msg.getReceiver() : msg.getSender();
			if (!conversations.contains(contact) && "EMPLOYEE".equals(contact.getRole()) && contact.isActif()) {
				conversations.add(contact);
			}
		}
		return conversations.stream().distinct().collect(Collectors.toList());
	}

	@GetMapping("/employee/department/{id}")
	@Transactional(readOnly = true)
	public String employeeDepartment(@PathVariable Long id, HttpSession session, Model model) {
		User employee = (User) session.getAttribute("employee");
		if (employee == null)
			return "redirect:/employee/login";

		Department department = departmentRepository.findById(id).orElse(null);
		if (department == null)
			return "redirect:/employee/dashboard";

		// ✅ LISTE EMPLOYES DU DÉPARTEMENT (actifs seulement)
		List<User> employees = userRepository.findByDepartmentId(id).stream().filter(User::isActif)
				.filter(u -> !"ADMIN".equals(u.getRole())) // Exclut admins
				.sorted((u1, u2) -> u1.getPrenom().compareToIgnoreCase(u2.getPrenom())).collect(Collectors.toList());

		int nonLusCount = messageRepository.findByReceiverAndLuFalseOrderByDateDesc(employee).size();

		model.addAttribute("user", employee);
		model.addAttribute("department", department);
		model.addAttribute("employees", employees);
		model.addAttribute("nonLusCount", nonLusCount);
		model.addAttribute("employeeCount", employees.size());

		return "employee-department";
	}

}

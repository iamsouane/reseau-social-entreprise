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

import java.time.LocalDateTime;
import java.util.ArrayList;
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
		return "redirect:/employee/profile";
	}

	@GetMapping("/employee/profile")
	public String employeeProfile(Model model, HttpServletRequest request) {
		User employee = (User) request.getSession().getAttribute("employee");
		if (employee == null)
			return "redirect:/employee/login";
		model.addAttribute("user", employee);
		return "employee-profile";
	}

	@GetMapping("/employee/logout")
	public String employeeLogout(HttpSession session) {
		session.removeAttribute("employee");
		return "redirect:/";
	}

	@GetMapping("/employee/messages")
	@Transactional
	public String employeeMessages(HttpSession session, Model model) {
		User employee = (User) session.getAttribute("employee");
		if (employee == null)
			return "redirect:/employee/login";

		// Conversations existantes
		List<User> conversations = getConversationsWithMessages(employee);

		// NON LUS PAR CONTACT ✅
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
}

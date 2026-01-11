package com.example.demo.user;

import com.example.demo.security.Role;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegisterController {

    private final UserService service;

    public RegisterController(UserService service) {
        this.service = service;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user) {
        service.register(user);
        return "redirect:/login";
    }
}


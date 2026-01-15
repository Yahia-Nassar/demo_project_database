package com.example.demo.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class PasswordResetController {

    private final UserService userService;
    private final PasswordResetService resetService;

    public PasswordResetController(UserService userService, PasswordResetService resetService) {
        this.userService = userService;
        this.resetService = resetService;
    }

    @GetMapping("/password-reset")
    public String requestForm() {
        return "password-reset-request";
    }

    @PostMapping("/password-reset")
    public String request(@RequestParam String email, Model model) {
        Optional<User> user = userService.findByEmailOptional(email);
        user.ifPresent(resetService::createToken);
        model.addAttribute("email", email);
        return "password-reset-confirm";
    }

    @GetMapping("/password-reset/{token}")
    public String resetForm(@PathVariable String token, Model model) {
        Optional<PasswordResetToken> resetToken = resetService.findValidToken(token);
        if (resetToken.isEmpty()) {
            model.addAttribute("error", "Reset link is invalid or expired.");
            return "password-reset-request";
        }
        model.addAttribute("token", token);
        return "password-reset-form";
    }

    @PostMapping("/password-reset/{token}")
    public String reset(@PathVariable String token,
                        @RequestParam String password,
                        @RequestParam String confirmPassword,
                        Model model) {
        Optional<PasswordResetToken> resetToken = resetService.findValidToken(token);
        if (resetToken.isEmpty()) {
            model.addAttribute("error", "Reset link is invalid or expired.");
            return "password-reset-request";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Passwords do not match.");
            return "password-reset-form";
        }
        User user = resetToken.get().getUser();
        userService.updatePassword(user, password);
        resetService.markUsed(resetToken.get());
        return "redirect:/login?reset=success";
    }
}
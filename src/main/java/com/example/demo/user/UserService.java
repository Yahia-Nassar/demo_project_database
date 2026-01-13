package com.example.demo.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


import jakarta.transaction.Transactional;

import com.example.demo.security.Role;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public User currentUser() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }

        Object principal = auth.getPrincipal();

        String email;

        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername(); // email
        } else {
            email = principal.toString();
        }

        return repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }


    public void register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        repository.save(user);
    }

    public User findByEmail(String email) {
        return repository.findByEmail(email).orElseThrow();
    }

    public Optional<User> findByEmailOptional(String email) {
        return repository.findByEmail(email);
    }

    public List<User> findDevelopers() {
        return repository.findByRole(Role.DEVELOPER);
    }

    public void updatePassword(User user, String rawPassword) {
        user.setPassword(encoder.encode(rawPassword));
        repository.save(user);
    }

}
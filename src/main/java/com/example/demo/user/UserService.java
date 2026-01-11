package com.example.demo.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import com.example.demo.security.Role;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public void register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        repository.save(user);
    }

    public User findByEmail(String email) {
        return repository.findByEmail(email).orElseThrow();
    }

    public List<User> findDevelopers() {
        return repository.findByRole(Role.DEVELOPER);
    }

}

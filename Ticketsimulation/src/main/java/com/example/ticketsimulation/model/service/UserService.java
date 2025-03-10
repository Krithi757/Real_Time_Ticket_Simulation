package com.example.ticketsimulation.model.service;

import com.example.ticketsimulation.model.entity.User;
import com.example.ticketsimulation.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository; // Repository to interact with the user database
    private final Random random = new Random(); // Random generator for username creation
    private String username;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    // Checks if a user with the given username is already registered
    public boolean isUserRegistered(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // Generates a unique username that doesn’t already exist in the database
    private String generateUsername() {



        String username;
        do {
            username = "user" + (random.nextInt());
        } while (isUserRegistered(username));
        return username;
    }


    // Registers a new user with a randomly generated username, a hashed password, and a specified role
    public String registerNewUser(String username, String password, User.Role role, boolean isRealtime) {
        if (!isRealtime) {
            // Generate a username for non-real-time customers
            username = generateUsername();
        }
        register(username, password, role);
        return username; // Return the provided or generated username
    }

    private void register(String username, String password, User.Role role) {
        if (isUserRegistered(username)) {
            throw new RuntimeException("User already exists!");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        userRepository.save(user);
    }

    // Attempts to log in a user by checking if the username exists and if the password matches
    public void login(String username, String password) {
        // Login logic
        if (!isUserRegistered(username)) {
            throw new RuntimeException("User not found!");
        }

        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new RuntimeException("User not found!")
        );

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }
    }
}

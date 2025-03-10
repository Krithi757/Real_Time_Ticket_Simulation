package com.example.ticketsimulation.model.service;



import com.example.ticketsimulation.model.entity.User;
import com.example.ticketsimulation.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    // Method to load a user by username
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find user by username
        Optional<User> optionalUser = userRepository.findByUsername(username);

        // Check if user is present
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        User user = optionalUser.get(); // Retrieve user
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),  // Gets user password
                user.getAuthorities() // Ensure User class has a method for authorities like their roles
        );
    }
}

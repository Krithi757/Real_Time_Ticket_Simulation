package com.example.ticketsimulation.model.entity;

import jakarta.persistence.*;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

//Entity representing a User in the ticket simulation system
//Implements UserDetails to integrate with Spring Security for authentication and authorization
@Entity
@Table(name = "users") // Specifies the database table for this entity
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // User ID field

    @Setter
    private String username; // Username for login

    @Setter
    private String password; // Hashed password for authentication
    // This collection stores the roles assigned to the user
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id")) // Defines a separate table for roles
    @Column(name = "role") // Specifies that the role will be stored as a column in the "user_roles" table
    private Collection<String> roles = new HashSet<>(); // Initialize the roles collection

    // Overriding method from UserDetails to provide the roles for this user
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> (GrantedAuthority) () -> "ROLE_" + role)  // Prefix with ROLE_ for Spring Security
                .collect(Collectors.toSet());
    }

    public void setRole(Role role) {
        roles.clear();  // Clear existing roles
        roles.add(role.name()); // Add the new role as a string
    }
    // Enum representing possible roles for the user
    public enum Role {
        CUSTOMER, VENDOR
    }
    // Method to get the user's password (hashed) for authentication
    @Override
    public String getPassword() {
        return password; // Return the stored hashed password
    }
    // Method to get the user's username for login
    @Override
    public String getUsername() {
        return username; // Return the username
    }
    // Overridden method from UserDetails to check the account status
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Override methods from UserDetail interface
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Override methods from UserDetail interface
    }

    @Override
    public boolean isEnabled() {
        return true; // Override methods from UserDetail interface
    }
}

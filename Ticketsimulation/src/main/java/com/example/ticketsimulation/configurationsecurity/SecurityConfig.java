package com.example.ticketsimulation.configurationsecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserDetailsService userDetailsService; // Service for retrieving user details from the database

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Configures a password encoder bean that uses BCrypt hashing for securing passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configures an authentication manager with custom userDetailsService and password encoder
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class); // Access shared AuthenticationManagerBuilder
        authenticationManagerBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder()); // Set user details service and encoder
        return authenticationManagerBuilder.build(); // Build and return the AuthenticationManager
    }

    // Defines the security filter chain with custom HTTP security settings
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Disables CSRF protection customize as needed for production
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/vendor/**").hasRole("VENDOR") // Restricts access to vendor routes to VENDOR role
                        .requestMatchers("/customer/**").hasRole("CUSTOMER") // Restricts access to customer routes to CUSTOMER role
                        .anyRequest().permitAll()) // Allows unrestricted access to all other routes
                .httpBasic(withDefaults()); // Configures HTTP Basic Authentication

        return http.build(); // Builds and returns the configured SecurityFilterChain
    }


}


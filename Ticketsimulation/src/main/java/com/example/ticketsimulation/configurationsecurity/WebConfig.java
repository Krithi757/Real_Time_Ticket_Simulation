package com.example.ticketsimulation.configurationsecurity;
import com.example.ticketsimulation.view.CspFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // Marks this class as a configuration class for Spring
@EnableWebMvc // Enables Spring MVC, necessary for configuring web-related settings
public class WebConfig implements WebMvcConfigurer {

    // This method adds CORS (Cross-Origin Resource Sharing) mappings for the application
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    // Bean definition to register the custom Content Security Policy (CSP) filter
    @Bean
    public FilterRegistrationBean<CspFilter> cspFilter() {
        FilterRegistrationBean<CspFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CspFilter());
        registrationBean.addUrlPatterns("/*"); // Apply the filter to all URLs in the application
        return registrationBean;
    }
}

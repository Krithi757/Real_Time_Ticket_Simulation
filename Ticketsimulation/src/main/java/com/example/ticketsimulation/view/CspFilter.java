package com.example.ticketsimulation.view;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
// Content Security Policy filter that adds a Content-Security-Policy header to HTTP responses
public class CspFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)throws IOException, ServletException {
        // Set the Content-Security-Policy header with specific rules for resources
        ((HttpServletResponse) response).setHeader("Content-Security-Policy", "default-src 'self'; connect-src 'self' ws://localhost:8080");
        chain.doFilter(request, response);
    }

    // Method for initialization (no specific initialization needed for this filter)
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    // Method to clean up any resources (no specific cleanup needed here)
    @Override
    public void destroy() {
    }
}

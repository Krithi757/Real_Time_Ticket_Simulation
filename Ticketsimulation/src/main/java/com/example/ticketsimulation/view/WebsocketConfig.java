package com.example.ticketsimulation.view;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

//Marking as a configuration class for websocket configuration and message broker setup
@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    // Configuring the message broker to handle messages for subscribers
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple broker to handle messages sent to "/topic" destination
        config.enableSimpleBroker("/topic");
        // Set the application destination prefix (used for messages sent by the server to the client)
        config.setApplicationDestinationPrefixes("/app");
    }
    // Register the STOMP endpoint that clients will use to connect
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Define the WebSocket endpoint "/ticket-updates" and allow connections from localhost
        registry.addEndpoint("/ticket-updates")
                .setAllowedOrigins("http://localhost:4200");
    }
}

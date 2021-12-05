package com.digvijayb.app.websocketprj;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@SpringBootApplication
public class WebsocketprjApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketprjApplication.class, args);
    }

    record GreetingRequest(String name) {
    }

    record GreetingResponse(String message) {
    }


    record AppMessage(String message){}
    record AppMessageResponse(String message){}

    @Controller
    class GreetingControllerAdvice {

        @MessageExceptionHandler
        @SendTo("/topic/errors")
        public String handleException(IllegalArgumentException e) {
            var message = ("an exception occurred! " + NestedExceptionUtils.getMostSpecificCause(e));
            System.out.println(message);
            return message;
        }

        @MessageMapping("/chat")
        @SendTo("/topic/greetings")
        GreetingResponse greet(GreetingRequest request) throws Exception {
            log.info("request : {}", request);
            Assert.isTrue(Character.isUpperCase(request.name().charAt(0)), () -> "the name must start with a capital letter!");
            Thread.sleep(1_000);
            return new GreetingResponse("Hello, " + request.name());
        }


        @MessageMapping("/message")
        @SendTo("/topic/message")
        AppMessageResponse message(AppMessage request) throws Exception {
            log.info("request : {}", request);
//            Assert.isTrue(Character.isUpperCase(request.name().charAt(0)), () -> "the name must start with a capital letter!");
            Thread.sleep(1_000);
            return new AppMessageResponse(LocalDateTime.now() + " => " + request.message);
        }





    }

    @Configuration
    @EnableWebSocketMessageBroker
    class GreetingWebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

        @Override
        public void configureMessageBroker(MessageBrokerRegistry registry) {
            registry.enableSimpleBroker("/topic");
            registry.setApplicationDestinationPrefixes("/app");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/chat").withSockJS();
        }
    }
}

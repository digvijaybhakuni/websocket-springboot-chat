package com.digvijayb.app.websocketprj;

import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    @RequiredArgsConstructor
    class GreetingControllerAdvice {

        private final ChatService chatService;

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
//            Thread.sleep(1_000);
            chatService.addMember(request.name());
            return new GreetingResponse("Hello, " + request.name());
        }


        @MessageMapping("/message")
        @SendTo("/topic/message")
        AppMessageResponse message(AppMessage request) throws Exception {
            log.info("request : {}", request);
//            Assert.isTrue(Character.isUpperCase(request.name().charAt(0)), () -> "the name must start with a capital letter!");
//            Thread.sleep(1_000);
            String message = LocalDateTime.now() + " - " + request.message;
            chatService.saveMessage(message);
            return new AppMessageResponse(message);
        }


        @ResponseBody
        @RequestMapping(value = "/api/chat/members", method = RequestMethod.GET)
        List<String> getAllMembers() {
            return chatService.getAllMembers();
        }

        @ResponseBody
        @RequestMapping(value = "/api/chat/messages", method = RequestMethod.GET)
        List<String> getAllMessages() {
            return chatService.getAllMessages();
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

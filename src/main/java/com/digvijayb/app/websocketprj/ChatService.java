package com.digvijayb.app.websocketprj;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class ChatService {


    private final List<String> allMessages = new LinkedList<>();
    private final List<String> allMembers = new LinkedList<>();

    public final List<String> getAllMessages() {
        return allMessages;
    }

    public final List<String> getAllMembers() {
        return allMembers;
    }

    public final String addMember(String member){
        allMembers.add(member);
        return member;
    }

    public final String saveMessage(String message) {
        allMessages.add(message);
        return message;
    }

}

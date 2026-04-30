package com.arena.catalog.service.impl;

import com.arena.catalog.service.MatchAiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class MatchAiServiceImpl implements MatchAiService {

    private final ChatClient chatClient;

    public MatchAiServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }


    @Override
    @Cacheable(value = "ai_trivia", key = "#opponentName")
    public String getFunFacts(String opponentName) {
        // Fixăm România ca gazdă în prompt
        String prompt = String.format(
                "Ești un asistent virtual pentru Arena Sport. " +
                        "Oferă-mi 3 informații scurte, captivante despre istoricul meciurilor de fotbal dintre " +
                        "echipa națională a României și %s. Te poți referi la meciuri celebre sau jucători legendari. " +
                        "Folosește un ton energic și răspunde în limba română.",
                opponentName
        );

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            System.err.println("ERROARE AI: " + e.getMessage());
            e.printStackTrace();
            return "Momentan nu am putut prelua curiozități pentru meciul cu " + opponentName + ".";
        }
    }
}
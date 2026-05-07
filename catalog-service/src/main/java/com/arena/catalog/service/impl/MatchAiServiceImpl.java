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
        String prompt = String.format(
                "Ești un istoric sportiv concis. Oferă exact 3 curiozități scurte despre meciurile dintre România și %s. " +
                        "Reguli stricte: " +
                        "1. Nu folosi introduceri (ex: 'Salutare fan...') sau încheieri. " +
                        "2. Fiecare informație trebuie să aibă maximum 15 cuvinte. " +
                        "3. Folosește un format de listă cu bulinuțe. " +
                        "4. Tonul să fie informativ, nu excesiv de entuziasmat. " +
                        "5. Răspunde în limba română.",
                opponentName
        );

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            return "Informații indisponibile momentan pentru meciul cu " + opponentName + ".";
        }
    }
}
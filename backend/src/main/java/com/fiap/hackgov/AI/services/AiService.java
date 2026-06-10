package com.fiap.hackgov.AI.services;

import com.fiap.hackgov.AI.dtos.RequestAI;
import com.fiap.hackgov.AI.dtos.ResponseAI;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class AiService {

    @Value("classpath:website-context.txt")
    private Resource contextoResource;

    public ResponseAI generateResponse(RequestAI request) {
        try (Client client = new Client()) {

            String contextText = new String(
                    contextoResource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            Content systemInstruction = Content.fromParts(Part.fromText(contextText));

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-3-flash-preview",
                    request.message(),
                    config
            );

            return new ResponseAI(response.text());

        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Erro ao carregar contexto do assistente", e);
        }
    }
}
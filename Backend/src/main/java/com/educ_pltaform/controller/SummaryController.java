package com.educ_pltaform.controller;

import com.educ_pltaform.entity.Summary;
import com.educ_pltaform.repository.SummaryRepository;
import com.educ_pltaform.service.LLMService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/summaries")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class SummaryController {

    private static final String SUMMARY_PROMPT =
            "You are an expert in creating educational summaries. " +
                    "Analyze the following text and generate a summary:\\n%s\\n\\n" +
                    "The summary must:\\n" +
                    "1. Capture essential points\\n" +
                    "2. Be clear and concise\\n" +
                    "3. Be logically organized\\n" +
                    "4. Highlight key concepts\\n" +
                    "5. Be written in French";

    @Autowired
    private SummaryRepository summaryRepository;

    @Autowired
    private LLMService llmService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateSummary(@RequestBody Summary summaryRequest) {
        try {
            String content = summaryRequest.getContent();
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Le contenu ne peut pas être vide");
            }

            log.info("Génération de résumé pour le contenu: {}", content);

            String generatedContent = llmService.generateContent(
                    String.format(SUMMARY_PROMPT, content),
                    "summary_generator"
            );

            Summary summary = new Summary();
            summary.setContent(generatedContent);
            summary.setTitle("Résumé - " + content.substring(0, Math.min(content.length(), 50)).trim());
            summary.setCreatedAt(new Date());

            return ResponseEntity.ok(summaryRepository.save(summary));

        } catch (Exception e) {
            log.error("Erreur lors de la génération du résumé: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur de génération: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Summary>> getAllSummaries() {
        return ResponseEntity.ok(summaryRepository.findAll());
    }
}
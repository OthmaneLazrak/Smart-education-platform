package com.educ_pltaform.controller;

import com.educ_pltaform.entity.Quiz;
import com.educ_pltaform.repository.QuizRepository;
import com.educ_pltaform.service.LLMService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class QuizController {

    private static final String QUIZ_PROMPT =
            "Create 5 multiple choice questions about this text:\\n\" +\n" +
                    "    \"%s\\n\\n\" +\n" +
                    "    \"Follow this format EXACTLY:\\n\" +\n" +
                    "    \"Question 1: [question about the text content]\\n\" +\n" +
                    "    \"a) [option]\\n\" +\n" +
                    "    \"b) [option]\\n\" +\n" +
                    "    \"c) [option]\\n\" +\n" +
                    "    \"d) [option]\\n\" +\n" +
                    "    \"Answer: [letter]\\n\\n\" +\n" +
                    "    \"Rules:\\n\" +\n" +
                    "    \"- Questions must be about the main concepts in the text\\n\" +\n" +
                    "    \"- Each question needs exactly one correct answer\\n\" +\n" +
                    "    \"- Make questions clear and specific\\n\" +\n" +
                    "    \"- Return only the formatted questions\\n\" +\n" +
                    "    \"- Generate all questions and answers in French";

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private LLMService llmService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateQuiz(@RequestBody Quiz quizRequest) {
        try {
            String content = quizRequest.getContent();
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Le contenu ne peut pas être vide");
            }

            log.info("Génération de quiz pour le contenu: {}", content);

            String generatedContent = llmService.generateContent(
                    String.format(QUIZ_PROMPT, content),
                    "quiz_generator"
            );

            Quiz quiz = new Quiz();
            quiz.setContent(generatedContent);
            quiz.setTitle("Quiz - " + content.substring(0, Math.min(content.length(), 50)).trim());
            quiz.setCreatedAt(new Date());

            return ResponseEntity.ok(quizRepository.save(quiz));

        } catch (Exception e) {
            log.error("Erreur lors de la génération du quiz: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur de génération: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        return ResponseEntity.ok(quizRepository.findAll());
    }
}
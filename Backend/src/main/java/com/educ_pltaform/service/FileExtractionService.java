package com.educ_pltaform.service;

import com.educ_pltaform.entity.UploadedFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class FileExtractionService {

    @Value("${python.microservice.url}")
    private String pythonMicroserviceUrl;

    private final RestTemplate restTemplate;

    public FileExtractionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String sendFileToPythonService(UploadedFile uploadedFile) {
        try {
            // Vérification du chemin du fichier
            if (uploadedFile.getPath() == null) {
                throw new IllegalArgumentException("Le chemin du fichier est null");
            }

            File file = new File(uploadedFile.getPath());
            if (!file.exists()) {
                log.error("Fichier non trouvé : {}", uploadedFile.getPath());
                throw new IllegalArgumentException("Fichier non trouvé : " + uploadedFile.getPath());
            }

            log.info("Préparation de l'envoi du fichier : {} (taille: {} bytes)", file.getName(), file.length());
            FileSystemResource fileResource = new FileSystemResource(file);

            // Configuration de la requête
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("Envoi de la requête vers : {}", pythonMicroserviceUrl);
            ResponseEntity<String> response = restTemplate.exchange(
                    pythonMicroserviceUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Erreur du service Python : {}", response.getStatusCode());
                throw new RuntimeException("Le service Python a répondu avec le code : " + response.getStatusCode());
            }

            log.info("Fichier traité avec succès");
            return response.getBody();

        } catch (Exception e) {
            log.error("Erreur lors du traitement du fichier : {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi du fichier au service Python: " + e.getMessage());
        }
    }

    public void testSendLocalFileToPythonService(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("Le fichier de test n'existe pas : " + filePath);
            }

            log.info("Test d'envoi du fichier : {}", filePath);

            UploadedFile file = new UploadedFile();
            file.setName(path.getFileName().toString());
            file.setPath(path.toAbsolutePath().toString());
            file.setType(Files.probeContentType(path));

            String response = sendFileToPythonService(file);
            log.info("Réponse du service Python : {}", response);

        } catch (Exception e) {
            log.error("Erreur lors du test d'envoi : {}", e.getMessage());
            throw new RuntimeException("Erreur lors du test d'envoi : " + e.getMessage());
        }
    }
}
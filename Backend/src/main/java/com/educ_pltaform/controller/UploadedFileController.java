package com.educ_pltaform.controller;

import com.educ_pltaform.entity.UploadedFile;
import com.educ_pltaform.repository.UploadedFileRepository;
import com.educ_pltaform.service.FileExtractionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RestController
@RequestMapping("/api/uploaded-files")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class UploadedFileController {

    @Value("${file.upload.directory:uploads}")
    private String uploadDirectory;

    private final UploadedFileRepository uploadedFileRepository;
    private final FileExtractionService fileExtractionService;

    public UploadedFileController(UploadedFileRepository uploadedFileRepository,
                                  FileExtractionService fileExtractionService) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.fileExtractionService = fileExtractionService;
    }

    @GetMapping
    public ResponseEntity<List<UploadedFile>> getAllUploadedFiles() {
        return ResponseEntity.ok(uploadedFileRepository.findAll());
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Fichier vide");
            }

            Path filePath = saveFileToDirectory(file, "uploaded");

            UploadedFile uploadedFile = new UploadedFile();
            uploadedFile.setName(file.getOriginalFilename());
            uploadedFile.setType(file.getContentType());
            uploadedFile.setPath(filePath.toAbsolutePath().toString());

            UploadedFile savedFile = uploadedFileRepository.save(uploadedFile);
            log.info("Fichier sauvegardé : {}", savedFile.getPath());

            return ResponseEntity.ok(savedFile);

        } catch (Exception e) {
            log.error("Erreur lors de l'upload", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de l'upload : " + e.getMessage());
        }
    }

    @PostMapping("/extract")
    public ResponseEntity<?> uploadAndExtractFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Fichier vide");
            }

            // Sauvegarder le fichier
            Path filePath = saveFileToDirectory(file, "toextract");

            // Créer et sauvegarder l'entité
            UploadedFile uploadedFile = new UploadedFile();
            uploadedFile.setName(file.getOriginalFilename());
            uploadedFile.setType(file.getContentType());
            uploadedFile.setPath(filePath.toAbsolutePath().toString());

            UploadedFile savedFile = uploadedFileRepository.save(uploadedFile);
            log.info("Fichier sauvegardé pour extraction : {}", savedFile.getPath());

            // Extraire le texte
            String extractedText = fileExtractionService.sendFileToPythonService(savedFile);

            // Sauvegarder le texte extrait
            Path extractedFilePath = saveExtractedText(savedFile.getName(), extractedText);
            log.info("Texte extrait sauvegardé : {}", extractedFilePath);

            return ResponseEntity.ok(extractedText);

        } catch (Exception e) {
            log.error("Erreur lors de l'extraction", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de l'extraction : " + e.getMessage());
        }
    }

    private Path saveFileToDirectory(MultipartFile file, String subDirectory) throws IOException {
        Path directory = Paths.get(uploadDirectory, subDirectory);
        Files.createDirectories(directory);

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = directory.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Fichier sauvegardé dans : {}", filePath);

        return filePath;
    }

    private Path saveExtractedText(String originalFileName, String extractedText) throws IOException {
        Path extractedDir = Paths.get(uploadDirectory, "extracted");
        Files.createDirectories(extractedDir);

        String fileName = System.currentTimeMillis() + "_" +
                originalFileName.replaceAll("\\s+", "_") + "_extracted.txt";
        Path filePath = extractedDir.resolve(fileName);

        Files.write(filePath, extractedText.getBytes());
        return filePath;
    }
}
package com.educ_pltaform.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Choice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId; // Référence à l'utilisateur
    private Long uploadedFileId; // Référence au fichier source
    private String choiceType; // "QUIZ", "SUMMARY", "PROBLEM"
}*/
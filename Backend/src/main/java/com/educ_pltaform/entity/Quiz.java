package com.educ_pltaform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private Long uploadedFileId;
    @Column(columnDefinition = "TEXT")
    private String content;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
}

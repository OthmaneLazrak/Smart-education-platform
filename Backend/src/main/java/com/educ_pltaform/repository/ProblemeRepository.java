package com.educ_pltaform.repository;

import com.educ_pltaform.entity.Probleme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ProblemeRepository extends JpaRepository<Probleme, Long> {
    // Custom query methods can be defined here if needed
    // For example, to find problems by user or other criteria
}

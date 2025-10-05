package com.fpt.careermate.repository;

import com.fpt.careermate.domain.WorkModel;
import com.fpt.careermate.domain.WorkModelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkModelRepo extends JpaRepository<WorkModel, WorkModelId> {
}

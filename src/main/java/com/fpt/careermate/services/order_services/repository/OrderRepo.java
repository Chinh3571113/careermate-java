package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.CandidateOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface OrderRepo extends JpaRepository<CandidateOrder,Integer> {
    List<CandidateOrder> findByCandidate_CandidateId(int candidateId);
    Optional<CandidateOrder> findByCandidate_CandidateIdAndIsActiveTrue(int candidateId);
}

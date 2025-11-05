package com.fpt.careermate.services.account_services.repository;

import com.fpt.careermate.services.account_services.domain.Account;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepo extends JpaRepository<Account,Integer> {
    boolean existsByEmail(String email);

    Optional<Account> findByEmail(String email);

    @Transactional
    @Modifying
    @Query("update account a set a.password = ?2 where a.email = ?1")
    void updatePassword(String email, String newPassword);

    @Query("SELECT DISTINCT a FROM account a " +
            "LEFT JOIN a.roles r " +
            "WHERE (:#{#roles == null || #roles.isEmpty()} = true OR r.name IN :roles) " +
            "AND (:#{#statuses == null || #statuses.isEmpty()} = true OR a.status IN :statuses) " +
            "AND (:#{#keyword == null || #keyword.isEmpty()} = true OR " +
            "LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Account> searchAccounts(
            @Param("roles") List<String> roles,
            @Param("statuses") List<String> statuses,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT a FROM account a JOIN a.roles r WHERE r.name = 'ADMIN' AND a.status = 'ACTIVE'")
    List<Account> findAdminAccounts();
}

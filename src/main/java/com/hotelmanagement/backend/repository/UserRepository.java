package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface UserRepository extends JpaRepository<User,String> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.role.name <> 'USER'
            """)
    Page<User> findEmployees(Pageable pageable);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.role.name = :role
              AND u.role.name <> 'USER'
            """)
    Page<User> findEmployeesByRole(
            @Param("role") String role,
            Pageable pageable
    );

    List<User> findByRole_NameInAndActiveTrue(
            List<String> roles
    );

    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.role.name = :role
            AND u.createdAt BETWEEN :start AND :end
    """)
    long countNewCustomers(
            @Param("role") String role,
            @Param("start") Date start,
            @Param("end") Date end
    );
}

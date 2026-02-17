package com.gotrip.backend.repository;

import com.gotrip.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Basic CRUD (Save, Delete, FindByID) is already included!
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

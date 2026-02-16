package com.gotrip.backend.repository;

import com.gotrip.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Basic CRUD (Save, Delete, FindByID) is already included!
}

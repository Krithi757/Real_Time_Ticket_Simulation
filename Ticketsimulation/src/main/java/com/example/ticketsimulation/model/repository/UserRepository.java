package com.example.ticketsimulation.model.repository;
import com.example.ticketsimulation.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
//Repository interface for User entities, providing database interaction methods
// Extends JpaRepository to inherit advanced CRUD operations and JPA functionalities
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //Finds a User entity based on the given username
    Optional<User> findByUsername(String username);

}

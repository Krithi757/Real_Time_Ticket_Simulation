package com.example.ticketsimulation.model.repository;
import com.example.ticketsimulation.model.entity.Ticket;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

//Repository interface for Ticket entities, providing database interaction methods
@Repository
public interface TicketRepository extends CrudRepository<Ticket, Integer> { //Extends CrudRepository to inherit basic CRUD operations
    //Retrieves a list of available tickets, where `isAvailable` is set to true
    List<Ticket> findByIsAvailableTrue();

    //Counts the number of tickets that are currently available
    long countByIsAvailableTrue();

}








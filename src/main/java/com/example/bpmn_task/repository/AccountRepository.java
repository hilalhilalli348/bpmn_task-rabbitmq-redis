package com.example.bpmn_task.repository;

import com.example.bpmn_task.entity.Account;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = "cardList"
    )
    Optional<Account> findByAccountNumber(String accountNumber);

}

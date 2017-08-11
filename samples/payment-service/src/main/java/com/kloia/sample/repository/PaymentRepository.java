package com.kloia.sample.repository;


import com.kloia.sample.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String>, QueryDslPredicateExecutor {

}

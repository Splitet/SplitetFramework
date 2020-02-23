package com.kloia.sample.repository;


import com.kloia.eventapis.view.SnapshotRepository;
import com.kloia.sample.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String>, QuerydslPredicateExecutor<Payment>, SnapshotRepository<Payment, String> {

}

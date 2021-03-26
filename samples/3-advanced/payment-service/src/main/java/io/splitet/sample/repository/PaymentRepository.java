package io.splitet.sample.repository;


import io.splitet.core.view.SnapshotRepository;
import io.splitet.sample.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String>, QuerydslPredicateExecutor<Payment>, SnapshotRepository<Payment, String> {

}

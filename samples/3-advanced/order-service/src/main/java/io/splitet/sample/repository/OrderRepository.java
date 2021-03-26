package io.splitet.sample.repository;


import io.splitet.core.view.SnapshotRepository;
import io.splitet.sample.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Orders, String>, QuerydslPredicateExecutor<Orders>, SnapshotRepository<Orders, String> {

}

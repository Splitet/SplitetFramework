package com.kloia.sample.repository;


import com.kloia.eventapis.view.SnapshotRepository;
import com.kloia.sample.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Orders, String>, QueryDslPredicateExecutor, SnapshotRepository<Orders> {

}

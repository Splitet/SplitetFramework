package com.kloia.sample.repository;


import com.kloia.sample.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String>, QueryDslPredicateExecutor {

}

package com.kloia.sample.repository;

import com.kloia.eventapis.view.SnapshotRepository;
import com.kloia.sample.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, String>, QuerydslPredicateExecutor<Stock>, SnapshotRepository<Stock, String> {

}

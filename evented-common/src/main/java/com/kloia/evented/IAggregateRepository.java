package com.kloia.evented;

import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 07/02/2017.
 */
/*
@Repository
public interface IAggregateRepository extends CrudRepository<AggregateEvent, IAggregate> {

    @Query("Select * from customer where firstname=?0")
    public IAggregate getAggreagetById(UUID id);

    @Query("Select * from customer where lastname=?0")
    public List<IAggregate> findByLastName(String lastName);

}*/

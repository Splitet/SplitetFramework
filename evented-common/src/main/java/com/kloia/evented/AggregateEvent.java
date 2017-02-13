package com.kloia.evented;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.util.UUID;

/**
 * Created by zeldalozdemir on 07/02/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "AggregateEvent")
public class AggregateEvent {

    @PrimaryKey
    private AggregateKey aggregateKey;

    @Column(value = "status")
    private String status;

    @Column(value = "description")
    private String description;


}

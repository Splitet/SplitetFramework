package com.kloia.evented;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.util.Date;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 07/02/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table
public class EntityEvent {

    @PrimaryKey
    private EventKey eventKey;

    @Column(value = "opId")
    private UUID opId;

    @Column(value= "opDate")
    private Date opDate;

    @Column(value = "aggregateName")
    private String aggregateName;

    @Column(value = "status")
    private String status;

    @Column(value = "eventData")
    private String eventData;


}

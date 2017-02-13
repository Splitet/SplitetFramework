package com.kloia.evented;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 13/02/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@PrimaryKeyClass
public class AggregateKey implements Serializable {

    @PrimaryKeyColumn(name = "entityId", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private long entityId;

    @PrimaryKeyColumn(name= "opDate",ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Date opDate;

    @PrimaryKeyColumn(name = "opId", ordinal = 2,type = PrimaryKeyType.CLUSTERED)
    private UUID opId;

    @PrimaryKeyColumn(name = "aggregateName", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    private String aggregateName;

}

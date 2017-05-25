package com.kloia.evented;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 13/02/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@PrimaryKeyClass
@Builder
public class EventKey implements Serializable {

    @PrimaryKeyColumn(name = "entityId", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String entityId;

    @PrimaryKeyColumn(name= "version",ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private long version;

}

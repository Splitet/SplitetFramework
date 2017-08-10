package com.kloia.evented;

import com.datastax.driver.core.PoolingOptions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventStoreConfig {
    private String contactPoints;
    private PoolingOptions poolingOptions = new PoolingOptions(); // default options
    private String keyspaceName;

}

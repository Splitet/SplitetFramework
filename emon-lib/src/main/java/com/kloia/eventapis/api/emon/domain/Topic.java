package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Topic implements Serializable {

    private static final long serialVersionUID = -8668554375371818043L;

    private Map<String, ServiceData> serviceDataHashMap = new HashMap<>();

    private List<Partition> partitions = new ArrayList<>();

    public Map<String, ServiceData> getServiceDataHashMap() {
        try {
            serviceDataHashMap.forEach((s, serviceData) -> serviceData.getPartition()
                    .forEach(partition -> getPartition(partition.getNumber()).ifPresent(partition1 -> partition.calculateLag(partition1.getOffset()))));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return serviceDataHashMap;
    }

    private Optional<Partition> getPartition(int number){
        return partitions.stream().filter(partition -> partition.getNumber() == number).findFirst();
    }
}

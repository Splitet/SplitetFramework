package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceData implements Serializable {
    private static final long serialVersionUID = 2401532791975588L;

    private String serviceName;
    private Map<Integer, Partition> partitions = new HashMap<>();

    public ServiceData(String serviceName) {
        this.serviceName = serviceName;
    }

    public static ServiceData createServiceData(String consumer, List<Partition> value) {
        return new ServiceData(consumer, value.stream().collect(Collectors.toMap(Partition::getNumber, Function.identity())));
    }

    public Partition getPartition(int number) {
        if (partitions != null && partitions.size() > number)
            return partitions.get(number);
        else
            return null;
    }

    public Partition setPartition(Partition partition) {
        return partitions.put(partition.getNumber(), partition);
    }

    public void setPartitions(List<Partition> value) {
        partitions = value.stream().collect(Collectors.toMap(Partition::getNumber, Function.identity()));
    }
}

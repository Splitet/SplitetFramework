package io.splitet.core.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Topic implements Serializable {

    private static final long serialVersionUID = -8668554375371818043L;

    private Map<String, ServiceData> serviceDataHashMap = new HashMap<>();

    private Map<Integer, Partition> partitions = new HashMap<>();

    public static Topic createTopic(String serviceName, int partitionNo, Long offset) {
        ServiceData serviceData = new ServiceData(serviceName, new HashMap<>());
        serviceData.setPartition(new Partition(partitionNo, offset));

        HashMap<String, ServiceData> serviceDataHashMap = new HashMap<>();
        serviceDataHashMap.put(serviceName, serviceData);
        return new Topic(serviceDataHashMap, new HashMap<>());
    }

    public Map<String, ServiceData> getServiceDataHashMap() {
        try {
            serviceDataHashMap.forEach((s, serviceData) -> serviceData.getPartitions().values()
                    .forEach(partition -> getPartition(partition.getNumber()).ifPresent(
                            partition1 -> partition.calculateLag(partition1.getOffset()))
                    )
            );
        } catch (Exception ex) {
            log.warn(ex.getMessage());
        }
        return serviceDataHashMap;
    }

    private Optional<Partition> getPartition(int number) {
        return Optional.ofNullable(partitions.get(number));
    }

    public void setPartitions(List<Partition> value) {
        partitions = value.stream().collect(Collectors.toMap(Partition::getNumber, Function.identity()));
    }
}

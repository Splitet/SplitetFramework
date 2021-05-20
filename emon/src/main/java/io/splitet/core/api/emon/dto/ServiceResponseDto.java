package io.splitet.core.api.emon.dto;

import io.splitet.core.api.emon.domain.Partition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceResponseDto {

    private Map<String, Map<Integer, Partition>> topicPartitions = new HashMap<>();
    private Set<String> commands = new HashSet<>();

    public void addTopicPartitions(String topic, Map<Integer, Partition> partitionMap) {
        topicPartitions.put(topic, partitionMap);
    }

    public void addCommand(String value) {
        commands.add(value);
    }
}

package io.splitet.core.api.emon.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.splitet.core.api.emon.domain.Partition;
import io.splitet.core.api.emon.domain.ServiceData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicResponseDto {

    @JsonIgnore
    private Map<String, ServiceData> serviceDataHashMap = new HashMap<>();

    @JsonIgnore
    private Map<Integer, Partition> partitionsMap = new HashMap<>();

    public Map<String, Map<Integer, String>> getServiceData() {
        return getServiceDataHashMap().entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey,
                        o -> o.getValue().getPartitions().entrySet().stream().collect(
                                Collectors.toMap(Map.Entry::getKey, p -> {
                                    if (p.getValue().getLag() != null && p.getValue().getLag() > 0L) {
                                        return p.getValue().getOffset() + "(lag=" + p.getValue().getLag() + ")";
                                    } else {
                                        return p.getValue().getOffset().toString();
                                    }
                                }))
                )
        );

    }

    @JsonProperty("partitions")
    public Map<Integer, Long> getPartitions() {
        return partitionsMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, o -> o.getValue().getOffset()));
    }
}

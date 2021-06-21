package io.splitet.core.api.emon.controller;


import com.hazelcast.core.IMap;

import io.splitet.core.api.emon.domain.ServiceData;
import io.splitet.core.api.emon.domain.Topic;
import io.splitet.core.api.emon.dto.TopicResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by zeldalozdemir on 04/2020.
 */
@Slf4j
@RestController
@RequestMapping(
        value = EventController.ENDPOINT
)
public class EventController {

    static final String ENDPOINT = "/events";

    @Autowired
    private IMap<String, Topic> topicsMap;


    @GetMapping
    public ResponseEntity<Map<String, TopicResponseDto>> getEvents() {
        try {
            Map<String, TopicResponseDto> result = topicsMap.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey, o -> new TopicResponseDto(o.getValue().getServiceDataHashMap(), o.getValue().getPartitions())
            ));
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping(value = "lag")
    public ResponseEntity<Map<String, TopicResponseDto>> getLaggedTopics() {
        try {
            Map<String, TopicResponseDto> result = topicsMap.entrySet().stream()
                    .map(entry -> new AbstractMap.SimpleEntry<>(
                                    entry.getKey(),
                                    new TopicResponseDto(
                                            entry.getValue()
                                                    .getServiceDataHashMap()
                                                    .entrySet().stream()
                                                    .map(serviceDataEntry -> new AbstractMap.SimpleEntry<>(
                                                            serviceDataEntry.getKey(),
                                                            new ServiceData(
                                                                    serviceDataEntry.getKey(),
                                                                    serviceDataEntry.getValue().getPartitions().values()
                                                                            .stream().filter(partition -> partition.getLag() != null && partition.getLag() > 0L)
                                                                            .collect(Collectors.toList())
                                                            ))

                                                    )
                                                    .filter(serviceData -> serviceData.getValue().getPartitions().size() > 0)
                                                    .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)),
                                            entry.getValue().getPartitions()
                                    )
                            )
                    )
                    .filter(topicsDtpEntry -> topicsDtpEntry.getValue().getServiceDataHashMap().size() > 0L)
                    .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }


    @GetMapping(value = "{topic}")
    public ResponseEntity<TopicResponseDto> getTopic(@PathVariable("topic") String topic) {
        try {
            Topic found = topicsMap.get(topic);
            TopicResponseDto result = new TopicResponseDto(found.getServiceDataHashMap(), found.getPartitions());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
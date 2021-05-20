package io.splitet.core.api.emon.controller;


import com.hazelcast.core.IMap;
import io.splitet.core.api.emon.domain.ServiceData;
import io.splitet.core.api.emon.domain.Topic;
import io.splitet.core.api.emon.dto.ServiceResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by zeldalozdemir on 04/2020.
 */
@Slf4j
@RestController
@RequestMapping(
        value = ServiceController.ENDPOINT
)
public class ServiceController {

    static final String ENDPOINT = "/services";

    @Autowired
    private IMap<String, Topic> topicsMap;

    @Autowired
    private IMap<String, String> commandsMap;


    @GetMapping
    public ResponseEntity<Map<String, ServiceResponseDto>> getServices() {
        try {
            Map<String, ServiceResponseDto> responseDtoHashMap = new HashMap<>();
            Set<Map.Entry<String, Topic>> entries = topicsMap.entrySet();
            entries
                    .forEach(topicEntry -> {
                        for (Map.Entry<String, ServiceData> serviceDataEntry : topicEntry.getValue().getServiceDataHashMap().entrySet()) {
                            if (!responseDtoHashMap.containsKey(serviceDataEntry.getKey())) {
                                responseDtoHashMap.put(serviceDataEntry.getKey(), new ServiceResponseDto());
                            }
                            responseDtoHashMap.get(serviceDataEntry.getKey()).addTopicPartitions(topicEntry.getKey(), serviceDataEntry.getValue().getPartitions());
                        }
                    });
            commandsMap.entrySet().forEach(entry -> {
                responseDtoHashMap.get(entry.getValue()).addCommand(entry.getKey());
            });
            return new ResponseEntity<>(responseDtoHashMap, HttpStatus.OK);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }


    @GetMapping(value = "{serviceName}")
    public ResponseEntity<ServiceResponseDto> getService(@PathVariable("serviceName") String serviceName) {
        try {
            ServiceResponseDto serviceResponseDto = new ServiceResponseDto();
            Set<Map.Entry<String, Topic>> entries = topicsMap.entrySet();
            entries
                    .forEach(topicEntry -> {
                        for (Map.Entry<String, ServiceData> serviceDataEntry : topicEntry.getValue().getServiceDataHashMap().entrySet()) {
                            if (Objects.equals(serviceName, serviceDataEntry.getKey())) {
                                serviceResponseDto.addTopicPartitions(topicEntry.getKey(), serviceDataEntry.getValue().getPartitions());
                            }
                        }
                    });

            commandsMap.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), serviceName))
                    .map(Map.Entry::getKey)
                    .forEach(serviceResponseDto::addCommand);
            return new ResponseEntity<>(serviceResponseDto, HttpStatus.OK);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
package com.kloia.eventapis.api.emon.controller;

import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.kloia.eventapis.api.emon.domain.Topic;
import com.kloia.eventapis.api.emon.domain.Topology;
import com.kloia.eventapis.api.emon.dto.ResponseDto;
import com.kloia.eventapis.common.OperationContext;
import com.kloia.eventapis.exception.EventStoreException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by zeldalozdemir on 22/01/2017.
 */
@Slf4j
@RestController
@RequestMapping(value = "/operations")
public class EventController {

    @Autowired
    private IMap<String, Topology> operationsMap;
    @Autowired
    private IMap<String, Topology> operationsHistoryMap;
    @Autowired
    private IMap<String, Topic> topicsMap;

    @RequestMapping(value = "/{opId}", method = RequestMethod.GET)
    public ResponseEntity<?> getOperation(@PathVariable(OperationContext.OP_ID) String opId) throws IOException, EventStoreException {
        Topology topology = operationsMap.get(opId);
        if (topology == null) {
            topology = operationsHistoryMap.get(opId);
        }
        if (topology == null)
            return ResponseEntity.notFound().build();
        return new ResponseEntity<Object>(topology, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Collection<Topology>> getOperations(
            @PageableDefault Pageable pageable
    ) throws IOException, EventStoreException {
        try {
            Collection<Topology> values = operationsMap.values(
                    new PagingPredicate<>((Comparator<Map.Entry<String, Topology>> & Serializable) (o1, o2) -> -1 * Long.compare(o1.getValue().getStartTime(), o2.getValue().getStartTime()),
                            pageable.getPageSize()));
            return new ResponseEntity<>(values, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @RequestMapping(value = "/history", method = RequestMethod.GET)
    public ResponseEntity<ResponseDto> getTopicsHistory(@PageableDefault Pageable pageable) {
        try {
            ResponseDto responseDto = new ResponseDto();
            responseDto.setOperations(operationsHistoryMap.entrySet());
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }


}
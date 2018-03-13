package com.kloia.eventapis.api.emon.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class Topic extends HashMap<String, ServiceData> {

    private static final long serialVersionUID = -8668554375371818043L;

    private Long endOffSet = 0L;
    private List<Integer> partitions = Collections.singletonList(0);

    public Topic() {
    }

    public Topic(Map<? extends String, ? extends ServiceData> map) {
        super(map);
    }

    public Topic(Long endOffSet) {
        this.endOffSet = endOffSet;
    }

    public Topic(Map<? extends String, ? extends ServiceData> map, Long endOffSet) {
        super(map);
        this.endOffSet = endOffSet;
    }

    public void setPartitions(List<Integer> partitions) {
        this.partitions = partitions;
    }
}

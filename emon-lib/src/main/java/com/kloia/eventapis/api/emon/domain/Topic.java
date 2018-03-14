package com.kloia.eventapis.api.emon.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Data
public class Topic implements Serializable {

    private static final long serialVersionUID = -8668554375371818043L;

    private HashMap<String, ServiceData> serviceDataHashMap = new HashMap<>();

    private Long endOffSet = 0L;
    private List<Integer> partitions = Collections.singletonList(0);

    public Topic() {
    }

    public void setPartitions(List<Integer> partitions) {
        this.partitions = partitions;
    }
}

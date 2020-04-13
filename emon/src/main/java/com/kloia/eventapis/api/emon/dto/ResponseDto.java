package com.kloia.eventapis.api.emon.dto;

import com.kloia.eventapis.api.emon.domain.Topology;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class ResponseDto {
    private Set<Map.Entry<String, Topology>> operations;
}

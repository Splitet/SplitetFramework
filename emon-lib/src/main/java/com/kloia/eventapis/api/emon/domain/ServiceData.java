package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceData implements Serializable {
    private static final long serialVersionUID = 2401532791975588L;

    private String serviceName;
    private List<Partition> partition;

    public ServiceData(String serviceName) {
        this.serviceName = serviceName;
    }
}

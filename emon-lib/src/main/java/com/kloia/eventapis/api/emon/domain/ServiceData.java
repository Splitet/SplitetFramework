package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceData implements Serializable {

    private static final long serialVersionUID = 2401532791975588L;
    private String serviceName;
    private Long offset;
    private Long lag;

    public ServiceData(String serviceName, Long offset) {
        this.serviceName = serviceName;
        this.offset = offset;
    }

    public void calculateLag(long endOffset) {
        if (endOffset > offset)
            lag = endOffset - offset;
        else lag = null;
    }


}

package com.kloia.eventapis.api.emon.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpanningService implements Comparable<SpanningService>, Serializable {

    private static final long serialVersionUID = 7156177214906375549L;

    private String serviceName;
    private boolean isConsumed;

    @Override
    public int compareTo(@NotNull SpanningService spanningService) {
        return serviceName.compareTo(spanningService.getServiceName());
    }
}

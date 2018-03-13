package com.kloia.eventapis.api.emon.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceData implements Serializable {

    private static final long serialVersionUID = 2401532791975588L;

    private String serviceName;
    private Long offset;
}

package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class OperationEvents {

    private static final long serialVersionUID = -2419694872838243026L;

    @JsonProperty
    private TransactionState transactionState = TransactionState.RUNNING;

    @JsonProperty
    private boolean finished;

    @JsonProperty
    private List<SpanningService> spanningServices = new ArrayList<>();


    @JsonIgnore
    private Map<String, String> userContext;

    public OperationEvents(Map<String, String> userContext) {
        this.userContext = userContext;
    }
}

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
public class Partition implements Serializable {
    private static final long serialVersionUID = -4771113118830062688L;

    private int number;
    private Long offset = 0L;
    private Long lag;

    public Partition(int number) {
        this.number = number;
    }

    public Partition(int number, Long offset) {
        this.number = number;
        this.offset = offset;
    }

    public void calculateLag(long endOffset) {
        if (endOffset > offset)
            lag = endOffset - offset;
        else lag = null;
    }

}

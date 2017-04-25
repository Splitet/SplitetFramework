package com.kloia.eventapis.pojos;

import lombok.Data;

/**
 * Created by zeldalozdemir on 20/04/2017.
 */
@Data
public class CreateOperationEvent implements IOperationEvents {
    private String mainAggregateName;

    public CreateOperationEvent() {
    }

    public CreateOperationEvent(String mainAggregateName) {


        this.mainAggregateName = mainAggregateName;
    }
}

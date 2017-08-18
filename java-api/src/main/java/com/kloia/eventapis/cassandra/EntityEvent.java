package com.kloia.eventapis.cassandra;

import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.pojos.EventState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by zeldalozdemir on 07/02/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityEvent {

    private EventKey eventKey;

    private String opId;

    private Date opDate;

    private String eventType;

    private EventState status;

    private String auditInfo;

    private String eventData;


}

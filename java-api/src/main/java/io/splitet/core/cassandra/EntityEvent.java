package io.splitet.core.cassandra;

import io.splitet.core.common.EventKey;
import io.splitet.core.pojos.EventState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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

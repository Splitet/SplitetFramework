package com.kloia.eventapis.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 25/04/2017.
 */
@Data
public class PublishedEventWrapper implements Serializable {

    private static final long serialVersionUID = 7950670808405003425L;
    private ObjectNode event;

    private UUID opId;

    public PublishedEventWrapper() {
    }

    public PublishedEventWrapper(UUID opId, ObjectNode event) throws IOException {
        this.opId = opId;
        this.event = event;
    }



}

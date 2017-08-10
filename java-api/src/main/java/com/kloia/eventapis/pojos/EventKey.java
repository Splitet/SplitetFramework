package com.kloia.eventapis.pojos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by zeldalozdemir on 13/02/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventKey implements Serializable {

    private String entityId;

    private int version;

}

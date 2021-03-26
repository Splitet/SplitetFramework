package io.splitet.core.common;

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

    private static final long serialVersionUID = -5751030991267102373L;

    private String entityId;

    private int version;

}

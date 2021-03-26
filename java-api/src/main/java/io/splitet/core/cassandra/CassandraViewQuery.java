package io.splitet.core.cassandra;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.splitet.core.view.Entity;
import io.splitet.core.view.EntityFunctionSpec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created by zeldalozdemir on 12/02/2017.
 */
@Slf4j
public class CassandraViewQuery<E extends Entity> extends BaseCassandraViewQuery {

    public CassandraViewQuery(String tableName, CassandraSession cassandraSession, ObjectMapper objectMapper, List<EntityFunctionSpec<E, ?>> commandSpecs) {
        super(tableName, cassandraSession, objectMapper, commandSpecs);
    }

}

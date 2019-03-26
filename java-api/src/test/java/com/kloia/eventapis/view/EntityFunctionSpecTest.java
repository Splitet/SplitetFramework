package com.kloia.eventapis.view;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EntityFunctionSpecTest {

    private EntityFunctionSpec entityFunctionSpec;

    private class EntityType {
    }

    private class QueryType {
    }

    @Before
    public void setUp() throws Exception {
        entityFunctionSpec = new EntityFunctionSpec<EntityType, QueryType>((previous, event) -> null) {
            @Override
            public EntityFunction<EntityType, QueryType> getEntityFunction() {
                return super.getEntityFunction();
            }
        };
    }

    @Test
    public void shouldGetQueryType() throws Exception {
        Class queryType = entityFunctionSpec.getQueryType();
        assertThat(queryType, equalTo(QueryType.class));
    }

    @Test
    public void shouldGetEntityType() throws Exception {
        Class entityType = entityFunctionSpec.getEntityType();
        assertThat(entityType, equalTo(EntityType.class));
    }

}
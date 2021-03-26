package com.kloia.eventapis.api.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class EmptyUserContextTest {

    @InjectMocks
    private EmptyUserContext emptyUserContext;

    @Test
    public void shouldGetUserContext() throws Exception {
        // Given

        // When
        Map<String, String> actual = emptyUserContext.getUserContext();

        // Then
        assertThat(actual, nullValue());
    }

    @Test
    public void shouldExtractUserContext() throws Exception {
        // Given
        Map<String, String> userContext = (Map<String, String>) Mockito.mock(Map.class);

        // When
        emptyUserContext.extractUserContext(userContext);

        // Then
        verifyZeroInteractions(userContext);
    }

    @Test
    public void shouldClearUserContext() throws Exception {
        // Given

        // When
        emptyUserContext.clearUserContext();

        // Then

    }

    @Test
    public void shouldGetAuditInfo() throws Exception {
        // Given

        // When
        String actual = emptyUserContext.getAuditInfo();

        // Then
        assertThat(actual, nullValue());
    }
}
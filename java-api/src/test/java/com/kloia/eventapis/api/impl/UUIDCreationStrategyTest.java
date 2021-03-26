package com.kloia.eventapis.api.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UUIDCreationStrategyTest {

    @InjectMocks
    private UUIDCreationStrategy uUIDCreationStrategy;

    @Test
    public void shouldGetNextId() throws Exception {
        // Given

        // When
        String actual = uUIDCreationStrategy.nextId();

        // Then
        UUID uuid = UUID.fromString(actual);
        String stringValue = uuid.toString();
        assertThat(actual, equalTo(stringValue));
    }
}
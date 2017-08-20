package com.kloia.eventapis.common;

import com.kloia.eventapis.exception.EventContextException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(MockitoJUnitRunner.class)
public class OperationContextTest {

    @InjectMocks
    private OperationContext operationContext;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldSwitchContext() {
        operationContext.generateContext();
        operationContext.switchContext("newOpId");

        String actual = operationContext.getContext();

        assertThat(actual, equalTo("newOpId"));
    }

    @Test
    public void shouldGetContext() {
        String context = operationContext.getContext();

        for (int i = 0; i < 10; i++) {
            String contextAgain = operationContext.getContext();
            assertThat(contextAgain, equalTo(context));
        }
    }

    @Test
    public void shouldGetCommandContext() throws EventContextException {
        operationContext.generateContext();
        operationContext.setCommandContext("123");

        String actual = operationContext.getCommandContext();

        assertThat(actual, equalTo("123"));
    }

    @Test
    public void shouldSetCommandContext() throws EventContextException {
        operationContext.generateContext();
        operationContext.setCommandContext("123");

        String actual = operationContext.getCommandContext();

        assertThat(actual, equalTo("123"));
    }

    @Test
    public void shouldClearContext() throws EventContextException {
        expectedException.expect(EventContextException.class);

        operationContext.generateContext();
        operationContext.setCommandContext("123");
        operationContext.clearContext();

        operationContext.setCommandContext("456");
    }

    @Test
    public void shouldClearCommandContext() throws EventContextException {
        operationContext.generateContext();
        operationContext.setCommandContext("123");
        operationContext.clearCommandContext();

        String actual = operationContext.getCommandContext();

        assertNull(actual);
    }

    @Test
    public void shouldGenerateContext() {
        String context = operationContext.generateContext();

        String actual = operationContext.getContext();

        assertThat(actual, equalTo(context));
    }

}
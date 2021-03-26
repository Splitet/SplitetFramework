package com.kloia.eventapis.cassandra;

import com.kloia.eventapis.common.EventKey;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class DefaultConcurrencyResolverTest {

    @InjectMocks
    private DefaultConcurrencyResolver defaultConcurrencyResolver;

    @Test
    public void testTryMore() throws Exception {
        try {
            defaultConcurrencyResolver.tryMore();
        } catch (Exception e) {
            assertThat(e, Matchers.instanceOf(ConcurrentEventException.class));
            assertThat(e.getMessage(), Matchers.containsString("Concurrent Events"));
        }
    }

    @Test
    public void testCalculateNext() throws Exception {
        EventKey eventKey = new EventKey("stock", 4);

        try {
            defaultConcurrencyResolver.calculateNext(eventKey, 5);
        } catch (Exception e) {
            assertThat(e, Matchers.instanceOf(ConcurrentEventException.class));
            assertThat(e.getMessage(), Matchers.containsString("Concurrent Events for:"));
        }
    }
}
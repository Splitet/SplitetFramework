package com.kloia.eventapis.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kloia.eventapis.api.IdCreationStrategy;
import com.kloia.eventapis.api.Views;
import com.kloia.eventapis.api.impl.UUIDCreationStrategy;
import com.kloia.eventapis.cassandra.ConcurrencyResolver;
import com.kloia.eventapis.cassandra.ConcurrentEventException;
import com.kloia.eventapis.cassandra.DefaultConcurrencyResolver;
import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.EventRecorder;
import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.PublishedEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.kafka.IOperationRepository;
import com.kloia.eventapis.kafka.SerializableConsumer;
import com.kloia.eventapis.pojos.Event;
import com.kloia.eventapis.pojos.EventState;
import com.kloia.eventapis.view.Entity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by orhanburak.bozan on 19/08/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class CompositeRepositoryImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @InjectMocks
    private CompositeRepositoryImpl compositeRepository;

    @Mock
    private EventRecorder eventRecorder;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private IOperationRepository operationRepository;

    @Mock
    private IdCreationStrategy idCreationStrategy = new UUIDCreationStrategy();

    @Captor
    private ArgumentCaptor<Function<EntityEvent, ConcurrencyResolver<ConcurrentEventException>>> concurrencyResolverFactoryCaptor;

    @Captor
    private ArgumentCaptor<Optional<EventKey>> previousEventKeyCaptor;

    @Mock
    private ObjectWriter objectWriter;

    @Mock
    private EventKey eventKey;

    private PublishedEvent successEvent;
    private PublishedEvent failEvent;
    private PublishedEvent intermediateEvent;
    private String intermediateEventJson;
    private String successEventJson;
    private String failEventJson;
    private Map<String, String> userContextMap;

    @Before
    public void setUp() throws ConcurrentEventException, EventStoreException, JsonProcessingException {
        successEvent = new SuccessEvent();
        failEvent = new FailEvent();
        intermediateEvent = new IntermediateEvent();

        intermediateEventJson = "{IntermediateEvent}";
        successEventJson = "{SuccessEvent}";
        failEventJson = "{FailEvent}";

        userContextMap = new HashMap<>();

        when(objectMapper.writerWithView(Views.PublishedOnly.class)).thenReturn(objectWriter);
//        when(userContext.getUserContext()).thenReturn(userContextMap);
//        when(operationContext.getContext()).thenReturn(new Context("opId"));
//        when(operationContext.getContextOpId()).thenReturn(OperationContext.OP_ID);
//        when(operationContext.getCommandContext()).thenReturn("eventId");
    }

    @Test
    public void shouldMarkFail() {
        compositeRepository.markFail("opId");

        verify(eventRecorder).markFail("opId");
    }

    @Test
    public void shouldMarkSuccess() {
        compositeRepository.markSuccess("opId");

        verify(eventRecorder).markSuccess("opId");
    }

    private void mockCommon(PublishedEvent event) throws EventStoreException, ConcurrentEventException, JsonProcessingException {
        when(eventRecorder.recordEntityEvent(eq(event), anyLong(), previousEventKeyCaptor.capture(), concurrencyResolverFactoryCaptor.capture())).thenReturn(eventKey);
        when(objectWriter.writeValueAsString(event)).thenReturn("{" + event.getClass().getSimpleName() + "}");
    }

    private void assertCommon(PublishedEvent event) {
        verify(operationRepository).publishEvent(eq(event.getClass().getSimpleName()), eq("{" + event.getClass().getSimpleName() + "}"), anyLong());

//        assertThat(publishedEventWrapper.getUserContext(), equalTo(userContextMap));
//        assertThat(publishedEventWrapper.getContext().getOpId(), equalTo("opId"));
//        assertThat(publishedEventWrapper.getEvent(), equalTo("{" + event.getClass().getSimpleName() + "}"));
    }

    @Test
    public void shouldRecordAndPublishWithPublishedEvent() throws ConcurrentEventException, EventStoreException, JsonProcessingException {
        mockCommon(intermediateEvent);

        EventKey actual = compositeRepository.recordAndPublish(intermediateEvent);

        assertCommon(intermediateEvent);

        assertThat(actual, equalTo(eventKey));
        assertThat(previousEventKeyCaptor.getValue(), equalTo(Optional.empty()));
        assertThat(concurrencyResolverFactoryCaptor.getValue().apply(new EntityEvent()).getClass(), equalTo(DefaultConcurrencyResolver.class));
    }

    @Test
    public void shouldRecordAndPublishWithPreviousEventAndPublishedEvent() throws JsonProcessingException, EventStoreException, ConcurrentEventException {
        mockCommon(intermediateEvent);

        Entity previousEntity = mock(Entity.class);
        EventKey previousEntityEventKey = new EventKey();
        when(previousEntity.getEventKey()).thenReturn(previousEntityEventKey);

        EventKey actual = compositeRepository.recordAndPublish(previousEntity, intermediateEvent);

        assertCommon(intermediateEvent);

        assertThat(actual, equalTo(eventKey));
        assertThat(previousEventKeyCaptor.getValue().isPresent(), equalTo(true));
        assertThat(previousEventKeyCaptor.getValue().get(), equalTo(previousEntityEventKey));
        assertThat(concurrencyResolverFactoryCaptor.getValue().apply(new EntityEvent()).getClass(), equalTo(DefaultConcurrencyResolver.class));
    }

    @Test
    public void shouldRecordAndPublishWithPreviousEventKeyAndPublishedEvent() throws JsonProcessingException, EventStoreException, ConcurrentEventException {
        mockCommon(intermediateEvent);

        EventKey previousEntityEventKey = new EventKey();

        EventKey actual = compositeRepository.recordAndPublish(previousEntityEventKey, intermediateEvent);

        assertCommon(intermediateEvent);

        assertThat(actual, equalTo(eventKey));
        assertThat(previousEventKeyCaptor.getValue().isPresent(), equalTo(true));
        assertThat(previousEventKeyCaptor.getValue().get(), equalTo(previousEntityEventKey));
        assertThat(concurrencyResolverFactoryCaptor.getValue().apply(new EntityEvent()).getClass(), equalTo(DefaultConcurrencyResolver.class));
    }

    @Test
    public void shouldRecordAndPublishWithPreviousEventAndPublishedEventAndConcurrencyResolverFactory() throws JsonProcessingException, EventStoreException, ConcurrentEventException {
        mockCommon(intermediateEvent);

        Entity previousEntity = mock(Entity.class);
        EventKey previousEntityEventKey = new EventKey();
        when(previousEntity.getEventKey()).thenReturn(previousEntityEventKey);
        ConcurrencyResolver concurrencyResolver = mock(ConcurrencyResolver.class);
        Function<EntityEvent, ConcurrencyResolver<ConcurrentEventException>> factory = entityEvent -> concurrencyResolver;

        EventKey actual = compositeRepository.recordAndPublish(previousEntity, intermediateEvent, factory);

        assertCommon(intermediateEvent);

        assertThat(actual, equalTo(eventKey));
        assertThat(previousEventKeyCaptor.getValue().isPresent(), equalTo(true));
        assertThat(previousEventKeyCaptor.getValue().get(), equalTo(previousEntityEventKey));
        assertThat(concurrencyResolverFactoryCaptor.getValue(), equalTo(factory));
    }

    @Test
    public void shouldRecordAndPublishWithPreviousEventKeyAndPublishedEventAndConcurrencyResolverFactory() throws JsonProcessingException, EventStoreException, ConcurrentEventException {
        mockCommon(intermediateEvent);

        EventKey previousEntityEventKey = new EventKey();
        ConcurrencyResolver concurrencyResolver = mock(ConcurrencyResolver.class);
        Function<EntityEvent, ConcurrencyResolver<ConcurrentEventException>> factory = entityEvent -> concurrencyResolver;

        EventKey actual = compositeRepository.recordAndPublish(previousEntityEventKey, intermediateEvent, factory);

        assertCommon(intermediateEvent);

        assertThat(actual, equalTo(eventKey));
        assertThat(previousEventKeyCaptor.getValue().isPresent(), equalTo(true));
        assertThat(previousEventKeyCaptor.getValue().get(), equalTo(previousEntityEventKey));
        assertThat(concurrencyResolverFactoryCaptor.getValue(), equalTo(factory));
    }

    @Test
    public void shouldSuccessOperationWithSuccessEvent() throws ConcurrentEventException, EventStoreException, JsonProcessingException {
        when(eventRecorder.recordEntityEvent(eq(successEvent), anyLong(), previousEventKeyCaptor.capture(), concurrencyResolverFactoryCaptor.capture())).thenReturn(eventKey);
        when(objectWriter.writeValueAsString(successEvent)).thenReturn(successEventJson);

        compositeRepository.recordAndPublish(successEvent);

        ArgumentCaptor<SerializableConsumer> serializableConsumerCaptor = ArgumentCaptor.forClass(SerializableConsumer.class);
        verify(operationRepository).successOperation(eq("SuccessEvent"), serializableConsumerCaptor.capture());

        Event event = new Event();
        serializableConsumerCaptor.getValue().accept(event);
        assertThat(event.getEventState(), equalTo(EventState.TXN_SUCCEEDED));
    }

    @Test
    public void shouldFailOperationWithFailEvent() throws ConcurrentEventException, EventStoreException, JsonProcessingException {
        when(eventRecorder.recordEntityEvent(eq(failEvent), anyLong(), previousEventKeyCaptor.capture(), concurrencyResolverFactoryCaptor.capture())).thenReturn(eventKey);
        when(objectWriter.writeValueAsString(failEvent)).thenReturn(failEventJson);

        compositeRepository.recordAndPublish(failEvent);

        ArgumentCaptor<SerializableConsumer> serializableConsumerCaptor = ArgumentCaptor.forClass(SerializableConsumer.class);
        verify(operationRepository).failOperation(eq("FailEvent"), serializableConsumerCaptor.capture());

        Event event = new Event();
        serializableConsumerCaptor.getValue().accept(event);
        assertThat(event.getEventState(), equalTo(EventState.TXN_FAILED));
    }

    @Test
    public void shouldThrowExceptionWhenObjectWriterThrowsException() throws JsonProcessingException, ConcurrentEventException, EventStoreException {
        expectedException.expect(EventStoreException.class);

        doThrow(JsonProcessingException.class).when(objectWriter).writeValueAsString(intermediateEvent);

        compositeRepository.recordAndPublish(intermediateEvent);
    }

    private static class IntermediateEvent extends PublishedEvent {
        @Override
        public EventType getEventType() {
            return EventType.EVENT;
        }
    }

    private static class FailEvent extends PublishedEvent {
        @Override
        public EventType getEventType() {
            return EventType.OP_FAIL;
        }
    }

    private static class SuccessEvent extends PublishedEvent {
        @Override
        public EventType getEventType() {
            return EventType.OP_SUCCESS;
        }
    }
}
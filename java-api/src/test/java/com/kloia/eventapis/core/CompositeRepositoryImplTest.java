package com.kloia.eventapis.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kloia.eventapis.api.IUserContext;
import com.kloia.eventapis.api.IdCreationStrategy;
import com.kloia.eventapis.api.Views;
import com.kloia.eventapis.api.impl.UUIDCreationStrategy;
import com.kloia.eventapis.cassandra.ConcurrencyResolver;
import com.kloia.eventapis.cassandra.ConcurrentEventException;
import com.kloia.eventapis.cassandra.DefaultConcurrencyResolver;
import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.common.Context;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.EventRecorder;
import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.OperationContext;
import com.kloia.eventapis.common.PublishedEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.kafka.IOperationRepository;
import com.kloia.eventapis.kafka.PublishedEventWrapper;
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
import static org.mockito.Matchers.anyString;
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
    private OperationContext operationContext;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private IOperationRepository operationRepository;
    @Mock
    private IUserContext userContext;
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
        successEvent = new PublishedEvent() {
            @Override
            public EventType getEventType() {
                return EventType.OP_SUCCESS;
            }
        };
        failEvent = new PublishedEvent() {
            @Override
            public EventType getEventType() {
                return EventType.OP_FAIL;
            }
        };
        intermediateEvent = new PublishedEvent() {
            @Override
            public EventType getEventType() {
                return EventType.EVENT;
            }
        };

        intermediateEventJson = "{}";
        successEventJson = "{success}";
        failEventJson = "{fail}";

        userContextMap = new HashMap<>();

        when(objectMapper.writerWithView(Views.PublishedOnly.class)).thenReturn(objectWriter);
        when(userContext.getUserContext()).thenReturn(userContextMap);
        when(operationContext.getContextOpId()).thenReturn(OperationContext.OP_ID);
        when(operationContext.getCommandContext()).thenReturn("eventId");
    }

    @Test
    public void shouldMarkFail() {
        compositeRepository.markFail("opId");

        verify(eventRecorder).markFail("opId");
    }

    private void mockCommon() throws EventStoreException, ConcurrentEventException, JsonProcessingException {
        when(eventRecorder.recordEntityEvent(eq(intermediateEvent), anyLong(), previousEventKeyCaptor.capture(), concurrencyResolverFactoryCaptor.capture())).thenReturn(eventKey);
        when(objectWriter.writeValueAsString(intermediateEvent)).thenReturn(intermediateEventJson);
    }

    private void assertCommon() {
        ArgumentCaptor<PublishedEventWrapper> publishedEventWrapperArgumentCaptor = ArgumentCaptor.forClass(PublishedEventWrapper.class);
        verify(operationRepository).publishEvent(anyString(), publishedEventWrapperArgumentCaptor.capture());

        PublishedEventWrapper publishedEventWrapper = publishedEventWrapperArgumentCaptor.getValue();
        assertThat(publishedEventWrapper.getUserContext(), equalTo(userContextMap));
        assertThat(publishedEventWrapper.getOpId(), equalTo("opId"));
        assertThat(publishedEventWrapper.getEvent(), equalTo(intermediateEventJson));
    }

    @Test
    public void shouldRecordAndPublishWithPublishedEvent() throws ConcurrentEventException, EventStoreException, JsonProcessingException {
        mockCommon();

        EventKey actual = compositeRepository.recordAndPublish(intermediateEvent);

        assertCommon();

        assertThat(actual, equalTo(eventKey));
        assertThat(previousEventKeyCaptor.getValue(), equalTo(Optional.empty()));
        assertThat(concurrencyResolverFactoryCaptor.getValue().apply(new EntityEvent()).getClass(), equalTo(DefaultConcurrencyResolver.class));
    }

    @Test
    public void shouldRecordAndPublishWithPreviousEventAndPublishedEvent() throws JsonProcessingException, EventStoreException, ConcurrentEventException {
        mockCommon();

        Entity previousEntity = mock(Entity.class);
        EventKey previousEntityEventKey = new EventKey();
        when(previousEntity.getEventKey()).thenReturn(previousEntityEventKey);

        EventKey actual = compositeRepository.recordAndPublish(previousEntity, intermediateEvent);

        assertCommon();

        assertThat(actual, equalTo(eventKey));
        assertThat(previousEventKeyCaptor.getValue().isPresent(), equalTo(true));
        assertThat(previousEventKeyCaptor.getValue().get(), equalTo(previousEntityEventKey));
        assertThat(concurrencyResolverFactoryCaptor.getValue().apply(new EntityEvent()).getClass(), equalTo(DefaultConcurrencyResolver.class));
    }

    @Test
    public void shouldRecordAndPublishWithPreviousEventKeyAndPublishedEvent() throws JsonProcessingException, EventStoreException, ConcurrentEventException {
        mockCommon();

        EventKey previousEntityEventKey = new EventKey();

        EventKey actual = compositeRepository.recordAndPublish(previousEntityEventKey, intermediateEvent);

        assertCommon();

        assertThat(actual, equalTo(eventKey));
        assertThat(previousEventKeyCaptor.getValue().isPresent(), equalTo(true));
        assertThat(previousEventKeyCaptor.getValue().get(), equalTo(previousEntityEventKey));
        assertThat(concurrencyResolverFactoryCaptor.getValue().apply(new EntityEvent()).getClass(), equalTo(DefaultConcurrencyResolver.class));
    }

    @Test
    public void shouldRecordAndPublishWithPreviousEventAndPublishedEventAndConcurrencyResolverFactory() throws JsonProcessingException, EventStoreException, ConcurrentEventException {
        mockCommon();

        Entity previousEntity = mock(Entity.class);
        EventKey previousEntityEventKey = new EventKey();
        when(previousEntity.getEventKey()).thenReturn(previousEntityEventKey);
        ConcurrencyResolver concurrencyResolver = mock(ConcurrencyResolver.class);
        Function<EntityEvent, ConcurrencyResolver<ConcurrentEventException>> factory = entityEvent -> concurrencyResolver;

        EventKey actual = compositeRepository.recordAndPublish(previousEntity, intermediateEvent, factory);

        assertCommon();

        assertThat(actual, equalTo(eventKey));
        assertThat(previousEventKeyCaptor.getValue().isPresent(), equalTo(true));
        assertThat(previousEventKeyCaptor.getValue().get(), equalTo(previousEntityEventKey));
        assertThat(concurrencyResolverFactoryCaptor.getValue(), equalTo(factory));
    }

    @Test
    public void shouldRecordAndPublishWithPreviousEventKeyAndPublishedEventAndConcurrencyResolverFactory() throws JsonProcessingException, EventStoreException, ConcurrentEventException {
        mockCommon();

        EventKey previousEntityEventKey = new EventKey();
        ConcurrencyResolver concurrencyResolver = mock(ConcurrencyResolver.class);
        Function<EntityEvent, ConcurrencyResolver<ConcurrentEventException>> factory = entityEvent -> concurrencyResolver;

        EventKey actual = compositeRepository.recordAndPublish(previousEntityEventKey, intermediateEvent, factory);

        assertCommon();

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
        verify(operationRepository).successOperation(eq (new Context("opId")), eq(""), serializableConsumerCaptor.capture());

        Event event = new Event();
        serializableConsumerCaptor.getValue().accept(event);
        assertThat(event.getEventState(), equalTo(EventState.TXN_SUCCEDEED));
    }

    @Test
    public void shouldFailOperationWithFailEvent() throws ConcurrentEventException, EventStoreException, JsonProcessingException {
        when(eventRecorder.recordEntityEvent(eq(failEvent), anyLong(), previousEventKeyCaptor.capture(), concurrencyResolverFactoryCaptor.capture())).thenReturn(eventKey);
        when(objectWriter.writeValueAsString(failEvent)).thenReturn(failEventJson);

        compositeRepository.recordAndPublish(failEvent);

        ArgumentCaptor<SerializableConsumer> serializableConsumerCaptor = ArgumentCaptor.forClass(SerializableConsumer.class);
        verify(operationRepository).failOperation(eq(new Context("opId")), eq(""), serializableConsumerCaptor.capture());

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
}
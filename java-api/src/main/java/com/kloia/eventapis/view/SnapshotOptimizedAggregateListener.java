package com.kloia.eventapis.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.RollbackSpec;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This Snapshot aggregation Listener is optimized for Highly Contented Resources.
 * If resource has concurrent and high rate event generation and Version increases too fast for each entity,
 * it's recommended to use this one.
 *
 * @apiNote this is more experimental compared to {@link AggregateListener}, AggregateListener is more stable and simple
 */
@Slf4j
public class SnapshotOptimizedAggregateListener<T extends Entity> extends AggregateListener<T> {


    public SnapshotOptimizedAggregateListener(ViewQuery<T> viewQuery,
                                              EventRepository eventRepository,
                                              SnapshotRepository<T, String> snapshotRepository,
                                              List<RollbackSpec> rollbackSpecs, ObjectMapper objectMapper) {
        super(viewQuery, eventRepository, snapshotRepository, rollbackSpecs, objectMapper);
    }

    /**
     * If operation Fails:.
     * <ul>
     * <li>Mark Events Fail</li>
     * <li>Run Manual Rollback For These Events</li>
     * <li>And Recalculate snapshot if it's necessary for Fail Cases</li>
     * </ul>
     * If Operation Succeed:
     * <ul>
     * <li>Recalculate snapshot if it's necessary or Success Cases</li>
     * <li>Run Manual Rollback For These Events</li>
     * </ul>
     *
     * @param data op record
     */
    public void listenOperations(ConsumerRecord<String, Operation> data) throws EventStoreException {
        try {
            if (data.value().getTransactionState() == TransactionState.TXN_FAILED) {
                List<EntityEvent> entityEvents = eventRepository.markFail(data.key());
                runRollbacks(entityEvents);
                snapshotFails(entityEvents);
            } else if (data.value().getTransactionState() == TransactionState.TXN_SUCCEEDED) {
                snapshotSuccess(data);
            }
            snapshotRepository.flush();
        } catch (EventStoreException e) {
            log.error("Error while applying operation:" + data.toString() + " Exception:" + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getClass().getSimpleName() + " - opId:" + data.value().getContext().getOpId());
            throw e;
        }

    }

    /**
     * Operation Succeed:.
     * <ul>
     * <li>Query EntityId/Versions for effected Entity/Event Series</li>
     * <li>Calculate Min/Max version for each Entity</li>
     * <ul>
     * <li>If we dont have existing snapshot: Calculate snapshot for EntityId/MaxVersion</li>
     * <li>If we have existing snapshot with version less then MaxVersion:  Calculate new snapshot for EntityId/MaxVersion with existing snapshot</li>
     * <li>If we have existing snapshot with version greater then MaxVersion:  Do nothing, it's valid</li>
     * </ul>
     * </ul>
     *
     * @param data op record
     * @throws EventStoreException Exceptions while updating events
     */
    private void snapshotSuccess(ConsumerRecord<String, Operation> data) throws EventStoreException {
        List<EventKey> eventKeys = viewQuery.queryEventKeysByOpId(data.key());
        Map<String, Pair<Integer, Integer>> minMaxVersions = getMinMax(eventKeys);
        List<T> entities = new ArrayList<>();
        for (Map.Entry<String, Pair<Integer, Integer>> entry : minMaxVersions.entrySet()) {
            T previous = snapshotRepository.findOne(entry.getKey());
            T result = null;
            if (previous == null) {
                // there is no snapshot available...
                result = viewQuery.queryEntity(entry.getKey(), entry.getValue().getRight(), null);
            } else if (previous.getVersion() < entry.getValue().getRight()) {
                result = viewQuery.queryEntity(entry.getKey(), entry.getValue().getRight(), previous);
            }
            if (result != null) {
                entities.add(result);
            }
        }
        snapshotRepository.save(entities);
    }

    /**
     * Operation Failed:.
     * <ul>
     * <li>Query EntityId/Versions for effected Entity/Event Series</li>
     * <li>Calculate Min/Max version for each Entity</li>
     * <ul>
     * <li>If we dont have existing snapshot: Calculate snapshot for EntityId/MaxVersion</li>
     * <li>If we have existing snapshot with version greater then MinVersion:  Calculate new snapshot for EntityId/MaxVersion</li>
     * <li>If we have existing snapshot with version less then MinVersion:  Do nothing, it's valid</li>
     * </ul>
     * </ul>
     *
     * @param entityEvents Events
     * @throws EventStoreException Exceptions while updating events
     */
    private void snapshotFails(List<EntityEvent> entityEvents) throws EventStoreException {
        Map<String, Pair<Integer, Integer>> minMaxVersions = getMinMax(
                entityEvents.stream().map(EntityEvent::getEventKey).collect(Collectors.toList())
        );
        List<T> entities = new ArrayList<>();
        for (Map.Entry<String, Pair<Integer, Integer>> entry : minMaxVersions.entrySet()) {
            T previous = snapshotRepository.findOne(entry.getKey());
            T result = null;
            if (previous == null) {
                // there is no snapshot available...
                result = viewQuery.queryEntity(entry.getKey(), entry.getValue().getRight(), null);
            } else if (previous.getVersion() >= entry.getValue().getLeft()) {
                // snapshot is taken near events
                result = viewQuery.queryEntity(entry.getKey(), Math.max(previous.getVersion(), entry.getValue().getRight()), null);
            }
            if (result != null)
                entities.add(result);
        }
        snapshotRepository.save(entities);
    }

    private Map<String, Pair<Integer, Integer>> getMinMax(List<EventKey> entityEvents) {
        Map<String, Pair<Integer, Integer>> minMaxVersions = new HashMap<>();
        for (EventKey eventKey : entityEvents) {
            int newVersion = eventKey.getVersion();
            minMaxVersions.compute(eventKey.getEntityId(),
                    (entityId, pair) -> Pair.of(
                            pair == null ? newVersion : Math.min(newVersion, pair.getLeft()),
                            pair == null ? newVersion : Math.max(newVersion, pair.getRight())
                    )
            );
        }
        return minMaxVersions;
    }

}

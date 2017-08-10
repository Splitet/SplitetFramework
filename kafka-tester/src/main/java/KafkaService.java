import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

@org.springframework.stereotype.Service
@Slf4j
public class KafkaService {



    @KafkaListener(topics = "test", containerFactory = "eventsKafkaListenerContainerFactory")
    public void handleMessage(JsonNode event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.warn(topic + " key: " + topic + " EventData: " + event.toString());
    }
}

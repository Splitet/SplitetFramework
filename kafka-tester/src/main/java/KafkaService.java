import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaService {

    private KafkaTemplate kafkaTemplate;
    private KafkaProducer kafkaProducer;

    @KafkaListener(topics = "test", containerFactory = "eventsKafkaListenerContainerFactory")
    public void handleMessage(JsonNode event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        kafkaTemplate.send("top", "id", "data");

        log.warn(topic + " key: " + topic + " EventData: " + event.toString());
    }
}

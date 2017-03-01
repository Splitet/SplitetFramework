package com.kloia.eventbus;

<<<<<<< HEAD
import lombok.Data;
=======
>>>>>>> 5cb7fff...  - Event bus implementation examples
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by zeldalozdemir on 26/02/2017.
 */
@Component
<<<<<<< HEAD
@Data
=======
>>>>>>> 5cb7fff...  - Event bus implementation examples
public class EventBus {
    @Autowired
    KafkaTemplate kafkaTemplate;


}

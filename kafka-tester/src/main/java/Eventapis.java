import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by mali on 20/01/2017.
 */
@Slf4j
@SpringBootApplication

public class Eventapis {


    public static void mai2n(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Eventapis.class, args);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Map props = new HashMap<>();
        // list of host:port pairs used for establishing the initial connections
        // to the Kakfa cluster
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "kafka-local:9092");
//        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
//                JsonSerializer.class);
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
//                JsonSerializer.class);
        // value to block, after which it will throw a TimeoutException
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 50000);
        AdminClient adminClient = AdminClient.create(props);
        adminClient.describeCluster();
        Collection<TopicListing> topicListings = adminClient.listTopics().listings().get();
        System.out.println(topicListings);
    }

}

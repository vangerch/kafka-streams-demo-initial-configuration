package demo.applcation;

import demo.entity.DemoOrder;
import io.quarkus.kafka.client.serialization.JsonbSerde;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TopologyProcessor {

    @ConfigProperty(name = "INPUT_TOPIC")
    String inputTopic;

    @ConfigProperty(name = "OUTPUT_TOPIC")
    String outputTopic;

    @Produces
    public Topology produceTopology(){

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, DemoOrder> stream = builder.stream(inputTopic, Consumed.with(Serdes.String(), new JsonbSerde<>(DemoOrder.class)));

        stream
                .peek((key, order) -> System.out.println("Order mit key " + key + " wurde bearbeitet."))
                .filter((key, order) -> order.getSummary().contains("example"))
                .mapValues((key, order) -> changeState(order))
                .to(outputTopic, Produced.with(Serdes.String(), new JsonbSerde<>(DemoOrder.class)));

        //Output Format ändern
        /*
        stream
                .peek((key, order) -> System.out.println("Order mit key " + key + " wurde bearbeitet."))
                .mapValues((key, order) -> order.getData().toUpperCase())
                .to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));

         */


        Topology topology = builder.build();

        return topology;
    }

    private DemoOrder changeState(DemoOrder order){
        order.setState(DemoOrder.State.DELIVERED);
        return order;
    }
}

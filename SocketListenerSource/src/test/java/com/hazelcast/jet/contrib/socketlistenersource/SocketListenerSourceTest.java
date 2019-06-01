package com.hazelcast.jet.contrib.socketlistenersource;


import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import org.junit.Test;

import static com.hazelcast.jet.config.ProcessingGuarantee.AT_LEAST_ONCE;


public class SocketListenerSourceTest {
    @Test
    public void test() {
        Pipeline pipeline = Pipeline.create();
        pipeline.drawFrom(SocketListenerSource.jsonRestServer(8080))
                .withIngestionTimestamps()
                .rollingAggregate(AggregateOperations.allOf(
                        AggregateOperations.counting(),
                        AggregateOperations.mapping(JsonUtil.<Double>attributeExtractor("temp"), AggregateOperations.averagingDouble(e -> e))))
                .drainTo(Sinks.logger());

        JetInstance jetInstance = Jet.newJetInstance();

        JobConfig jobConfig = new JobConfig().setName("temp").setProcessingGuarantee(AT_LEAST_ONCE);
        jetInstance.newJob(pipeline, jobConfig).join();
    }

}
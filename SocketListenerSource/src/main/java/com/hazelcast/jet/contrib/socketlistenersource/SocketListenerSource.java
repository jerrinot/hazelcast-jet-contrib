package com.hazelcast.jet.contrib.socketlistenersource;


import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.jet.pipeline.SourceBuilder;
import com.hazelcast.jet.pipeline.StreamSource;
import io.javalin.Javalin;

import java.net.HttpURLConnection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public final class SocketListenerSource {
    private static final String JSON_CONTENT_TYPE = "application/json";

    public static StreamSource<HazelcastJsonValue> jsonRestServer(int port) {
        return SourceBuilder.stream("name", c -> new Reader(port))
                .fillBufferFn(Reader::fillBuffer)
                .distributed(1)
                .destroyFn(Reader::destroy)
                .build();
    }


    private static final class Reader {
        private Javalin app;
        private Queue<HazelcastJsonValue> bufferQueue = new ConcurrentLinkedQueue<>();

        Reader(int port) {
            app = Javalin.create()
                    .port(port)
                    .start();

            app.post("/", ctx -> {
                String contentType = ctx.contentType();
                if (!JSON_CONTENT_TYPE.equals(contentType)) {
                    ctx.status(HttpURLConnection.HTTP_UNSUPPORTED_TYPE);
                    ctx.result("Request content type must be " + JSON_CONTENT_TYPE);
                    return;
                }
                String json = ctx.body();
                bufferQueue.add(new HazelcastJsonValue(json));
                ctx.status(200);
            });
        }

        private void destroy() {
            app.stop();
        }

        private void fillBuffer(SourceBuilder.SourceBuffer<HazelcastJsonValue> buffer) {
            HazelcastJsonValue jsonValue = bufferQueue.poll();
            while (jsonValue != null) {
                buffer.add(jsonValue);
                jsonValue = bufferQueue.poll();
            }
        }
    }
}

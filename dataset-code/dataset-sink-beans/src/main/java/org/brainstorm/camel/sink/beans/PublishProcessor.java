package org.brainstorm.camel.sink.beans;

import java.io.File;
import java.io.FileInputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.BindToRegistry;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.brainstorm.api.event.DataAcquired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@BindToRegistry("publishProcessor")
public class PublishProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(PublishProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        LOG.info("Received event: {}", body);

        ObjectMapper mapper = new ObjectMapper();
        final DataAcquired dataAcquired = mapper.readValue(body, DataAcquired.class);

        ProducerTemplate producerTemplate = exchange.getContext().createProducerTemplate();
        // TODO: this should be handled via config map or something different
        String bootStrapServer = exchange.getContext().getRegistry().lookupByNameAndType("global.bootstrapServer", String.class);
        int port = exchange.getContext().getRegistry().lookupByNameAndType("global.bootstrapServerPort", Integer.class);

        String endpoint = String.format("kafka:camel.dataset?brokers=%s:%d", bootStrapServer, port);

        File path = new File(dataAcquired.getPath(), "dataset");
        final File[] files = path.listFiles();

        if (files == null || files.length == 0) {
            LOG.error("No files found in path: {}", path.getAbsolutePath());
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }

            try (FileInputStream fis = new FileInputStream(file)) {
                final byte[] bytes = fis.readAllBytes();
                String record = new String(bytes);
                LOG.info("Publishing record: {}", record);

                producerTemplate.sendBody(endpoint, record);
            }
        }
    }
}

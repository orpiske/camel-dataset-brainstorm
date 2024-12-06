package org.brainstorm.camel.codefetcher.beans;

import org.apache.camel.BindToRegistry;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.brainstorm.api.event.DataAcquired;

@BindToRegistry("acquiredProcessor")
public class AcquiredProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        DataAcquired dataAcquired = new DataAcquired();

        final String name = exchange.getMessage().getHeader("name", String.class);
        final String repository = exchange.getMessage().getHeader("REPOSITORY", String.class);
        final String dataDir = exchange.getMessage().getHeader("DATA_DIRECTORY", String.class);

        dataAcquired.setName(name);
        dataAcquired.setAddress(repository);
        dataAcquired.setPath(dataDir);

        exchange.getMessage().setBody(dataAcquired);
    }
}

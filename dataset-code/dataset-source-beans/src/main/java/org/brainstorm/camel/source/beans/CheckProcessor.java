package org.brainstorm.camel.source.beans;

import java.io.File;

import org.apache.camel.BindToRegistry;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@BindToRegistry("checkRepo")
public class CheckProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(CheckProcessor.class);

    public void process(Exchange exchange) throws InvalidPayloadException {
        final String repository = exchange.getMessage().getHeader("REPOSITORY", String.class);

        if (repository == null) {
            exchange.getMessage().setHeader("valid", false);
            LOG.error("A null repository was provided");
            return;
        }

        final String dataDir = exchange.getMessage().getHeader("DATA_DIRECTORY", String.class);

        final String[] parts = repository.split("/");
        if (parts == null || parts.length == 0) {
            exchange.getMessage().setHeader("valid", false);
            LOG.error("An invalid repository was provided: {}", repository);
            return;
        }

        var name = parts[parts.length - 1];
        exchange.getMessage().setHeader("valid", true);
        exchange.getMessage().setHeader("name", name);

        File repoDir = new File(dataDir, name);

        LOG.info("Repo dir: {}", repoDir.exists() && repoDir.isDirectory());
        exchange.getMessage().setHeader("exists", repoDir.exists() && repoDir.isDirectory());
        exchange.getMessage().setBody(repository);

        LOG.info("Processing repository {} with address {} ", name, repository);
    }


}

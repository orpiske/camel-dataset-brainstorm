package org.brainstorm.camel.codefetcher;

import java.io.File;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.brainstorm.api.event.DataAcquired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.camel.Exchange;

public class Route extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(Route.class);

    private final String bootstrapHost;
    private final int bootstrapPort;
    private final String dataDir;
    private final String repository;

    public Route(String bootstrapHost, int bootstrapPort, String dataDir, String repository) {
        this.bootstrapHost = bootstrapHost;
        this.bootstrapPort = bootstrapPort;
        this.dataDir = dataDir;
        this.repository = repository;
    }

    private void acquired(Exchange exchange) {
        DataAcquired dataAcquired = new DataAcquired();

        dataAcquired.setName("lalala acquired");
        exchange.getMessage().setBody(dataAcquired);
    }

    private void process(Exchange exchange) {
        if (repository == null) {
            exchange.getMessage().setHeader("valid", false);
            LOG.error("A null repository was provided");
            return;
        }

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
        exchange.getMessage().setHeader("exists", repoDir.exists() && repoDir.isDirectory());
        exchange.getMessage().setBody(repository);

        LOG.info("Processing repository {} with address {} ", name, repository);
    }

    @Override
    public void configure() throws Exception {
        from("timer:start?delay=1000&repeatCount=1")
            .routeId("start")
                    .process(this::process)
                    .choice()
                        .when(header("valid").isEqualTo(true))
                            .to("direct:valid")
                        .otherwise()
                            .to("direct:invalid");


        // If it's a valid repo, then either clone of pull (depending on whether the dest dir exists)
        from("direct:valid")
                .routeId("repositories-valid")
                .choice()
                    .when(header("exists").isEqualTo(false))
                    .to("direct:clone")
                .otherwise()
                    .to("direct:pull")
                    .end()
                .multicast()
                .to("direct:acquired");

        // Handles cloning the repository
        from("direct:clone")
                .routeId("repositories-clone")
                .toD(String.format("git://%s/${header.name}?operation=clone&remotePath=${body}", dataDir));


        // Handles pulling the repository (i.e.: if it has been cloned in the past)
        from("direct:pull")
                .routeId("repositories-pull")
                .toD(String.format("git://%s/${header.name}?operation=pull&remoteName=origin", dataDir));

//        from("direct:pulling")
//                .routeId("repositories-pulling")
//                .toF("kafka:tracking?brokers=%s:%d", bootstrapHost, bootstrapPort);

        // Logs if invalid stuff is provided
        from("direct:invalid")
                .routeId("repositories-invalid")
                .log(LoggingLevel.ERROR, "Unable to process repository ${body}");


        from("direct:acquired")
                .routeId("data-acquired")
                // For the demo: this one is really cool, because without it, sending to Kafka is quite slow
                //                .aggregate(constant(true)).completionSize(20).aggregationStrategy(AggregationStrategies.groupedBody())
                //                .threads(5)
                .process(this::acquired)
                .marshal().json(JsonLibrary.Jackson)
                .toF("kafka:commits?brokers=%s:%d", bootstrapHost, bootstrapPort);
    }
}

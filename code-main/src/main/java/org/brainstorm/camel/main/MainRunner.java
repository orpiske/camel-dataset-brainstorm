package org.brainstorm.camel.main;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.camel.CamelContext;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.main.download.DependencyDownloader;
import org.apache.camel.main.download.DependencyDownloaderClassLoader;
import org.apache.camel.main.download.DependencyDownloaderRoutesLoader;
import org.apache.camel.main.download.MavenDependencyDownloader;
import org.apache.camel.spi.Resource;
import org.apache.camel.spi.ResourceLoader;
import org.apache.camel.spi.RoutesLoader;
import org.apache.camel.support.PluginHelper;
import picocli.CommandLine;

public class MainRunner implements Callable<Integer> {
    @CommandLine.Option(names = {"-f", "--file"}, description = "The integration file to use", required = true)
    private String file;

    @CommandLine.Option(names = {"-s", "--bootstrap-server"}, description = "The Kafka bootstrap server to use", required = true)
    private String bootstrapServer;

    @CommandLine.Option(names = {"-p", "--bootstrap-server-port"}, description = "The Kafka bootstrap server port to use", defaultValue = "9092")
    private int bootstrapPort;

   @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
    private boolean helpRequested = false;


    private static void loadRoute(CamelContext context, String path) {
        final ExtendedCamelContext camelContextExtension = context.getCamelContextExtension();

        DependencyDownloaderClassLoader cl = new DependencyDownloaderClassLoader(null);
        MavenDependencyDownloader downloader = new MavenDependencyDownloader();
        downloader.setClassLoader(cl);

        camelContextExtension.addContextPlugin(DependencyDownloader.class, downloader);

        DependencyDownloaderRoutesLoader loader = new DependencyDownloaderRoutesLoader(context);
        camelContextExtension.addContextPlugin(RoutesLoader.class, loader);

        final ResourceLoader resourceLoader = PluginHelper.getResourceLoader(context);
        final Resource resource = resourceLoader.resolveResource(path);

        try {
            loader.loadRoutes(resource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        context.build();
    }

    public static void main(String[] args) throws Exception {
        int exitCode = new CommandLine(new MainRunner()).execute(args);

        System.exit(exitCode);


    }

    @Override
    public Integer call() throws Exception {
        CamelContext context = new DefaultCamelContext();

        CountDownLatch launchLatch = new CountDownLatch(1);

        loadRoute(context, file);
        context.addRoutes(new DataAcquiredRoute(bootstrapServer, bootstrapPort, launchLatch));

        try {
            context.start();
        } finally {
            launchLatch.await();
        }

        return 0;
    }
}

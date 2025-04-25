package java.prototype_5;

import java.prototype_5.config.ServerConfig;
import java.prototype_5.server.CombinedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    
    public static void main(String[] args) {
        try {
            ServerConfig config = ServerConfig.createDefault();
            
            CombinedServer server = new CombinedServer(config);
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down server...");
                server.shutdown();
            }));
            
            logger.info("Starting combined STUN/HTTP3 server on port {}", config.getPort());
            server.start();
        } catch (Exception e) {
            logger.error("Failed to start server", e);
            System.exit(1);
        }
    }
}
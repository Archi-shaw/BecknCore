package java.prototype_5.config;

import java.io.File;

public class ServerConfig {
    private final int port;
    private final File certificateChainFile;
    private final File privateKeyFile;
    
    public ServerConfig(int port, File certificateChainFile, File privateKeyFile) {
        this.port = port;
        this.certificateChainFile = certificateChainFile;
        this.privateKeyFile = privateKeyFile;
    }
    
    public int getPort() {
        return port;
    }
    
    public File getCertificateChainFile() {
        return certificateChainFile;
    }
    
    public File getPrivateKeyFile() {
        return privateKeyFile;
    }
    
    public static ServerConfig createDefault() {
        ClassLoader classLoader = ServerConfig.class.getClassLoader();
        File certFile = new File(classLoader.getResource("certs/cert.crt").getFile());
        File keyFile = new File(classLoader.getResource("certs/private.key").getFile());
        
        return new ServerConfig(8443, certFile, keyFile);
    }
}
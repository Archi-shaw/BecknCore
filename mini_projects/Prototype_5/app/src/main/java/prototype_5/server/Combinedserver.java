package java.prototype_5.server;

import java.prototype_5.config.ServerConfig;
import java.prototype_5.handler.Http3Handler;
import java.prototype_5.handler.PacketInspector;
import java.prototype_5.handler.StunHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.incubator.codec.http3.Http3;
import io.netty.incubator.codec.http3.Http3ServerConnectionHandler;
import io.netty.incubator.codec.quic.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

public class CombinedServer {
    private static final Logger logger = LoggerFactory.getLogger(CombinedServer.class);
    
    private final ServerConfig config;
    private EventLoopGroup group;
    private Channel channel;
    
    public CombinedServer(ServerConfig config) {
        this.config = config;
    }
    
    public void start() throws Exception {
        group = new NioEventLoopGroup();
        
        try {
            Bootstrap bootstrap = new Bootstrap();
            final QuicTokenHandler tokenHandler = new InsecureQuicTokenHandler();
            final QuicSslContext sslContext = createSslContext();
            
            bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        
                        pipeline.addLast(new PacketInspector());
                        
                        pipeline.addLast(new StunHandler());
                        
                        pipeline.addLast(new Http3Handler());
                        
                        QuicServerCodecBuilder serverCodecBuilder = new QuicServerCodecBuilder()
                            .sslContext(sslContext)
                            .maxIdleTimeout(5000, TimeUnit.MILLISECONDS)
                            .initialMaxData(10000000)
                            .initialMaxStreamDataBidirectionalLocal(1000000)
                            .initialMaxStreamDataBidirectionalRemote(1000000)
                            .initialMaxStreamsBidirectional(100)
                            .tokenHandler(tokenHandler)
                            .handler(new QuicChannelInitializer() {
                                @Override
                                protected void initChannel(QuicChannel channel) {
                                    Http3ServerConnectionHandler h3ConnectionHandler = 
                                        Http3.newServerConnectionHandler(new Http3Handler.Http3RequestHandler());
                                    channel.pipeline().addLast(h3ConnectionHandler);
                                    logger.debug("HTTP/3 connection initialized");
                                }
                            });
                        
                        pipeline.addLast(serverCodecBuilder.build());
                    }
                });
            
            channel = bootstrap.bind(new InetSocketAddress(config.getPort())).sync().channel();
            logger.info("Combined STUN/HTTP3 server started on port {}", config.getPort());
            
            channel.closeFuture().sync();
        } finally {
            shutdown();
        }
    }
    
    private QuicSslContext createSslContext() throws CertificateException {
        if (config.getCertificateChainFile().exists() && config.getPrivateKeyFile().exists()) {
            return QuicSslContextBuilder.forServer(
                config.getPrivateKeyFile(), null, config.getCertificateChainFile())
                .applicationProtocols(Http3.supportedApplicationProtocols())
                .build();
        } else {
            SelfSignedCertificate selfSignedCert = new SelfSignedCertificate();
            return QuicSslContextBuilder.forServer(
                selfSignedCert.key(), null, selfSignedCert.cert())
                .applicationProtocols(Http3.supportedApplicationProtocols())
                .build();
        }
    }
    
    public void shutdown() {
        if (channel != null) {
            channel.close().syncUninterruptibly();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
        logger.info("Server shutdown complete");
    }
    
    private static final class InsecureQuicTokenHandler implements QuicTokenHandler {
        private static final Logger logger = LoggerFactory.getLogger(InsecureQuicTokenHandler.class);
        
        @Override
        public boolean writeToken(ByteBuf out, ByteBuf dcid, InetSocketAddress address) {
            out.writeByte(dcid.readableBytes());
            out.writeBytes(dcid.duplicate());
            logger.debug("Generated token for {}", address);
            return true;
        }
        
        @Override
        public int validateToken(ByteBuf token, InetSocketAddress address) {
            if (token.readableBytes() >= 1) {
                int len = token.readByte();
                if (token.readableBytes() >= len) {
                    return len;
                }
            }
            return -1;
        }
        
        @Override
        public QuicTokenHandler.RetrievedAddressResults extractAddressFromToken(
                ByteBuf token, int retrieveAddressIndex, ByteBuf oldDcid) {
            return new QuicTokenHandler.RetrievedAddressResults(true, null);
        }
    }
}
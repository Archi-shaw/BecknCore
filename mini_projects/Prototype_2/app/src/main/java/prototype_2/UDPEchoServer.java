package prototype_2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UDPEchoServer {
    private static final int PORT = 3478;

    public static void main(String[] args) throws Exception {
        // Create an EventLoopGroup for handling I/O operations
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            // Bootstrap for configuring the UDP server
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new UDPEchoServerHandler());

            // Bind to port 3478
            bootstrap.bind(PORT).sync().channel().closeFuture().await();
        } finally {
            // Shut down the EventLoopGroup gracefully
            group.shutdownGracefully();
        }
    }
}
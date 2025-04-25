package org.beckn.prototype_3; 

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class HttpServer {
    private static final int PORT = 3478;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting HTTP server on port " + PORT);
        
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                            new HttpServerCodec(),
                            new HttpObjectAggregator(65536),
                            new SimpleChannelInboundHandler<FullHttpRequest>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
                                    System.out.println("Received HTTP request: " + request.uri());
                                    
                                    FullHttpResponse response = new DefaultFullHttpResponse(
                                            HttpVersion.HTTP_1_1, 
                                            HttpResponseStatus.OK,
                                            Unpooled.copiedBuffer("Hello from Beckn", CharsetUtil.UTF_8));
                                    
                                    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
                                    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                                    
                                    ctx.writeAndFlush(response)
                                        .addListener(ChannelFutureListener.CLOSE);
                                    
                                    System.out.println("Sent response: 'Hello from Beckn'");
                                }
                            });
                    }
                });
            
            Channel channel = b.bind(new InetSocketAddress(PORT)).sync().channel();
            System.out.println("HTTP server started and listening on port " + PORT);
            
            channel.closeFuture().sync();
            
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("HTTP server shutdown");
        }
    }
}
package java.prototype_5.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.incubator.codec.http3.Http3;
import io.netty.incubator.codec.http3.Http3DataFrame;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.http3.Http3RequestStreamInboundHandler;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.incubator.codec.quic.QuicStreamChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

public class Http3Handler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Http3Handler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof PacketInspector.StunPacketMarker) {
            ((PacketInspector.StunPacketMarker) msg).content().release();
            return;
        }

        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Error in HTTP/3 handler", cause);
        ctx.close();
    }

    public static class Http3RequestHandler extends Http3RequestStreamInboundHandler {
        private static final Logger logger = LoggerFactory.getLogger(Http3RequestHandler.class);

        @Override
        protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame headersFrame, 
                                  boolean isLast) {
            logger.debug("Received HTTP/3 headers: {}", headersFrame.headers());

            Http3HeadersFrame responseHeaders = Http3.newHttp3HeadersFrame();
            responseHeaders.headers()
                .status(OK.codeAsText())
                .set(CONTENT_TYPE, TEXT_PLAIN)
                .set(SERVER, "Netty-HTTP3-STUN-Server");

            QuicStreamChannel channel = (QuicStreamChannel) ctx.channel();
            ctx.write(responseHeaders);

            if (isLast) {
                ByteBuf content = ctx.alloc().buffer();
                content.writeBytes("Hello from HTTP/3 server!".getBytes());
                ctx.writeAndFlush(new Http3DataFrame(content));
            }
        }

        @Override
        protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame dataFrame, boolean isLast) {
            try {
                logger.debug("Received HTTP/3 data frame, size: {}", dataFrame.content().readableBytes());
                
                ByteBuf content = ctx.alloc().buffer();
                content.writeBytes("Received your HTTP/3 data, thanks!".getBytes());
                
                Http3DataFrame responseDataFrame = new Http3DataFrame(content);
                ctx.write(responseDataFrame);
                
                if (isLast) {
                    ctx.flush();
                }
            } finally {
                dataFrame.release();
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }
    }
}
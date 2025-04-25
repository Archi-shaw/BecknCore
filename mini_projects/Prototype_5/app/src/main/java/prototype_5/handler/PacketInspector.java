package java.prototype_5.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketInspector extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(PacketInspector.class);
    
    private static final int STUN_MAGIC_COOKIE = 0x2112A442;
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof ByteBuf)) {
            ctx.fireChannelRead(msg);
            return;
        }
        
        ByteBuf buf = (ByteBuf) msg;
        if (!buf.isReadable(4)) {
            ctx.fireChannelRead(msg);
            return;
        }
        
        buf.markReaderIndex();
        
       
        int firstByte = buf.readByte() & 0xFF;
        
        if ((firstByte & 0xC0) != 0) {
            buf.resetReaderIndex();
            logger.debug("Detected QUIC/HTTP3 packet");
            ctx.fireChannelRead(msg);
            return;
        }
        
        buf.skipBytes(3);
        
        if (buf.readableBytes() >= 4) {
            int magicCookie = buf.readInt();
            if (magicCookie == STUN_MAGIC_COOKIE) {
                buf.resetReaderIndex();
                logger.debug("Detected STUN packet");
                ctx.fireChannelRead(new StunPacketMarker(buf.retain()));
                return;
            }
        }
        
        buf.resetReaderIndex();
        logger.debug("Forwarding to HTTP3/QUIC handler");
        ctx.fireChannelRead(msg);
    }
    
    public static class StunPacketMarker {
        private final ByteBuf content;
        
        public StunPacketMarker(ByteBuf content) {
            this.content = content;
        }
        
        public ByteBuf content() {
            return content;
        }
    }
}
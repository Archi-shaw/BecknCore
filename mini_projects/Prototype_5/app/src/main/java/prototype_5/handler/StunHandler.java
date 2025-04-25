package java.prototype_5.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.ice4j.StunException;
import org.ice4j.message.Message;
import org.ice4j.message.MessageFactory;
import org.ice4j.message.Response;
import org.ice4j.stack.StunStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class StunHandler extends SimpleChannelInboundHandler<PacketInspector.StunPacketMarker> {
    private static final Logger logger = LoggerFactory.getLogger(StunHandler.class);
    private final StunStack stunStack;
    
    public StunHandler() {
        this.stunStack = new StunStack();
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PacketInspector.StunPacketMarker msg) throws Exception {
        ByteBuf content = msg.content();
        try {
            byte[] bytes = new byte[content.readableBytes()];
            content.readBytes(bytes);
            
            Message stunMessage = MessageFactory.parseMessage(bytes, 0, bytes.length);
            logger.debug("Received STUN message: {}", stunMessage);
            
            if (stunMessage instanceof Response) {
                logger.debug("Received STUN response, forwarding to client");
            } else {
                Response response = stunStack.getServerResponseFactory()
                    .createBindingResponse(stunMessage, new InetSocketAddress(0));
                
                byte[] responseBytes = response.encode();
                
                ByteBuf responseBuf = ctx.alloc().buffer(responseBytes.length);
                responseBuf.writeBytes(responseBytes);
                
                InetSocketAddress clientAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                
                if (ctx.channel().isActive()) {
                    DatagramPacket packet = new DatagramPacket(responseBuf, clientAddress);
                    ctx.writeAndFlush(packet);
                    logger.debug("Sent STUN response to {}", clientAddress);
                }
            }
        } catch (StunException e) {
            logger.error("Error processing STUN message", e);
        } finally {
            content.release();
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Error in STUN handler", cause);
        ctx.close();
    }
}
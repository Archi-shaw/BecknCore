package com.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class CombinedServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private static final Logger logger = LoggerFactory.getLogger(CombinedServerHandler.class);
    private static final int MAGIC_COOKIE = 0x2112A442;
    private static final short STUN_BINDING_REQUEST = 0x0001;
    private static final short STUN_BINDING_RESPONSE = 0x0101;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        ByteBuf content = packet.content();
        
        if (isStunRequest(content)) {
            logger.info("Received STUN request from: {}", packet.sender());
            handleStun(ctx, packet);
        } else {
            logger.info("Received regular message from: {}", packet.sender());
            echoMessage(ctx, packet);
        }
    }

    private boolean isStunRequest(ByteBuf buf) {
        if (buf.readableBytes() < 20) {
            return false;
        }
        
        int firstByte = buf.getUnsignedByte(buf.readerIndex());
        if ((firstByte & 0xC0) != 0) { 
            return false;
        }
        
        int messageType = buf.getUnsignedShort(buf.readerIndex());
        
        int magicCookie = buf.getInt(buf.readerIndex() + 4);
        
        return messageType == STUN_BINDING_REQUEST && magicCookie == MAGIC_COOKIE;
    }

    private void handleStun(ChannelHandlerContext ctx, DatagramPacket packet) {
        ByteBuf request = packet.content();
        
        ByteBuf response = Unpooled.buffer(32);
        
        response.writeShort(STUN_BINDING_RESPONSE);  
        
        response.writeShort(12);
        
        response.writeInt(MAGIC_COOKIE);
        
        byte[] transactionId = new byte[12];
        request.getBytes(8, transactionId);
        response.writeBytes(transactionId);
        
        response.writeShort(0x0020);  
        response.writeShort(8);       
        response.writeByte(0);        
        response.writeByte(1);       
        
        int port = packet.sender().getPort();
        response.writeShort(port ^ (MAGIC_COOKIE >> 16));
        
        byte[] ipBytes = packet.sender().getAddress().getAddress();
        int ipInt = ((ipBytes[0] & 0xFF) << 24) | 
                    ((ipBytes[1] & 0xFF) << 16) | 
                    ((ipBytes[2] & 0xFF) << 8) | 
                    (ipBytes[3] & 0xFF);
        response.writeInt(ipInt ^ MAGIC_COOKIE);
        
        ctx.writeAndFlush(new DatagramPacket(response, packet.sender()));
        
        logger.info("Sent STUN response with XOR-mapped address {}:{} to {}", 
                packet.sender().getAddress().getHostAddress(), 
                packet.sender().getPort(),
                packet.sender());
    }

    private void echoMessage(ChannelHandlerContext ctx, DatagramPacket packet) {
        ByteBuf content = packet.content();
        
        if (isTextMessage(content)) {
            String message = content.toString(CharsetUtil.UTF_8);
            logger.info("Received message: {}", message);
        } else {
            logger.info("Received binary data of {} bytes", content.readableBytes());
        }
        
        ByteBuf echoed = Unpooled.copiedBuffer(content);
        ctx.writeAndFlush(new DatagramPacket(echoed, packet.sender()));
    }
    
    private boolean isTextMessage(ByteBuf content) {
        ByteBuf copy = content.copy();
        try {
            while (copy.isReadable()) {
                byte b = copy.readByte();
                if ((b < 32 || b > 126) && b != 9 && b != 10 && b != 13) {
                    return false;
                }
            }
            return true;
        } finally {
            copy.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Error in server handler", cause);
    }
}
package prototype_2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class UDPEchoServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        // Echo the received packet back to the sender
        ctx.writeAndFlush(new DatagramPacket(
                packet.content().retainedDuplicate(),
                packet.sender()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Handle exceptions
        cause.printStackTrace();
        ctx.close();
    }
}
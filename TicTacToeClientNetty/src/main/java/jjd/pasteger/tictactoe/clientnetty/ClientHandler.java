package jjd.pasteger.tictactoe.clientnetty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class ClientHandler extends SimpleChannelInboundHandler<String> {
    private final BlockingQueue<String> messageQueue;
    private final CountDownLatch latch;

    public ClientHandler(BlockingQueue<String> messageQueue, CountDownLatch latch) {
        this.messageQueue = messageQueue;
        this.latch = latch;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) {
        if (messageQueue != null)
            try {
                messageQueue.put(s);
                latch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println(cause.getMessage());
        ctx.channel().close();
        try {
            messageQueue.put("exit");
            latch.countDown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

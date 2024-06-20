package jjd.pasteger.tictactoe.clientnetty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class NetworkChannelInitializer extends ChannelInitializer<SocketChannel> {
    private SocketChannel channel;
    private final BlockingQueue<String>  messageQueue;
    private final CountDownLatch latch;
    public NetworkChannelInitializer(BlockingQueue<String>  messageQueue, CountDownLatch latch) {
        this.messageQueue = messageQueue;
        this.latch = latch;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        channel = socketChannel;
        socketChannel.pipeline().addLast(new StringDecoder(), new StringEncoder(),
                new ClientHandler(messageQueue, latch));
    }

    public SocketChannel getChannel() {
        return channel;
    }
}

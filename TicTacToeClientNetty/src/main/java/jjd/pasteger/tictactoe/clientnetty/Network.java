package jjd.pasteger.tictactoe.clientnetty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Network {
    private final ClientConfig clientConfig = new ClientConfig("application.properties");
    private SocketChannel channel;
    private NetworkChannelInitializer networkChannelInitializer;

    public Network(BlockingQueue<String> messageQueue, CountDownLatch latch) {
        Thread thread = new Thread(() -> {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                networkChannelInitializer = new NetworkChannelInitializer(messageQueue, latch);
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .handler(networkChannelInitializer);

                ChannelFuture future = bootstrap.connect(clientConfig.getHost(), clientConfig.getPort()).sync();

                channel = networkChannelInitializer.getChannel();

                latch.countDown();
                future.channel().closeFuture().sync();
            } catch (Exception exception) {
                exception.printStackTrace();
                latch.countDown();
            } finally {
                workerGroup.shutdownGracefully();
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    public void sendMessage(String message) {
        channel.writeAndFlush(message);
    }

    public void disconnect(){
        channel.close();
    }
}

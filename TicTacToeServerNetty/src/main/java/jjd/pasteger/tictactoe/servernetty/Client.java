package jjd.pasteger.tictactoe.servernetty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

public class Client {
    private final ChannelId id;
    private final Channel channel;
    private String name;
    private boolean ready;
    private boolean restartConsent;

    public Client(Channel channel) {
        this.channel = channel;
        this.id = channel.id();
    }

    public ChannelId getId() {
        return id;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getReady() {
        return ready;
    }

    public void setReady(Boolean ready) {
        this.ready = ready;
    }

    public boolean isRestartConsent() {
        return restartConsent;
    }

    public void setRestartConsent(boolean restartConsent) {
        this.restartConsent = restartConsent;
    }
}

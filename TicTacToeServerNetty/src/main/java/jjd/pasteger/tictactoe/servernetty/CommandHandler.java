package jjd.pasteger.tictactoe.servernetty;

import io.netty.channel.*;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler extends SimpleChannelInboundHandler<String> {
    private static final List<Client> clients = new ArrayList<>();
    private static String winnerName = "";
    private static boolean confirmation;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client connected: " + ctx.channel().id());

        if (clients.size() > 1) {
            System.out.println("The session is full. Disconnecting " + ctx.channel().id());
            ctx.channel().writeAndFlush("message:The session is full");
            ctx.channel().disconnect();
            return;
        }

        clients.add(new Client(ctx.channel()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Client disconnected " + ctx.channel().id());

        boolean thisClient = false;
        for (Client c : clients) {
            if (c.getId() == ctx.channel().id()) {
                thisClient = true;
            }
        }

        if (thisClient) {
            String clientName = clients.stream()
                    .filter(client -> client.getId() == ctx.channel().id())
                    .findFirst()
                    .map(Client::getName).get();

            clients.removeIf(client -> client.getId() == ctx.channel().id());

            sendingToAllChannels("message:" + clientName + " disconnected\n" +
                    "Waiting for another player...");
            clearClientMemory();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String message) {
        System.out.println(channelHandlerContext.channel().id() + ": " + message);

        try {
            String[] messageItems = message.split(":");
            CommandTypes command = CommandTypes.valueOf(messageItems[0].toUpperCase());

            switch (command) {
                case SETNAME:
                    setName(channelHandlerContext.channel().id(), messageItems);
                    break;
                case ACTION:
                    playerAction(messageItems);
                    break;
                case START:
                    checkReadiness(channelHandlerContext.channel().id());
                    break;
                case WIN:
                    handleEndGame(messageItems);
                    break;
                case RESTART:
                    restartGame(channelHandlerContext.channel().id());
                    break;
            }
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println(cause.getMessage());

        String clientName = clients.stream()
                .filter(client -> client.getId() == ctx.channel().id())
                .findFirst()
                .map(Client::getName).get();

        clients.removeIf(client -> client.getId() == ctx.channel().id());

        ctx.close();

        sendingToAllChannels("message:" + clientName + " disconnected\n" +
                "Waiting for another player...");
    }

    private void sendingToAllChannels(String sendMessage) {
        for (Client client : clients) {
            client.getChannel().writeAndFlush(sendMessage);
        }
    }

    private void setName(ChannelId id, String[] messageItems) {
        String name = messageItems[1];

        clients.stream()
                .filter(client -> client.getId() == id)
                .findFirst()
                .ifPresent(client -> client.setName(name));

        sendingToAllChannels("message:" + name + " has connected" +
                "\n" + "Waiting for rest players...");

        checkPlayerCount();
    }

    private void playerAction(String[] messageItems) {
        if (clients.size() < 2) return;

        String nextClient = messageItems[1];

        if (nextClient.equals(clients.get(0).getName())) {
            nextClient = clients.get(1).getName();
        } else {
            nextClient = clients.get(0).getName();
        }

        String response = "action:" + nextClient + ":" + messageItems[2] + ":" + messageItems[3];

        sendingToAllChannels(response);
    }

    private void checkReadiness(ChannelId id) {
        clients.stream()
                .filter(client -> client.getId() == id)
                .findFirst()
                .ifPresent(client -> client.setReady(true));

        if (clients.size() > 1) {
            if (clients.get(0).getReady() && clients.get(1).getReady()) {
                sendingToAllChannels("start:" + clients.get(0).getName());
            }
        }
    }

    private void checkPlayerCount() {
        try {
            Thread.sleep(10); //Без этого действия предыдущее сообщение и последующее объединяются в одно
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (clients.size() == 2) {
            sendingToAllChannels("ALL_PLAYERS_CONNECTED");
        }
    }

    private void handleEndGame(String[] messageItems) {
        String name = messageItems[1];

        if (name.equals("draw")) {
            sendingToAllChannels("win:draw");
            return;
        }

        if (name.equals("opponent")) {
            confirmation = true;
        }

        if (clients.stream().anyMatch(client -> client.getName().equals(name))) {
            winnerName = name;
        }

        if (!winnerName.equals("") && confirmation) {
            sendingToAllChannels("win:" + winnerName);
            winnerName = "";
            confirmation = false;
        }
    }

    private void restartGame(ChannelId id) {
        clients.stream().filter(client -> client.getId() == id)
                .findFirst()
                .ifPresent(client -> client.setRestartConsent(true));

        for (Client client : clients) {
            if (!client.isRestartConsent()) {
                return;
            }
        }

        clearClientMemory();

        checkPlayerCount();
    }

    private void clearClientMemory() {
        for (Client client : clients) {
            client.setReady(false);
            client.setRestartConsent(false);
        }

        winnerName = "";
        confirmation = false;
    }
}

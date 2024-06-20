package jjd.pasteger.tictactoe.clientnetty;

import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameApp {
    private static final CountDownLatch latch = new CountDownLatch(1);
    private static final BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(1);
    private static final Scanner scanner = new Scanner(System.in);
    private static Network network;
    private static String[][] gameField = {{" ", " ", " "}, {" ", " ", " "}, {" ", " ", " "}};
    private static String playerName = "";
    private static String mySymbol;
    private static String opponentSymbol;
    private static String conditionOfEnding = " ";
    private static boolean turn = false;

    public static void main(String[] args) {
        initGame();

        gameLoop();

        System.out.println("Game is close");
    }

    private static void initGame() {
        System.out.println("Welcome to TicTac game");
        System.out.println("Enter you name:");
        playerName = scanner.next();

        while (true) {
            inputLoop("connect", 7, "Enter 'connect' to connect to server:");

            try {
                network = new Network(messageQueue, latch);
                System.out.println("connecting...");
                latch.await();
                network.sendMessage("setname:" + playerName);
                break;
            } catch (Exception exception) {
                exception.printStackTrace();
            }

        }
    }

    private static void startGame(String[] responseItems){
        System.out.println(
                " ----------------\n" +
                        "  0:0  |  0:1  |  0:2  \n" +
                        " ----------------\n" +
                        "  1:0  |  1:1  |  1:2  \n" +
                        " ----------------\n" +
                        "  2:0  |  2:1  |  2:2  \n" +
                        " ----------------\n");

        System.out.println("Start " + responseItems[1] + ":\n");

        if (responseItems[1].equals(playerName)) {
            turn = true;
            mySymbol = "X";
            opponentSymbol = "0";
        } else {
            mySymbol = "0";
            opponentSymbol = "X";
        }

        gameField = new String[][]{{" ", " ", " "}, {" ", " ", " "}, {" ", " ", " "}};
    }

    private static void gameLoop() {
        while (true) {
            handleConditionOfEnding();

            String response;
            try {
                latch.await();
                response = messageQueue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            handlingMessage(response);

            handlingPlayerTurn();
        }
    }

    private static void handlingMessage(String response) {
        String[] responseItems = response.split(":");

        switch (responseItems[0]) {
            case "message":
                System.out.println(responseItems[1]);
                break;

            case "all players connected":
                System.out.println("All players connected");
                String message = inputLoop("start", 5,
                        "Enter 'start' if you ready:");
                network.sendMessage(message);
                break;

            case "start":
                startGame(responseItems);
                break;

            case "action":
                playerAction(responseItems);
                break;

            case "win":
                String winMessage = responseItems[1].equals("draw") ?
                        "Draw!" : responseItems[1] + " is win!";

                System.out.println(winMessage);

                inputLoop("again", 5,
                        "Enter 'again' if you want to play again\n" +
                                "or 'exit' to exit the game");
                network.sendMessage("restart");
                break;
            case "exit":
                System.out.println("Game is close");
                System.exit(0);
        }
    }

    private static void handlingPlayerTurn(){
        if (turn) {
            String input;

            while (true) {
                input = inputLoop("[012]:[012]", 3, "Enter x:y");

                int x = Integer.parseInt(input.split(":")[0]);
                int y = Integer.parseInt(input.split(":")[1]);

                if (gameField[x][y].equals(" ")) {
                    break;
                } else {
                    System.out.println("Cell is already filled");
                }
            }

            String message = "action:" + playerName + ":" + input;

            network.sendMessage(message);

            turn = false;
        }
    }

    private static void playerAction(String[] responseItems){
        int x = Integer.parseInt(responseItems[2]);
        int y = Integer.parseInt(responseItems[3]);

        if (responseItems[1].equals(playerName)) {
            gameField[x][y] = mySymbol;
        } else {
            gameField[x][y] = opponentSymbol;
        }

        printField();

        conditionOfEnding = checkConditionsOfEnding();

        if (responseItems[1].equals(playerName) && conditionOfEnding.equals(" ")) {
            turn = true;
        }
        if (conditionOfEnding.equals(" ")) {
            System.out.println("Turn " + responseItems[1] + ":\n");
        }
    }

    private static String checkConditionsOfEnding() {
        for (int i = 0; i < gameField.length; i++) {
            for (int j = 0; j < gameField[i].length; j++) {
                String symbol = gameField[i][j];

                if (symbol.equals(" ")) continue;

                try {
                    if (gameField[i][j + 1].equals(symbol) && gameField[i][j - 1].equals(symbol)) {
                        return symbol;
                    }
                } catch (Exception ignored) {
                }
                try {
                    if (gameField[i + 1][j].equals(symbol) && gameField[i - 1][j].equals(symbol)) {
                        return symbol;
                    }
                } catch (Exception ignored) {
                }
                try {
                    if (gameField[i + 1][j + 1].equals(symbol) && gameField[i - 1][j - 1].equals(symbol)) {
                        return symbol;
                    }
                } catch (Exception ignored) {
                }
                try {
                    if (gameField[i + 1][j - 1].equals(symbol) && gameField[i - 1][j + 1].equals(symbol)) {
                        return symbol;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        int countEmpty = 0;
        for (String[] line : gameField) {
            for (String symbol : line) {
                if (symbol.equals(" ")) {
                    countEmpty++;
                }
            }
        }
        if (countEmpty == 0) {
            return "N";
        }

        return " ";
    }

    private static void handleConditionOfEnding(){
        if (!conditionOfEnding.equals(" ")) {
            System.out.println("Game come to end");

            if (conditionOfEnding.equals("N")) {
                network.sendMessage("win:draw");
            }
            else if (conditionOfEnding.equals(opponentSymbol)) {
                network.sendMessage("win:" + playerName);
            } else {
                network.sendMessage("win:opponent");
            }
            conditionOfEnding = " ";
        }
    }

    private static String inputLoop(String patternString, int length, String message) {
        Pattern pattern = Pattern.compile(patternString);

        while (true) {
            System.out.println(message);

            String input = scanner.next();

            Matcher matcher = pattern.matcher(input);

            if (input.equals("exit")) {
                System.out.println("Game is close");
                network.disconnect();
                System.exit(0);
            }

            if (input.length() == length && matcher.find()) {
                return input;
            } else {
                System.out.println("Incorrect input");
            }
        }
    }

    private static void printField() {
        System.out.println(
                " ----------------\n" +
                        "  " + gameField[0][0] + "  |  " + gameField[0][1] + "  |  " + gameField[0][2] + "  \n" +
                        " ----------------\n" +
                        "  " + gameField[1][0] + "  |  " + gameField[1][1] + "  |  " + gameField[1][2] + "  \n" +
                        " ----------------\n" +
                        "  " + gameField[2][0] + "  |  " + gameField[2][1] + "  |  " + gameField[2][2] + "  \n" +
                        " ----------------\n");
    }
}

package de.unistgt.ipvs.vs.ex1.calcSocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

/**
 * Extend the run-method of this class as necessary to complete the assignment.
 * You may also add some fields, methods, or further classes.
 */
public class CalcSocketServer extends Thread {
    private ServerSocket srvSocket;
    private int port;

    public CalcSocketServer(int port) {
        this.srvSocket = null;
        this.port = port;
    }

    @Override
    public void interrupt() {
        try {
            if (srvSocket != null) srvSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // Start listening server socket ..
        try {
            srvSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not listen on port:" + port + ".");
            System.exit(-1);
        }
        // wait for client connection
        while (true) {
            try {
                 new clientHandler(srvSocket.accept()).start();
            } catch (IOException e) {
                System.out.println("Accept failed: " + port + ".");
                System.exit(-1);
            }
        }
    }

    @Override
    protected void finalize() {
        // cleanup
        try {
            srvSocket.close();
        } catch (IOException e) {
            System.out.println("Could not close socket");
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        CalcSocketServer calcSocketServer = new CalcSocketServer(12345);
        calcSocketServer.start();
    }
}

    class clientHandler extends Thread {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;
        private String input, output;

        public clientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {

            // Read from the streams
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // 5. server sends the content 'RDY' to the client
                String readyMessage = "<08:RDY>";
                out.println(readyMessage);

                Protocol protocol = new Protocol();
                while ((input = in.readLine()) != null) {

                    // 12. session gets terminated when writing to client fails, indicating that client closed the connection
                    if (out.checkError()) {
                        try {
                            if (clientSocket != null) {
                                clientSocket.close();
                            }
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                        System.out.println("Session terminated");
                    }
                    // 6. after each received message, the server sends the response 'OK' to the client
                    out.println("OK");
                    output = protocol.processInput(input);

                    if (output != null) {

                        // sends the result of the calculation only when processing '>' character
                        out.println(output);
                    }
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        class Protocol {
            private List calculatorInfoOperators = Arrays.asList("ADD", "SUB", "MUL", "RES", "RDY", "OK", "ERR", "FIN");
            private String output;
            Integer result = 0;

            public String processInput(String input) {

                // 1. messages are case-insensitive, so all are upperCase
                String inputUpper = input.toUpperCase();
                Integer indexOfSmallerThan;
                Integer indexOfGreaterThan;

                // 1. counts how often '<' and '>' occur in a message
                Integer countSmallerThan = inputUpper.length() - inputUpper.replace("<", "").length();
                Integer countGreaterThan = inputUpper.length() - inputUpper.replace(">", "").length();

                // 1. messages are framed by two characters '<' and '>', both only allowed once in a message
                if (countSmallerThan.equals(1) && countGreaterThan.equals(1)) {
                    indexOfSmallerThan = inputUpper.indexOf("<");
                    indexOfGreaterThan = inputUpper.indexOf(">");

                    // 1. and 2. bytes outside a message are ignored
                    inputUpper = inputUpper.substring(indexOfSmallerThan, indexOfGreaterThan + 1).trim();
                } else {
                    System.out.println("The message requires the two characters '<' and '>', once each!");
                    return null;
                }
                // 2. checks if the two characters following '<' are the two digits indicating message length
                String digitsStr = inputUpper.substring(indexOfSmallerThan + 1,
                        indexOfSmallerThan + 3).trim();
                Integer digits = Integer.parseInt(digitsStr);
                if (!digitsStr.matches("-?\\d+(\\.\\d+)?")) {
                    System.out.println("Message in the wrong format, needs two digits after the '<' Operator!");
                    return null;
                    // checks whether the message length equals the two digits which indicate the message length
                } else if (!digits.equals(input.length())) {
                    return null;
                }

                // 2. get the colon after the two digits
                String colonInMessage = inputUpper.substring(indexOfSmallerThan + 3, indexOfSmallerThan + 4).trim();

                // 2. get the remaining string after the colon till the end
                String validContent = inputUpper.substring(indexOfSmallerThan + 4, countGreaterThan + 1).trim();

                // 4. split the content of the message by whitespace, for checking each remaining substring
                String[] splitMessageContent = validContent.trim().split("\\s+");

                // 2. checks if a colon follows the two digits
                // 2. and 3. checks if integer values, a calculation or info operator follows the colon
                if (colonInMessage.equals(":") && splitMessageContent.length > 0) {

                    // checks if the first substring after the colon is a calculator/info operator for correct message format
                    if (!calculatorInfoOperators.contains(splitMessageContent[0])) {
                        return null;
                    }

                    Integer currentNumber;
                    String currentCalcInfoOperator = "";
                    for (String message : splitMessageContent) {

                        // 8. checks if current string is an Integer,
                        // if so calculate the value based on current calculation operator
                        if (message.matches("-?\\d+(\\.\\d+)?")) {
                            currentNumber = Integer.parseInt(message);

                            // 9. Each valid content is acknowledged to the client with the content 'OK' followed by single
                            // whitespace character and the valid content
                            sendMessageToClient(currentNumber.toString(), "Number");
                            switch (currentCalcInfoOperator) {
                                case "ADD":
                                    result += currentNumber;
                                    break;
                                case "SUB":
                                    result -= currentNumber;
                                    break;
                                case "MUL":
                                    result *= currentNumber;
                                    break;
                            }
                            // 3. checks if a calculation/info operator does not follow the colon or the end of the message '>'
                        } else if (calculatorInfoOperators.contains(message)) {
                            int index = calculatorInfoOperators.indexOf(message);

                            // 7. if valid content equals 'ADD', 'SUB' or 'MUL' the calculation operator is changed
                            switch (message) {
                                case "ADD":
                                    calculatorInfoOperators.set(index, "ADDing");
                                    currentCalcInfoOperator = "ADD";
                                    sendMessageToClient(message, "ADD");
                                    break;
                                case "SUB":
                                    calculatorInfoOperators.set(index, "SUBtracting");
                                    currentCalcInfoOperator = "SUB";
                                    sendMessageToClient(message,"SUB");
                                    break;
                                case "MUL":
                                    calculatorInfoOperators.set(index, "MULtiplying");
                                    currentCalcInfoOperator = "MUL";
                                    sendMessageToClient(message,"SUB");
                                    break;
                                case "RES":
                                    sendMessageToClient(message,"RES");
                                    break;
                            }
                            // '>' defines the end of the message so the result is returned
                        } else if (message.equals(">")) {
                            sendMessageToClient(message,"FIN");
                        } else {
                            System.out.println("Invalid message content!");
                            sendMessageToClient(message, "ERR");
                            return null;
                        }
                    }
                }
                return null;
            }

            private void sendMessageToClient(String content, String type) {
                if (type.equals("RES")) {
                    // 10. current calculation result is sent to client with "OK" followed by a single whitespace
                    // character, the operator "RES", another whitespace character and the current calculation value
                    out.println("OK " + "RES " + result.toString());
                } else if (calculatorInfoOperators.contains(type) || type.equals("Number")) {
                    // 9. Each valid content is acknowledged to the client with the content 'OK' followed by a single
                    // whitespace character and the valid content
                    out.println("OK " + content);
                } else if (type.equals("FIN")){
                    // 6. after processing every message content from a received message, it sends the content 'FIN' to the client
                    out.println("FIN");
                } else {
                    // invalid content is acknowledged with "ERR" followed by a single whitespace character and the invalid content
                    out.println("ERR " + content);
                }
            }
        }
    }

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
        CalcSocketServer calcSocketServer = new CalcSocketServer(32345);
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
            private List calculatorOperators = Arrays.asList("ADD", "SUB", "MUL", "RES");
            private String output;
            Integer result = 0;

            /**
             * contains the protocol
             * @param input the input message from the client
             * @return a
             */
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

                    // checks if the message length changed after the trim operation
                    Integer originalMessageLength = input.length();
                    Integer trimedMessageLength = inputUpper.length();
                    if (!(originalMessageLength.equals(trimedMessageLength))) {
                        indexOfSmallerThan = inputUpper.indexOf("<");
                        indexOfGreaterThan = inputUpper.indexOf(">");
                    }
                } else {
                    System.out.println("The message requires the two characters '<' and '>', once each!");
                    sendMessageToClient(inputUpper, "ERR");
                    return null;
                }
                // 2. checks if the two characters following '<' are the two digits indicating message length
                String digitsStr = inputUpper.substring(indexOfSmallerThan + 1,
                        indexOfSmallerThan + 3).trim();
                Integer digits;
                if (!digitsStr.matches("-?\\d+(\\.\\d+)?")) {
                    System.out.println("Message in the wrong format, needs two digits after the '<' Operator!");
                    sendMessageToClient(digitsStr, "ERR");
                    return null;
                    // checks whether the message length equals the two digits which indicate the message length
                } else {
                    digits = Integer.parseInt(digitsStr);
                    if (!digits.equals(inputUpper.length())) {
                        System.out.println("The two digits indicating message length don't match the message length!");
                        sendMessageToClient(digitsStr, "ERR");
                        return null;
                    }
                }

                // 2. get the colon after the two digits
                String colonInMessage = inputUpper.substring(indexOfSmallerThan + 3, indexOfSmallerThan + 4).trim();

                // 2. get the remaining string after the colon till the end
                String validContent = inputUpper.substring(indexOfSmallerThan + 4, indexOfGreaterThan).trim();

                // 4. split the content of the message by whitespace, for checking each remaining substring
                String[] splitMessageContent = validContent.trim().split("\\s+");

                // gets the last ">" substring
                String endOfMessage = inputUpper.substring(indexOfGreaterThan, indexOfGreaterThan + 1).trim();
                // 2. checks if a colon follows the two digits
                // 2. and 3. checks if integer values, a calculation or info operator follows the colon
                if (colonInMessage.equals(":")) {
                    Integer splitMessageLength = splitMessageContent.length;
                    if (splitMessageLength > 1) {

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
                                        // System.out.println("ADD!");
                                        break;
                                    case "SUB":
                                        result -= currentNumber;
                                        // System.out.println("SUB!");
                                        break;
                                    case "MUL":
                                        result *= currentNumber;
                                        // System.out.println("MUL!");
                                        break;
                                }
                                // 3. checks if a calculation/info operator does not follow the colon or the end of the message '>'
                            } else if (calculatorOperators.contains(message) || message.equals("MUL")
                                    || message.equals("ADD") || message.equals("SUB")) {

                                // since the calculation operators are changed from ADD to ADDing etc. but only on the server
                                // side it has to check if it already happened
                                int index = 0;
                                if (calculatorOperators.contains(message)) {
                                    index = calculatorOperators.indexOf(message);
                                    if (message.equals("RES")) {
                                        sendMessageToClient("RES", "RES");
                                    }
                                } else {
                                    switch (message) {
                                        case "ADD":
                                            index = calculatorOperators.indexOf("ADDing");
                                            break;
                                        case "SUB":
                                            index = calculatorOperators.indexOf("SUBtracting");
                                            break;
                                        case "MUL":
                                            index = calculatorOperators.indexOf("MULtiplying");
                                            break;
                                    }
                                }
                                // 7. if valid content equals 'ADD', 'SUB' or 'MUL' the calculation operator is changed
                                String currentOpCalc = (String) calculatorOperators.get(index);
                                if (currentOpCalc.equals("ADD") || currentOpCalc.equals("ADDing")) {
                                    if (!currentOpCalc.equals("ADDing")) {
                                        calculatorOperators.set(index, "ADDing");
                                    }
                                    currentCalcInfoOperator = "ADD";
                                    sendMessageToClient(message, "ADDing");
                                } else if (currentOpCalc.equals("SUB") || currentOpCalc.equals("SUBtracting")) {
                                    if (!currentOpCalc.equals("SUBtracting")) {
                                        calculatorOperators.set(index, "SUBtracting");
                                    }
                                    currentCalcInfoOperator = "SUB";
                                    sendMessageToClient(message, "SUBtracting");
                                } else if (currentOpCalc.equals("MUL") || currentOpCalc.equals("MULtiplying")) {
                                    if (!currentOpCalc.equals("MULtiplying")) {
                                        calculatorOperators.set(index, "MULtiplying");
                                    }
                                    currentCalcInfoOperator = "MUL";
                                    sendMessageToClient(message, "MULtiplying");
                                }
                            } else {
                                sendMessageToClient(message, "ERR");
                            }
                        }
                        // '>' defines the end of the message so the result is returned
                        checkForMessageEnd(endOfMessage);
                    } else if (splitMessageLength.equals(1)){
                        if (splitMessageContent[0].equals("RES")) {
                            sendMessageToClient("RES", "RES");
                            // '>' defines the end of the message so the result is returned
                            checkForMessageEnd(endOfMessage);
                        } else {
                            System.out.println("The rest of the message after the colon has to have valid content!");
                            sendMessageToClient(splitMessageContent[0], "ERR");
                            return null;
                        }
                    }
                } else {
                    System.out.println("There has to be a colon after the two digits!");
                    sendMessageToClient(colonInMessage, "ERR");
                    return null;
                }
                return null;
            }

            /**
             * checks if the message ends in the right format
             * @param endOfMessage the last string in the message to be checked
             */
            private void checkForMessageEnd(String endOfMessage) {
                if (endOfMessage.equals(">")) {
                    sendMessageToClient(endOfMessage, "FIN");
                } else {
                    System.out.println("The message has to end with '>'!");
                    sendMessageToClient(endOfMessage, "ERR");
                }
            }

            /**
             * Sends a fitting message back to the client
             * @param content the message to be sent back to the client for verification
             * @param type the calculator or info operator which determines which message to send
             */
            private void sendMessageToClient(String content, String type) {
                if (type.equals("RES")) {
                    // 10. current calculation result is sent to client with "OK" followed by a single whitespace
                    // character, the operator "RES", another whitespace character and the current calculation value
                    out.println("OK " + "RES ");
                    out.println("OK " + "RES " + result.toString());
                } else if (type.equals("FIN")){
                    // 6. after processing every message content from a received message, it sends the content 'FIN' to the client
                    out.println("FIN");
                } else if (calculatorOperators.contains(type) || type.equals("Number")) {
                    // 9. Each valid content is acknowledged to the client with the content 'OK' followed by a single
                    // whitespace character and the valid content
                    out.println("OK " + content);
                } else {
                    // invalid content is acknowledged with "ERR" followed by a single whitespace character and the invalid content
                    out.println("ERR " + content);
                }
            }
        }
    }

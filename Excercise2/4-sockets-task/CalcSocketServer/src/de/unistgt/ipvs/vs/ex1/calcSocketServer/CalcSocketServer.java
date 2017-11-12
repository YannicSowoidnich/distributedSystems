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

                Protocol protocol = new Protocol();
                while ((input = in.readLine()) != null) {
                    output = protocol.processInput(input);
                    out.println(output);
                }
            } catch (IOException e) {
                System.err.println(e);
            }
            finally {
                try {
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                }
                catch (IOException e) {
                    System.err.println(e);
                }
                System.out.println("Connection closed");
            }
        }
    }

    class Protocol {
        private List calculatorOperators = Arrays.asList("ADD", "SUB", "MUL", "RES", "RDY", "OK", "ERR", "FIN");

        public String processInput(String input) {
            String theOutput = null;

            // messages are case-insensitive, so all are upperCase
            String inputUpper = input.toUpperCase();
            Integer indexOfSmallerThan;
            Integer indexOfGreaterThan;

            // counts how often '<' and '>' occur in a message
            Integer countSmallerThan = inputUpper.length() - inputUpper.replace("<", "").length();
            Integer countGreaterThan = inputUpper.length() - inputUpper.replace(">", "").length();

            // messages are framed by two characters '<' and '>', both only allowed once in a message
            if (countSmallerThan.equals(1) && countGreaterThan.equals(1)) {
                indexOfSmallerThan = inputUpper.indexOf("<");
                indexOfGreaterThan = inputUpper.indexOf(">");

                // bytes outside a message are ignored
                inputUpper = inputUpper.substring(indexOfSmallerThan, indexOfGreaterThan + 1).trim();
            } else {
                System.out.println("The message requires the two characters '<' and '>', once each!");
                return null;
            }
            try {
                // checks if the two characters following '<' are the two digits for message length, if not -> Exception
                Integer totalMessageLength = Integer.parseInt(inputUpper.substring(indexOfSmallerThan + 1,
                        indexOfSmallerThan + 3).trim());
            } catch (NumberFormatException e) {
                System.out.println("Total message length in the wrong format, has to be with two digits after '<' Operator!");
                System.err.println(e);
                return null;
            }

            // get the character after the two digits
            String colonInMessage = inputUpper.substring(indexOfSmallerThan + 3, indexOfSmallerThan + 4).trim();

            // get the remaining string after the colon till the end
            String validContent = inputUpper.substring(indexOfSmallerThan + 4, countGreaterThan + 1).trim();

            // split the content of the message by whitespace, for checking each remaining substring
            String[] splitMessageContent = validContent.trim().split("\\s+");

            // checks if a colon follows the two digits
            // checks if integer values, a calculation or info operator follows the colon
            if (colonInMessage.equals(":") && splitMessageContent.length > 0) {
                for (String message: splitMessageContent) {
                    try {
                        Integer.parseInt(message);
                    } catch (NumberFormatException e) {
                        if (!(calculatorOperators.contains(message) || message.equals(">"))) {
                            return null;
                        }
                    }
                }
            }
            return theOutput;
        }
    }

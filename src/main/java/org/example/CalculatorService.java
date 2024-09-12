package org.example;

import java.io.*;
import java.net.*;

public class CalculatorService {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8081);
        ReflexCalculator calculator = new ReflexCalculator();

        while (true) {
            Socket socket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String request = in.readLine();
            if (request != null && !request.isEmpty()) {
                String[] parts = request.split("\\(");
                if (parts.length > 1) {
                    String command = parts[0];
                    String paramString = parts[1].replace(")", "");
                    String[] paramStrings = paramString.split(",");
                    Double[] params = new Double[paramStrings.length];
                    try {
                        for (int i = 0; i < paramStrings.length; i++) {
                            params[i] = Double.parseDouble(paramStrings[i]);
                        }
                        String result = calculator.compute(command, params);
                        out.println(result);
                    } catch (NumberFormatException e) {
                        out.println("Error: Invalid number format.");
                    }
                } else {
                    out.println("Error: Invalid request format.");
                }
            } else {
                out.println("Error: Empty request.");
            }
        }
    }
}



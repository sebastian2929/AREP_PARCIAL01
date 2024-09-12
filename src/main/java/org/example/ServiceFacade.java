package org.example;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class ServiceFacade {

    private static final String CALCULATOR_HOST = "localhost";
    private static final int CALCULATOR_PORT = 8081;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            handleRequest(clientSocket);
        }
    }

    private static void handleRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String requestLine = in.readLine();
        if (requestLine != null && requestLine.startsWith("GET")) {
            if (requestLine.startsWith("GET /calculadora")) {
                sendHtml(out);
            } else if (requestLine.startsWith("GET /computar")) {
                String queryString = requestLine.split(" ")[1];
                String command = extractCommand(queryString);
                Double[] params = extractParams(queryString);
                String result = delegateToCalculator(command, params);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println();
                out.println("{\"result\": \"" + result + "\"}");
            } else {
                out.println("HTTP/1.1 400 Bad Request");
            }
        } else {
            out.println("HTTP/1.1 400 Bad Request");
        }
        out.flush();
        clientSocket.close();
    }

    private static String delegateToCalculator(String command, Double[] params) throws IOException {
        Socket calculatorSocket = new Socket(CALCULATOR_HOST, CALCULATOR_PORT);
        PrintWriter out = new PrintWriter(calculatorSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(calculatorSocket.getInputStream()));

        out.println(command + "(" + Arrays.toString(params).replaceAll("[\\[\\] ]", "") + ")");
        String result = in.readLine();
        calculatorSocket.close();
        return result;
    }

    private static void sendHtml(PrintWriter out) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html");
        out.println();
        out.println("<!DOCTYPE html><html><head><title>Calculator</title></head><body>");
        out.println("<h1>Calculator</h1>");
        out.println("<input id='input' type='text' placeholder='Command (e.g., sin(45))'><br>");
        out.println("<button onclick='compute()'>Compute</button>");
        out.println("<div id='result'></div>");
        out.println("<script>");
        out.println("function compute() {");
        out.println("var input = document.getElementById('input').value;");
        out.println("fetch('/computar?comando=' + encodeURIComponent(input))");
        out.println(".then(response => response.json())");
        out.println(".then(data => document.getElementById('result').innerHTML = 'Result: ' + data.result);");
        out.println(".catch(error => document.getElementById('result').innerHTML = 'Error: ' + error.message);");
        out.println("}");
        out.println("</script>");
        out.println("</body></html>");
    }

    private static String extractCommand(String queryString) {
        return queryString.split("comando=")[1].split("\\(")[0];
    }

    private static Double[] extractParams(String queryString) {
        if (queryString.contains("(") && queryString.contains(")")) {
            String paramString = queryString.split("\\(")[1].split("\\)")[0];
            String[] paramStrings = paramString.split(",");
            Double[] params = new Double[paramStrings.length];
            try {
                for (int i = 0; i < paramStrings.length; i++) {
                    params[i] = Double.parseDouble(paramStrings[i]);
                }
            } catch (NumberFormatException e) {
                params = new Double[0];  // Empty array if there's an error in format
            }
            return params;
        } else {
// Return an empty array if no parameters are present
            return new Double[0];
        }
    }
}
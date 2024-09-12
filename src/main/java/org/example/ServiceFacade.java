package org.example;

import java.io.*;
import java.net.*;
import java.util.Arrays;

/**
 * Created by sebatian blanco.
 */
public class ServiceFacade {

    private static final String CALCULATOR_HOST = "localhost";
    private static final int CALCULATOR_PORT = 8081;
    private static final String RESOURCE_DIR = "src/main/resources/static";

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
            if (requestLine.startsWith("GET /")) {
                String path = requestLine.split(" ")[1];
                if (path.equals("/")) {
                    path = "/index.html";  // Default to index.html if no file is specified
                }
                if (path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js")) {
                    sendStaticFile(out, path);
                } else if (path.startsWith("/computar")) {
                    String queryString = requestLine.split(" ")[1];
                    String command = extractCommand(queryString);
                    Double[] params = extractParams(queryString);
                    String result = delegateToCalculator(command, params);
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: application/json");
                    out.println();
                    out.println("{\"result\": \"" + result + "\"}");
                } else {
                    out.println("HTTP/1.1 404 Not Found");
                    out.println();
                    out.println("404 Not Found");
                }
            } else {
                out.println("HTTP/1.1 400 Bad Request");
            }
        } else {
            out.println("HTTP/1.1 400 Bad Request");
        }
        out.flush();
        clientSocket.close();
    }


    private static void sendStaticFile(PrintWriter out, String path) throws IOException {
        File file = new File("src/main/resources/static", path.substring(1));
        if (file.exists() && !file.isDirectory()) {
            String contentType = determineContentType(path);
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: " + contentType);
            out.println();
            try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = fileReader.readLine()) != null) {
                    out.println(line);
                }
            }
        } else {
            out.println("HTTP/1.1 404 Not Found");
            out.println();
            out.println("404 Not Found");
        }
    }

    private static String determineContentType(String path) {
        if (path.endsWith(".css")) {
            return "text/css";
        } else if (path.endsWith(".js")) {
            return "application/javascript";
        } else if (path.endsWith(".html")) {
            return "text/html";
        } else {
            return "text/plain";
        }
    }

    private static String delegateToCalculator(String command, Double[] params) throws IOException {
        try (Socket calculatorSocket = new Socket(CALCULATOR_HOST, CALCULATOR_PORT);
             PrintWriter out = new PrintWriter(calculatorSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(calculatorSocket.getInputStream()))) {

            out.println(command + "(" + Arrays.toString(params).replaceAll("[\\[\\] ]", "") + ")");
            return in.readLine();
        } catch (IOException e) {
            return "Error: Unable to connect to calculator service.";
        }
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
                params = new Double[0];
            }
            return params;
        } else {
            return new Double[0];
        }
    }
}

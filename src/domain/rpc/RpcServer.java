package domain.rpc;

import java.io.*;
import java.net.*;
import java.util.*;

public class RpcServer {
    private final int port;
    private final CalculatorService service;

    public RpcServer(int port, CalculatorService service) {
        this.port = port;
        this.service = service;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[RPC] Server listening on port " + port);
            while (true) {
                Socket client = serverSocket.accept();
                //Debug
                System.out.println("\n[REQUERIMIENTO] Nueva conexión desde: " + client.getInetAddress());
                new Thread(() -> handleClient(client)).start();
            }
        }
    }

    private void handleClient(Socket client) {
        try (client;
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {
            
            String line = in.readLine();
            if (line == null || line.isBlank()) return;
            
             //Debug
            System.out.println("[RECIBIDO] Hola: " + line);

            Map<String, String> req = RpcProtocol.parseLine(line);
            String id = req.getOrDefault("id", "no-id");
            String method = req.get("method");
            String params = req.getOrDefault("params", "");
            //Prozzzsando debug
            System.out.println("[PROCESANDO] ID: " + id + " | Método: " + method + " | Parámetros: [" + params + "]");

            String response;
            try {
                int result = dispatch(method, params);
                response = RpcProtocol.buildResponse(id, true, String.valueOf(result), null);
                System.out.println("[EXITO] Resultado calculado: " + result);
            } catch (Exception e) {
                response = RpcProtocol.buildResponse(id, false, null, e.getMessage());
                System.out.println("[ERROR] Error en dispatch: " + e.getMessage());
            }

            out.write(response);
            out.newLine();
            out.flush();
            System.out.println("[RESPUESTA] Enviada al cliente.");

        } catch (IOException e) {
            System.out.println("[RPC] Client error: " + e.getMessage());
        }
    }

    private int dispatch(String method, String params) {
        if (method == null) throw new IllegalArgumentException("Missing method");
        switch (method) {
            case "add": {
                int[] p = parseInts(params, 2);
                return service.add(p[0], p[1]);
            }
            case "square": {
                int[] p = parseInts(params, 1);
                return service.square(p[0]);
            }
            default:
                throw new IllegalArgumentException("Unknown method: " + method);
        }
    }

    private int[] parseInts(String csv, int expected) {
        String[] parts = csv.split(",");
        if (parts.length != expected) throw new IllegalArgumentException("Expected " + expected + " params");
        int[] out = new int[expected];
        for (int i = 0; i < expected; i++) out[i] = Integer.parseInt(parts[i].trim());
        return out;
    }

    public static void main(String[] args) throws Exception {
        new RpcServer(5000, new CalculatorServiceImpl()).start();
    }
}
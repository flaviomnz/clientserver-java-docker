package Servidor;
import java.io.*;
import java.net.*;
import java.util.*;

public class server1 {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;

    public server1(int port) {
        clients = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(port);
            // Obter o endereço IP da máquina local
            InetAddress localAddress = InetAddress.getLocalHost();
            System.out.println("Servidor iniciado em " + localAddress.getHostAddress() + ":" + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Usuário conectou: " + clientSocket);

                // Criar um novo handler para o cliente
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);

                // Iniciar uma thread para o cliente
                Thread clientThread = new Thread(handler);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Classe interna para lidar com cada cliente
    class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;
        private PrintWriter output;
        private String clientAddress;

        public ClientHandler(Socket socket) {
            try {
                clientSocket = socket;
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                output = new PrintWriter(clientSocket.getOutputStream(), true);

                // Obter o endereço IP do cliente
                clientAddress = clientSocket.getInetAddress().getHostAddress();

                // Informar a conexão ao servidor
                System.out.println("Uma nova conexão de: " + clientAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String message;
            try {
                while ((message = input.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit")) {
                        break;
                    } else if (message.startsWith("!private")) {
                        handlePrivateMessage(message);
                    } else {
                        // Enviar a mensagem recebida para todos os clientes
                        broadcastMessage(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("O usuário " + clientAddress + " encerrou a conexão.");
                    clients.remove(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Método para enviar mensagem para todos os clientes
        public void broadcastMessage(String message) {
            for (ClientHandler client : clients) {
                client.output.println(clientAddress + ": " + message);
            }
        }

        // Método para lidar com mensagens privadas
        private void handlePrivateMessage(String message) {
            String[] parts = message.split("\\s+", 3); // Divide a mensagem em partes (comando, IP_destino, mensagem)
            if (parts.length >= 3) {
                String ipAddress = parts[1];
                String privateMessage = parts[2];
                sendPrivateMessage(ipAddress, privateMessage);
            } else {
                System.out.println("Erro. Use: !private <IP_destino> <mensagem>");
            }
        }

        // Método para enviar mensagem privada para um cliente específico
        private void sendPrivateMessage(String ipAddress, String message) {
            for (ClientHandler client : clients) {
                if (client.clientAddress.equals(ipAddress)) {
                    client.output.println("(Private) " + clientAddress + ": " + message);
                    return;
                }
            }
            // Caso o IP_destino não seja encontrado
            output.println("Cliente não encontrado para o IP especificado: " + ipAddress);
        }
    }

    public static void main(String[] args) {
        int port = 8080; // Porta do servidor
        new server1(port);
    }
}

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
            InetAddress localAddress = InetAddress.getLocalHost();
            System.out.println("Servidor iniciado em " + localAddress.getHostAddress() + ":" + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Usuário conectou: " + clientSocket);

                // responsável por gerenciar a comunicação com um usuário/client específico.
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);

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

    // classe responsável para lidar com o usuário
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

        // método q envia a mensagem para todos
        public void broadcastMessage(String message) {
            for (ClientHandler client : clients) {
                client.output.println(clientAddress + ": " + message);
            }
        }

        // tratar mensagens privadas
        private void handlePrivateMessage(String message) {
            String[] parts = message.split("\\s+", 3); 
            if (parts.length >= 3) {
                String ipAddress = parts[1];
                String privateMessage = parts[2];
                sendPrivateMessage(ipAddress, privateMessage);
            } else {
                System.out.println("Erro. Use: !private <IP_destino> <mensagem>");
            }
        }

        // método para enviar mensagem privada para um usuário específico
        private void sendPrivateMessage(String ipAddress, String message) {
            for (ClientHandler client : clients) {
                if (client.clientAddress.equals(ipAddress)) {
                    client.output.println("(Private) " + clientAddress + ": " + message);
                    return;
                }
            }
          
            output.println("Cliente não encontrado para o IP especificado: " + ipAddress);
        }
    }

    public static void main(String[] args) {
        int port = 8080; // Porta do servidor
        new server1(port);
    }
}

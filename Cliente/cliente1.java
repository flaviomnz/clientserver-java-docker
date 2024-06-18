package Cliente;
import java.io.*;
import java.net.*;

public class cliente1 {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String clientAddress; // Endereço IP do cliente

    public cliente1(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            input = new BufferedReader(new InputStreamReader(System.in));
            output = new PrintWriter(socket.getOutputStream(), true);
            clientAddress = socket.getLocalAddress().getHostAddress(); // Obtém o endereço IP do cliente
            System.out.println("Conectado ao servidor.");
            System.out.println("Para mandar mensagem para um usuário, digite !private (ip_do_user) (msg). ");
            System.out.println("Para sair do CHAT, digite: 'exit' ");
            // Thread para receber mensagens do servidor
            Thread receiveThread = new Thread(new ReceiveMessage());
            receiveThread.start();

            // Loop para enviar mensagens para o servidor
            String message;
            while (true) {
                message = input.readLine();
                if (message.equalsIgnoreCase("exit")) {
                    break;
                } else if (message.startsWith("!private")) {
                    handlePrivateMessage(message);
                } else {
                    output.println(message);
                }
            }

            // Fechar recursos ao sair
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para lidar com mensagens privadas
    private void handlePrivateMessage(String message) {
        String[] parts = message.split("\\s+", 3); // Divide a mensagem em partes (comando, IP_destino, mensagem)
        if (parts.length >= 3) {
            String ipAddress = parts[1];
            String privateMessage = parts[2];
            output.println("!private " + ipAddress + " " + privateMessage); // Envia a mensagem privada diretamente
        } else {
            System.out.println("Erro! Use: !private <IP_destino> <mensagem>");
        }
    }

    // Classe interna para receber mensagens do servidor
    class ReceiveMessage implements Runnable {
        private BufferedReader serverInput;

        public ReceiveMessage() throws IOException {
            serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = serverInput.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

        try {
            // Solicitar ao usuário o endereço IP do servidor
            System.out.print("Digite o endereço IP: ");
            String serverAddress = consoleInput.readLine();

            // Solicitar ao usuário a porta do servidor
            System.out.print("Agora, digite a porta: ");
            int port = Integer.parseInt(consoleInput.readLine());

            // Criar o cliente com as informações fornecidas pelo usuário
            new cliente1(serverAddress, port);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                consoleInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
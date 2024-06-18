package Cliente;
import java.io.*;
import java.net.*;

public class cliente1 {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String clientAddress; 

    public cliente1(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            input = new BufferedReader(new InputStreamReader(System.in));
            output = new PrintWriter(socket.getOutputStream(), true);
            clientAddress = socket.getLocalAddress().getHostAddress(); // Obtém o endereço IP do cliente
            System.out.println("Conectado ao servidor.");
            System.out.println("Para mandar mensagem para um usuário, digite !private (ip_container) (msg). ");
            System.out.println("Para sair do CHAT, digite: 'exit' ");
            // Thread para receber mensagens do servidor
            Thread receiveThread = new Thread(new ReceiveMessage());
            receiveThread.start();

            // loop para enviar mensagens para o servidor
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

           
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // método para responsavel para lidar com as mensagens privadas.
    private void handlePrivateMessage(String message) {
        String[] parts = message.split("\\s+", 3); // divide a mensagem em partes (comando, IP_destino, mensagem)
        if (parts.length >= 3) {
            String ipAddress = parts[1];
            String privateMessage = parts[2];
            output.println("!private " + ipAddress + " " + privateMessage); 
        } else {
            System.out.println("Erro! Use: !private <ip_destino> <mensagem>");
        }
    }

    // classe que recebe mensagens do server
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
            System.out.print("Digite o endereço IP: ");
            String serverAddress = consoleInput.readLine();

            System.out.print("Agora, digite a porta: ");
            int port = Integer.parseInt(consoleInput.readLine());

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
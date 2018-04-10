package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerCristian extends Thread {

    private final ServerSocket serverSocket;
    private long timeRecv;  //O tempo em receber mensagem do cliente
    private long timeSend;  //O momento em que envia mensagem ao cliente

    public ServerCristian(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (true) {
            try {
                //Saída do nome do servidor
                String localHostName = java.net.InetAddress.getLocalHost().getHostName();
                System.out.println("Nome do Servidor: " + localHostName);

                System.out.println("Esperado cliente na porta " +
                        serverSocket.getLocalPort() + "...");

                //Aceite uma conexão de clientes
                Socket server = serverSocket.accept();
                System.out.println("Conectado em: " + server.getRemoteSocketAddress());

                //Receber mensagem de clientes
                DataInputStream in = new DataInputStream(server.getInputStream());
                timeRecv = System.currentTimeMillis();
                System.out.println(in.readUTF());

                //Enviar mensagem de volta aos clientes
                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                long time_on_server = System.currentTimeMillis();
                timeSend = System.currentTimeMillis();
                out.writeLong(time_on_server);  //Envia o tempo total no servidor de volta ao cliente
                out.writeLong(timeSend);        //Envia o tempo de envio para o cliente

                //Fecha a conexão
                server.close();
            } catch (SocketTimeoutException s) {
                System.out.println("Socket expirou!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String [] args) {
        int port = 9092;
        try {
            Thread t = new ServerCristian(port);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
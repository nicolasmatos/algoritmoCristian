import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Servidor extends Thread {

    private final ServerSocket servidorSocket;
    private long tempoRecv;  //O tempo em receber mensagem do cliente
    private long tempoEnvio;  //O momento em que envia mensagem ao cliente

    public Servidor(int port) throws IOException {
        servidorSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (true) {
            try {
                //Saída do nome do servidor
                String nomeHost = java.net.InetAddress.getLocalHost().getHostName();
                System.out.println("--------------------------------------------");
                System.out.println("Nome do Servidor: " + nomeHost);

                System.out.println("Esperado cliente na porta " + servidorSocket.getLocalPort() + "...");

                //Aceite uma conexão de clientes
                Socket server = servidorSocket.accept();
                System.out.println("Conectado em: " + server.getRemoteSocketAddress());

                //Receber mensagem de clientes
                DataInputStream in = new DataInputStream(server.getInputStream());
                tempoRecv = System.currentTimeMillis();
                System.out.println(in.readUTF());

                //Enviar mensagem de volta aos clientes
                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                long tempoServidor = System.currentTimeMillis();
                tempoEnvio = System.currentTimeMillis();
                out.writeLong(tempoServidor);  //Envia o tempo total no servidor de volta ao cliente
                out.writeLong(tempoEnvio);     //Envia o tempo de envio para o cliente

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
        int porta = 9092;
        try {
            Thread t = new Servidor(porta);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
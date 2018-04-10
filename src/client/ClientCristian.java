package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientCristian {

    private String serverName;
    private int serverPort;
    private static int count;   //O número de conexões
    private Timer timer;        //Este temporizador é para enviar o pedido ao servidor a cada 6 segundos
    private PrintWriter pr;     //Para escrever em arquivo
    private long t0;            //O momento em que envia o pedido ao servidor
    private long t3;            //O momento em que recebe a resposta do servidor

    // Constructor
    public ClientCristian(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
        ClientCristian.count = 0;
        this.timer = new Timer();
        try {
            //Caminho onde será salvo o arquivo com os tempos dos clientes
            this.pr = new PrintWriter("C:\\Dev\\Ideia\\algoritmoCristian\\ClientTest.txt", "UTF-8");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    class Conversation extends TimerTask {
        //Função de execução Overide em TimerTask
        @Override
        public void run() {
            //Número de cliente que vão fazer a sincronização
            if (count < 5) {
                try {
                    System.out.println("Conectando a ... " + serverName + " na porta " + serverPort);

                    //Conecta ao servidor
                    Socket client = new Socket(serverName, serverPort);
                    System.out.println("Conectado a " + client.getRemoteSocketAddress());

                    //Envia mensagem para o servidor
                    OutputStream outToServer = client.getOutputStream();
                    DataOutputStream out = new DataOutputStream(outToServer);
                    t0 = System.currentTimeMillis();
                    out.writeUTF("Olá de " + client.getLocalSocketAddress());

                    //Recebe mensagem do servidor
                    InputStream inFromServer = client.getInputStream();
                    DataInputStream in = new DataInputStream(inFromServer);
                    long t1 = in.readLong();   //Recebe o tempo total no servidor
                    long t2 = in.readLong();   //Recebe o tempo de envio no servidor
                    t3 = System.currentTimeMillis();

                    //Fecha a conexão
                    client.close();

                    count ++;

                    //Definir tempos de atraso para simular os atrasos de solicitação / resposta
                    t1 += 100;
                    t2 += 150;
                    t3 += 250;

                    //Obtém o RTT (tempo de atraso de ida e volta)
                    long rtt = (t3 - t0) - (t2 - t1);

                    pr.println("###########################################");
                    pr.println("Tempo Cliente Envio: " + t0);
                    pr.println("Tempo do Servidor Recebimento: " + t1);
                    pr.println("Tempo do Servidor Envio: " + t2);
                    pr.println("Tempo Cliente Recebimento: " + t3);

                    pr.println("*** RTT ***");
                    pr.println("a -> (t3 - t0) = " + (t3 - t0));
                    pr.println("b -> (t2 - t1) = " + (t2 - t1));

                    pr.println("*** RTT divido por 2 ***");
                    pr.println("(a-b)/2 =  " + rtt / 2);

                    //RTT Offset
                    long theta = (t1 - t0) + (t2 - t3 ) / 2;
                    pr.println("*** RTT Offset ***");
                    pr.println("Theta -> (t1 - t0) + (t2 - t3 ) / 2 = " + theta);


                    long cristianTime = t2 + (rtt / 2);
                    long cristianTimeComOffset = t2 + (theta);
                    pr.println("*** Horario de Cristian ***");
                    pr.println("Horario de Cristian -> t2 + (rtt/2): " + cristianTime);
                    pr.println("Horario de Cristian -> t2 + (rtt_offset): " + cristianTimeComOffset);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                pr.close(); //Libera o arquivo
                timer.cancel();
                timer.purge();
            }
        }
    }

    public static void main(String [] args) {

        //Nome do servidor
        String serverName = "localhost";

        //Porta do servidor
        int serverPort = 9092;

        //Cria um cliente que vai conecar no servidor
        ClientCristian client = new ClientCristian(serverName, serverPort);

        //Tempo que o objeto Timer vai fazer as conexoes
        long period = 6000;

        //Instancia a classe Conversation
        ClientCristian.Conversation  conversation = client.new Conversation();

        client.timer.schedule(conversation, 0, period);
    }
}
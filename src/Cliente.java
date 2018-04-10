import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Cliente {

    private String nomeServidor;
    private int portaServidor;
    private static int count;   //O número de conexões
    private Timer temporizador;        //Este temporizador é para enviar o pedido ao servidor a cada 6 segundos
    private PrintWriter pr;     //Para escrever em arquivo
    private long t0;            //O momento em que envia o pedido ao servidor
    private long t3;            //O momento em que recebe a resposta do servidor

    //Construtor
    public Cliente(String nomeServidor, int portaServidor) {
        this.nomeServidor = nomeServidor;
        this.portaServidor = portaServidor;
        Cliente.count = 0;
        this.temporizador = new Timer();
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
                    System.out.println("--------------------------------------------");
                    System.out.println("Conectando a ... " + nomeServidor + " na porta " + portaServidor);

                    //Conecta ao servidor
                    Socket cliente = new Socket(nomeServidor, portaServidor);
                    System.out.println("Conectado a " + cliente.getRemoteSocketAddress());

                    //Envia mensagem para o servidor
                    OutputStream outToServer = cliente.getOutputStream();
                    DataOutputStream out = new DataOutputStream(outToServer);
                    t0 = System.currentTimeMillis();
                    out.writeUTF("Olá de " + cliente.getLocalSocketAddress());

                    //Recebe mensagem do servidor
                    InputStream inFromServer = cliente.getInputStream();
                    DataInputStream in = new DataInputStream(inFromServer);
                    long t1 = in.readLong();   //Recebe o tempo total no servidor
                    long t2 = in.readLong();   //Recebe o tempo de envio no servidor
                    t3 = System.currentTimeMillis();

                    //Fecha a conexão
                    cliente.close();

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

                    //Imprimindo no console
                    System.out.println("\nTempo Cliente Envio: " + formataData(t0));
                    System.out.println("Tempo do Servidor Recebimento: " + formataData(t1));
                    System.out.println("Tempo do Servidor Envio: " + formataData(t2));
                    System.out.println("Tempo Cliente Recebimento: " + formataData(t3));

                    /*
                    System.out.println("*** RTT ***");
                    System.out.println("a -> (t3 - t0) = " + (t3 - t0));
                    System.out.println("b -> (t2 - t1) = " + (t2 - t1));

                    System.out.println("*** RTT divido por 2 ***");
                    System.out.println("(a-b)/2 =  " + rtt / 2);

                    System.out.println("*** RTT Offset ***");
                    System.out.println("Theta -> (t1 - t0) + (t2 - t3 ) / 2 = " + theta);

                    System.out.println("*** Horario de Cristian ***");*/

                    System.out.println("Horario de Cristian -> t2 + (rtt/2): " + formataData(cristianTime));
                    System.out.println("Horario de Cristian -> t2 + (rtt_offset): " + formataData(cristianTimeComOffset));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                pr.close(); //Libera o arquivo
                temporizador.cancel();
                temporizador.purge();
            }
        }
    }

    public static void main(String [] args) {

        //Nome do servidor
        String nomeServidor = "Localhost";

        //Porta do servidor
        int portaServidor = 9092;

        //Cria um cliente que vai conecar no servidor
        Cliente cliente = new Cliente(nomeServidor, portaServidor);

        //Tempo que o objeto Timer vai fazer as conexoes
        long periodo = 6000;

        //Instancia a classe Conversation
        Cliente.Conversation  conversation = cliente.new Conversation();

        cliente.temporizador.schedule(conversation, 0, periodo);
    }

    public String formataData(long data) {
        Timestamp timeStamp = new Timestamp(data);
        Date date = new Date(timeStamp.getTime());

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        String dataFormatada = formato.format(date);

        return dataFormatada;
    }
}
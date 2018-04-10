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
            this.pr = new PrintWriter("C:\\Dev\\Ideia\\algoritmoCristian\\LogClientes.txt", "UTF-8");
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
                    t0 = System.currentTimeMillis();
                    OutputStream outToServer = cliente.getOutputStream();
                    DataOutputStream out = new DataOutputStream(outToServer);
                    out.writeUTF("Solicito a hora" + cliente.getLocalSocketAddress());

                    //Recebe mensagem do servidor
                    InputStream inFromServer = cliente.getInputStream();
                    DataInputStream in = new DataInputStream(inFromServer);
                    long t1 = in.readLong();   //Recebe o quando o servidor recebeu a mensagem
                    long ts = in.readLong();   //Recebe o tempo do servidor
                    long t2 = in.readLong();   //Recebe o tempo de envio no servidor
                    t3 = System.currentTimeMillis();

                    //Fecha a conexão
                    cliente.close();

                    count ++;

                    //Definir tempos de atraso para simular os atrasos de solicitação / resposta
                    //t1 += 500;
                    //ts += 900;
                    t2 += 1500; //Simulando um atraso na resposta do servidor
                    t3 += 2000; //Simulando um atraso no recebimento da resposta do servidor para o cliente

                    //Cálculo do algoitmo de Cristian
                    long tiv = t3 - t0; //Tempo que levou no cliente, desde sua requisição ao servidor, até o recebimento da resposta do servidor
                    long tm2 = t3 - t2; //Tempo que levou para mensagem de resposta do servidor, chegar no cliente
                    long tm1 = t1 - t0; //Tempo que levou para mensagem de requisição do cliente, chegar no servidor

                    //Formula do slide do algortimo de Cristian
                    long tc = ts + ((tiv + tm2 - tm1)/2);

                    //Salvando os dados no arquivo
                    pr.println("----------------------------------------------------");
                    pr.println("Tempo envio Cliente: " + formataData(t0));
                    pr.println("Tempo recebimento Servidor: " + formataData(t1));
                    pr.println("Tempo envio Servidor: " + formataData(t2));
                    pr.println("Tempo recebimento Cliente: " + formataData(t3));

                    pr.println("\nTempo marcado no servidor: " + formataData(ts));
                    pr.println("Tempo Algoritmo de Cristian: " + formataData(tc));

                    //Imprimindo no console
                    System.out.println("\nTempo envio Cliente: " + formataData(t0));
                    System.out.println("Tempo recebimento Servidor: " + formataData(t1));
                    System.out.println("Tempo envio Servidor: " + formataData(t2));
                    System.out.println("Tempo recebimento Cliente: " + formataData(t3));

                    System.out.println("\nTempo marcado no servidor: " + formataData(ts));
                    System.out.println("Tempo Algoritmo de Cristian: " + formataData(tc));

                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
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
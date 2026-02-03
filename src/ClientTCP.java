//ClientTCP.java
import java.io.*;
import java.net.*;
import java.util.Scanner;

/** class ClientTCP
 *  Gestisce la connessione lato utente verso un server remoto.
 *  Permette l'invio di messaggi tramite console e riceve le risposte dal server.
 **/
public class ClientTCP {
    private Socket mySocket;
	private String serverAddress;
	private Scanner inDalServer;
	private PrintWriter outVersoServer;
	private int porta;

    /** ClientTCP(String serverAddress, int porta)
     *  COSTRUTTORE
     *  param <- serverAddress: l'indirizzo IP o l'hostname del server a cui connettersi.
     *  param <- porta: la porta TCP su cui il server è in ascolto.
     **/
    public ClientTCP(String serverAddress, int porta) {
		this.serverAddress = serverAddress;
		this.porta = porta;
	}

    /** connetti()
     *  Tenta di stabilire una connessione socket con il server specificato.
     *  Inizializza i flussi di input e output per la comunicazione.
     *  param <- nessuno.
     *  output -> inizializza mySocket, inDalServer e outVersoServer.
     **/
    public void connetti() {
		try {
			mySocket = new Socket(serverAddress, porta);
			System.out.println("Connesso al server");

			inDalServer = new Scanner(mySocket.getInputStream());
			outVersoServer = new PrintWriter(mySocket.getOutputStream(), true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /** comunica()
     *  Gestisce la comunicazione bidirezionale asincrona.
     *  Utilizza un thread secondario per l'ascolto continuo dei messaggi dal server,
     *  param <- nessuno (utilizza System.in internamente).
     *  output -> invia stringhe al server e stampa le risposte ricevute.
     **/
    public void comunica() {
        //---THREAD DI ASCOLTO---
        Thread ascoltatore = new Thread(() -> {
            try {
                while (inDalServer.hasNextLine()) {
                    String msg = inDalServer.nextLine();
                    System.out.println("\n[SERVER]: " + msg);
                    System.out.print("> ");
                }
            } catch (Exception e) {
                System.out.println("Connessione con il server interrotta.");
            }
        });
        ascoltatore.setDaemon(true);
        ascoltatore.start();

        //---THREAD PRINCIPALE---
        Scanner scan = new Scanner(System.in);
        String userInput;
        
        System.out.println("---Sessione avviata---");
        do {
            System.out.print("> ");
            userInput = scan.nextLine();
            outVersoServer.println(userInput);
        } while (!userInput.equalsIgnoreCase("exit"));

        scan.close();
    }

    /** chiusura()
     *  Chiude in modo sicuro i flussi di comunicazione e il socket del client.
     *  param <- nessuno.
     *  output -> rilascia le risorse di rete locali.
     **/
    public void chiusura() {
		try {
			inDalServer.close();
			outVersoServer.close();
			mySocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


    /** main(String[] args)
     *  Punto di ingresso dell'applicazione lato Client.
     *  Nota: Per testare localmente, sostituire "SERVER-IP-ADDRESS-LOCAL" con "localhost".
     **/
    public static void main(String[] args) throws IOException {
		ClientTCP cliente = new ClientTCP("SERVER-IP-ADDRESS-LOCAL", 1234);
		cliente.connetti();
		cliente.comunica();
		cliente.chiusura();
	}
}

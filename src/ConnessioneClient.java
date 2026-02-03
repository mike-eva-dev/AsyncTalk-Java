//ConnessioneClient.java
import java.io.*;
import java.net.*;
import java.util.Scanner;

/** class ConnessioneClient extends Thread
 *  Gestisce la logica di comunicazione per un singolo Client.
 *  estente Thread per permettere la gestione asincrona di molteplici connessioni simultanee.
 **/
public class ConnessioneClient extends Thread {
    private Socket client;
	private ServerTCP server;
	private Scanner inDalClient;
	private PrintWriter outVersoClient;
	private String nomeUtente;

    /** ConnessioneClient(Socket socket, ServerTCP server)
     *  COSTRUTTORE
     *  param <- socket: il socket specifico del client.
     *  param <- server: riferimento al server padre per la gestione dello stato.
     **/
    public ConnessioneClient(Socket socket, ServerTCP server) {
		this.client = socket;
		this.server = server;
	}

	/** registrazione()
 	 *  Fase iniziale: obbliga l'utente a scegliere un nickname univoco.
 	 **/
	public void registrazione() {
		boolean registrato = false;
		while (!registrato) {
			outVersoClient.println("Inserisci il tuo nome utente: ");
			if(inDalClient.hasNextLine()) {
				String nomeScelto = inDalClient.nextLine().trim();
				if(nomeScelto.isEmpty()) {
					outVersoClient.println("Il nome non può essere vuoto! riprova.");
				} else if (server.aggiungiClient(nomeScelto, this)) {
					this.nomeUtente = nomeScelto;
					registrato = true;
					outVersoClient.println("Benvenuto " + nomeUtente + "! Ora puoi messaggiare.");
				} else {
					outVersoClient.println("Nome già occupato, riprova.");
				}
			}
		}
	}

    /** getClient()
     *  GETTER di 'client'
     *  output -> restituisce l'oggetto Socket del client.
     **/
    public Socket getClient() {
		return client;
	}

    /** run()
     *  Metodo core, eseguito all'avvio del Thread.
     *  Gestisce l'intero ciclo di vita della comunicazione.
     **/
    @Override
	public void run() {
		preparazione();
		registrazione();
		risposta();
		chiusura();
		server.chiudiConnessione(this.nomeUtente);
	}

    /** preparazione()
     *  Inizializza i flussi di comunicazione (InputStream e OutputStream).
     *  param <- nessuno.
     *  output -> prepara lo Scanner e il PrintWriter per lo scambio dati.
     **/
    public void preparazione() {
		try {
			inDalClient = new Scanner(client.getInputStream());
			outVersoClient = new PrintWriter(client.getOutputStream(), true);
		} catch (Exception e) {
			System.out.println("Errore nella preparazione della connessione");
			chiusura();
		}
	}

    /** risposta()
     *  Ciclo di ascolto: legge i messaggi in entrata e invia una risposta (Echo).
     *  param <- nessuno (ascolta dal socket).
     *  output -> stampa i log sul server e invia stringhe di risposta al client.
     **/
    public void risposta() {
		try {
			while (inDalClient.hasNextLine()) {
				String inputLine = inDalClient.nextLine(); //INPUT dal Client
				System.out.println("LOG [" + client.getInetAddress() + "]: " + inputLine);
				if (inputLine.isEmpty()) continue;

                //---LOGICA DI RISPOSTA---
                String[] parti = inputLine.split(":", 2);
				String comando = parti[0].toLowerCase();
				switch(comando) {
					case "help":
						mostraHelp();
						break;
					case "who":
						outVersoClient.println("Utenti connessi: " + server.getListaUtenti());
						break;
					case "exit":
						outVersoClient.println("Connessione in chiusura... Arrivederci!");
						return;
					case "broadcast":
						if (parti.length > 1) {
							server.inviaATutti("[" + nomeUtente + "]:" + parti[1], nomeUtente);
						} else {
							outVersoClient.println("ERRORE: Devi scrivere qualcosa dopo 'broadcast:'");
						}
						break;
					default:
						if (comando.startsWith("whisper")) {
							gestisciWhisper(inputLine);
						} else {
							outVersoClient.println("Comando non riconosciuto. Usa 'help:' per la lista.");
						}
						break;
				}
			}
			
		} catch (Exception e) {
			System.out.println("Errore durante la comunicazione con il client");
			chiusura();
		}
	}
	//whisper leonardo: ciao, come stai?
	public void gestisciWhisper(String input) {
		try {
			String dopoWhisper = input.substring(input.indexOf(" ") + 1).trim();
			String[] parti = dopoWhisper.split(":", 2);

			if (parti.length < 2) {
				outVersoClient.println("ERRORE: Formato errato. Usa: whisper nome: messaggio");
				return;
			}

			String destinatario = parti[0].trim();
			String messaggio = parti[1].trim();
			server.inviaPrivato(destinatario, messaggio, this.nomeUtente);
		} catch (Exception e) {
			outVersoClient.println("ERRORE nel comando whisper.");
		}
	}

	/** inviaMessaggio(String messaggio)
	 *  Mostra la lista dei comandi.
	 **/
	public void mostraHelp() {
		outVersoClient.println("\n--- COMANDI DISPONIBILI ---");
        outVersoClient.println("help:             			: Mostra questa lista");
        outVersoClient.println("exit:             			: Chiude la connessione");
		outVersoClient.println("who:              			: Mostra gli utenti online");
        outVersoClient.println("broadcast: [msg] 			: Invia a tutti");
        outVersoClient.println("whisper nomeutente: [msg]    : Messaggio privato");
        outVersoClient.println("---------------------------\n");
	}

	/** inviaMessaggio(String messaggio)
	 *  Metodo chiamato dal Server per inviare dati verso questo specifico client.
	 **/
	public void inviaMessaggio(String messaggio) {
		if (outVersoClient != null) {
			outVersoClient.println(messaggio);
		}
	}

	/** getNomeUtente()
	 *  GETTER di nomeUtente
	 **/
	public String getNomeUtente() {
		return nomeUtente;
	}

    /** chiusura()
     *  Rilascia le risorse chiudendo i flussi e il socket individuale del client.
     *  param <- nessuno.
     *  output -> chiude gli oggetti I/O e termina il socket.
     **/
    public void chiusura() {
		try {
			if (inDalClient != null)
				inDalClient.close();
			if (outVersoClient != null)
				outVersoClient.close();
			if (client != null)
				client.close();
		} catch (Exception e) {
			System.out.println("Errore nella chiusura della connessione");
		}
	}
}
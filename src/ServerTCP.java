//ServerTCP.java
import java.net.*;
import java.util.HashMap;

/** class ServerTCP
 *  Rappresenta il Server principale che rimane in ascolto sulla porta specificata.
 *  Gestisce un elenco di client attivi e delega la comunicazione ai Thread.
 **/
public class ServerTCP {
    private int porta;
	private ServerSocket server;
	private HashMap<String, ConnessioneClient> mappaClient;

    /** ServerTCP(int porta)
     *  COSTRUTTORE
     *  param <- porta: il numero della porta su cui il server si metterà in ascolto.
     **/
	public ServerTCP(int porta) {
		this.porta = porta;
		this.mappaClient = new HashMap<>();
	}

    /** attendi()
     *  Inizializza il ServerSocket e avvia il ciclo infinito di accettazione client.
     *  param <- nessuno.
     *  output -> per ogni connessione riuscita, istanzia e avvia un nuovo Thread.
     **/
    public void attendi() {
		try {
			server = new ServerSocket(porta);
			System.out.println("Server avviato e in attesa di connessioni...");

			while (true) {
				Socket client = server.accept();
				System.out.println("Connessione accettata da " + client.getInetAddress());

				ConnessioneClient clientThread = new ConnessioneClient(client, this);
				clientThread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** aggiungiClient(String nome, ConnessioneClient threadClient)
	 *  Registra il client nella mappa se il nome è disponibile.
	 *  param <- nome: il nickname scelto dall'utente.
	 *  param <- threadClient: l'istanza del thread che lo gestisce.
	 *  output -> true se aggiunto con successo, false se il nome esiste già.
	 **/
	public synchronized boolean aggiungiClient(String nome, ConnessioneClient threadClient) {
		if (mappaClient.containsKey(nome)) {
        	return false;
    	}
		mappaClient.put(nome, threadClient);
		return true;
	}

    /** chiudiConnessione(ConnessioneClient connessione)
     *  Rimuove in modo sicuro un client dalla lista di quelli attualmente connessi.
     *  L'uso di 'synchronized' previene conflitti tra thread diversi.
     *  param <- connessione: l'istanza del thread da rimuovere dalla lista.
     **/
	public synchronized void chiudiConnessione(String nome) {
		if (nome != null) {
			mappaClient.remove(nome);
			System.out.println("Utente [" + nome + "] rimosso dalla rubrica.");
		}
	}

	/** getListaUtenti()
	 *  Recupera i nomi di tutti gli utenti attualmente registrati nella HashMap.
	 *  L'uso di 'synchronized' garantisce che la lista sia letta correttamente
	 *  anche se altri thread si stanno connettendo o disconnettendo.
	 *  output -> Una stringa formattata con l'elenco dei nickname (es. "[Marco, Luca, Anna]").
	 **/
	public synchronized String getListaUtenti() {
		if (mappaClient.isEmpty()) {
			return "Nessun utente online.";
		}
		return mappaClient.keySet().toString();
	}

	/** inviaATutti(String messaggio, String mittente)
 	 *  Spedisce un messaggio a tutti i client connessi.
 	 *  param <- messaggio: il testo da distribuire.
 	 *  param <- mittente: il nome di chi ha inviato il messaggio.
 	 **/
	public synchronized void inviaATutti(String messaggio, String mittente) {
		for(ConnessioneClient client : mappaClient.values()) {
			if(client.getNomeUtente() != null && !client.getNomeUtente().equals(mittente)) {
				client.inviaMessaggio(messaggio);
			}
		}
		System.out.println("BROADCAST da " + mittente + ": " + messaggio);
	}

	/** inviaPrivato(String dest, String mes, String nome)
 	 *  Spedisce un messaggio al client desiderato dal mittente.
 	 *  param <- dest: a chi deve arrivare il messaggio.
	 *  param <- mes: il testo da distribuire.
 	 *  param <- nome: il nome di chi ha inviato il messaggio.
 	 **/
	public synchronized void inviaPrivato(String dest, String mes, String nome) {
		ConnessioneClient target = mappaClient.get(dest);
		ConnessioneClient mittente = mappaClient.get(nome);

		if (target != null) {
			target.inviaMessaggio("[Sussurro da " + nome + "]: " + mes);
			mittente.inviaMessaggio("[Hai sussurrato a " + dest + "]: " + mes);
		} else {
			mittente.inviaMessaggio("ERRORE: L'utente '" + dest + "' non è online o il nome è errato.");
		}
	}

    /** chiusura()
     *  Termina il server e chiude tutte le connessioni client ancora aperte.
     *  param <- nessuno.
     *  output -> arresta il server e libera la porta occupata.
     **/
    public void chiusura() {
		try {
			for (ConnessioneClient client : mappaClient.values()) {
				client.chiusura();
			}
			mappaClient.clear();
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
    	}
	}

    /** main(String[] args)
     *  Punto di ingresso dell'applicazione lato Server.
     **/
    public static void main(String[] args) {
		ServerTCP s = new ServerTCP(1234);
		s.attendi();
		s.chiusura();
	}
}

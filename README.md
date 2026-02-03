# Multi-User Async Chat System (Java TCP/IP)

Un sistema di messaggistica client-server multithread sviluppato in Java, che implementa un protocollo di comunicazione personalizzato su socket TCP. Il sistema permette la gestione di più utenti simultanei, messaggistica broadcast e messaggi privati (whisper).

---

## 🚀 Caratteristiche Principali

* **Architettura Multithreaded**: Gestione parallela delle connessioni tramite thread dedicati per ogni client (`ConnessioneClient`).
* **Comunicazione Asincrona**: Il client utilizza un thread di ascolto separato per ricevere messaggi in tempo reale senza bloccare l'input dell'utente.
* **Protocollo Command-Based**: Parsing dei messaggi basato su una sintassi personalizzata (es. `comando: messaggio`).
* **Gestione Stato Server**: Utilizzo di `HashMap` sincronizzate per mappare i nickname alle sessioni attive, garantendo thread-safety.
* **Handshake & Registrazione**: Fase di validazione del nickname univoco prima dell'accesso alla chat.

---

## 🛠️ Architettura Tecnica

Il progetto è strutturato secondo il paradigma client-server e sfrutta i Socket Java per la comunicazione a basso livello.



### 1. ServerTCP
Agisce come orchestratore centrale. Accetta le connessioni in entrata e mantiene la "rubrica" degli utenti connessi tramite una `HashMap<String, ConnessioneClient>`.
* **Metodi chiave**: `inviaATutti()` (Broadcast), `inviaPrivato()` (Whisper).
* **Thread Safety**: Utilizzo di metodi `synchronized` per prevenire *race conditions* durante la manipolazione della mappa client.

### 2. ConnessioneClient (Thread)
Rappresenta la logica di business per ogni singolo utente connesso. Estendendo la classe `Thread`, permette al server di non bloccarsi durante l'interazione con i singoli client.
* **Parsing**: Implementa uno `switch-case` per interpretare i comandi inviati dal client tramite il separatore `:`.
* **Lifecycle**: Gestisce la preparazione dei flussi I/O (`Scanner` e `PrintWriter`), la registrazione del nickname, la messaggistica e la chiusura pulita delle risorse.

### 3. ClientTCP
L'interfaccia lato utente, progettata per essere totalmente reattiva.
* **Dual-Thread**: Un thread principale gestisce l'input da tastiera (`System.in`), mentre un thread secondario (ascoltatore) rimane in attesa di messaggi dal server per stamparli immediatamente a video.

---

## ⌨️ Comandi Disponibili

Una volta connesso, l'utente può interagire con il server utilizzando la seguente sintassi:

| Comando | Descrizione |
| :--- | :--- |
| `help:` | Mostra la lista dei comandi disponibili. |
| `who:` | Mostra l'elenco dei nickname attualmente online. |
| `broadcast: [messaggio]` | Invia un messaggio a tutti gli utenti connessi. |
| `whisper [utente]: [messaggio]` | Invia un messaggio privato a un utente specifico. |
| `exit:` | Chiude la connessione in modo sicuro e rimuove l'utente dal server. |

---

## 💻 Requisiti e Esecuzione

### Requisiti
* **Java Development Kit (JDK)** 8 o superiore.

### Istruzioni
1.  **Compilazione**:
    ```bash
    javac *.java
    ```
2.  **Avvio Server**:
    ```bash
    java ServerTCP
    ```
3.  **Avvio Client** (aprire terminali multipli per testare la chat):
    ```bash
    java ClientTCP
    ```

---

## 🧠 Skill Dimostrate
* **Networking**: Socket Programming (TCP/IP).
* **Concurrency**: Multithreading e gestione dei Thread Daemon.
* **Collections**: Utilizzo avanzato di `HashMap` e `KeySet`.
* **Logic**: String Manipulation, Parsing e gestione dei flussi di I/O.
* **Clean Code**: Programmazione orientata agli oggetti (OOP) e incapsulamento.

---
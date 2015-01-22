import java.util.*;
import java.io.*;
import java.net.*;

/**
 * G³ówna klasa serwera. Zapewnia poprawne zainicjowanie jego pracy. Serwer
 * zapisuje dane loguj¹cych siê u¿ytkowników - numer identyfikacyjny oraz
 * gniazdo. Pozwala na przesy³anie danych miêdzy u¿ytkownikami na podstawie
 * jedynie ich numeru.
 * 
 * @author GRUPA 1AS
 * 
 */
public class Server {

	// -------POLA---------------------------------------------------------------------

	/** port serwera s³u¿¹cy do oczekiwania na próby po³¹czeñ, domyœlnie 20000 */
	private static int serverPort = 20000;

	/** zbiór zalogowanych uzytkowników */
	private static UsersCollection loggedUsers;

	/**
	 * zbiór u¿ywanych numerów sesji, które s¹ niepowtarzalne i przydziela je
	 * serwer
	 */
	private static Vector<Integer> sessionIds = new Vector<Integer>();

	// -------MAIN----------------------------------------------------------------------
	/**
	 * G³ówna metoda aplikacji.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		getConfiguration();

		loggedUsers = new UsersCollection();

		listening(serverPort);
	}

	// -------METODY--------------------------------------------------------------
	/**
	 * Metoda obs³uguj¹ca przyjmowanie zg³oszeñ klientów przez serwer.
	 * 
	 * @param listeningPort
	 *            port, na którym nas³uchiwane s¹ po³¹czenia
	 */
	private static void listening(int listeningPort) {

		try {
			ServerSocket serverSocket = new ServerSocket(listeningPort);

			while (true) {
				try {
					ListeningThread clientListeningThread;
					clientListeningThread = new ListeningThread(
							serverSocket.accept(), sessionIds, loggedUsers);
					clientListeningThread.start();

				} catch (IOException e) {
					System.out.println("B³¹d po³¹czenia z klientem");
				}
			}
			// TODO zamykanie serverSocket
			// TODO wylogowywanie u¿ytkowników
			// TODO zabezpieczenie przed wielokrotnym logowaniem

		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
	}

	/**
	 * Metoda s³u¿y do pobrania konfiguracji serwera z pliku.
	 */
	private static void getConfiguration() {

		try {

			File file = new File("config.conf");
			Scanner in = new Scanner(file);

			serverPort = Integer.parseInt(in.nextLine());

			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("Problem z pobraniem konfiguracji \n"
					+ "ustawiam domyœlny port nas³uchiwania: 20000");
			serverPort = 20000;
		}
	}
}
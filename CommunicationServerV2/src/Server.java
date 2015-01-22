import java.util.*;
import java.io.*;
import java.net.*;

/**
 * G��wna klasa serwera. Zapewnia poprawne zainicjowanie jego pracy. Serwer
 * zapisuje dane loguj�cych si� u�ytkownik�w - numer identyfikacyjny oraz
 * gniazdo. Pozwala na przesy�anie danych mi�dzy u�ytkownikami na podstawie
 * jedynie ich numeru.
 * 
 * @author GRUPA 1AS
 * 
 */
public class Server {

	// -------POLA---------------------------------------------------------------------

	/** port serwera s�u��cy do oczekiwania na pr�by po��cze�, domy�lnie 20000 */
	private static int serverPort = 20000;

	/** zbi�r zalogowanych uzytkownik�w */
	private static UsersCollection loggedUsers;

	/**
	 * zbi�r u�ywanych numer�w sesji, kt�re s� niepowtarzalne i przydziela je
	 * serwer
	 */
	private static Vector<Integer> sessionIds = new Vector<Integer>();

	// -------MAIN----------------------------------------------------------------------
	/**
	 * G��wna metoda aplikacji.
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
	 * Metoda obs�uguj�ca przyjmowanie zg�osze� klient�w przez serwer.
	 * 
	 * @param listeningPort
	 *            port, na kt�rym nas�uchiwane s� po��czenia
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
					System.out.println("B��d po��czenia z klientem");
				}
			}
			// TODO zamykanie serverSocket
			// TODO wylogowywanie u�ytkownik�w
			// TODO zabezpieczenie przed wielokrotnym logowaniem

		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
	}

	/**
	 * Metoda s�u�y do pobrania konfiguracji serwera z pliku.
	 */
	private static void getConfiguration() {

		try {

			File file = new File("config.conf");
			Scanner in = new Scanner(file);

			serverPort = Integer.parseInt(in.nextLine());

			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("Problem z pobraniem konfiguracji \n"
					+ "ustawiam domy�lny port nas�uchiwania: 20000");
			serverPort = 20000;
		}
	}
}
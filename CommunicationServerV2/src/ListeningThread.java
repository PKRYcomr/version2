import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Obiekt tej klasy obs�uguje w oddzielnym w�tku pojedynczego klienta. Odpowiada
 * na zapytania klienta oraz wysy�a do niego komunikaty. Obiekt ten NIE inicjuje
 * komunikacji z klientem.
 * 
 * @author GRUPA 1AS
 * 
 */
public class ListeningThread extends Thread {

	// -------POLA--------------------------------------------------------------------------------------------------------

	// dla u�atwienia nie opakowuj� userNumber oraz clientSocket w obiekt User
	/** 6-cyfrowy numer u�ytkownika, kt�rego obs�uguje bie��cy w�tek */
	private int userNumber;

	/** gniazdo, z kt�rym bie��cy w�tek jest po��czony */
	private Socket clientSocket;

	/** pula u�ytkownik�w obs�ugiwanych w ca�ym serwerze */
	private UsersCollection loggedUsers;

	/**
	 * zbi�r u�ywanych numer�w sesji, kt�re s� niepowtarzalne i przydziela je
	 * serwer
	 */
	private Vector<Integer> sessionIds;

	// -------KONSTRUKTORY---------------------------------------------------------------------------------------------------------
	/**
	 * Jedyny konstruktor w�tku obs�uguj�cego klienta.
	 * 
	 * @param clientSocket
	 *            gniazdo, odpowiadaj�ce po��czeniu tego w�tku
	 * @param loggedUsers
	 *            zbi�r wszystkich aktualnie zalogowanych u�ytkownik�w
	 */
	public ListeningThread(Socket clientSocket, Vector<Integer> sessionIds,
			UsersCollection loggedUsers) {

		this.clientSocket = clientSocket;
		this.loggedUsers = loggedUsers;
		this.sessionIds = sessionIds;

		getUserNumber();

		// TODO przed zamkni�ciem gniazda usun�� z zalogowanych uzytkownik�w
	}

	// -------METODY-------------------------------------------------------

	public void run() {

		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));

			while (true) {

				String request;

				request = in.readLine(); // zawieszenie

				System.out.println("User " + userNumber + " -> Server | "
						+ request);

				// wykonanie zapytania
				int result = executeRequest(request);
				if (result == -1 || result == 2) {
					in.close();
					clientSocket.close();
					break;
				}
			}

			// TODO zamykanie BufferedReader
		} catch (IOException e) {
		//	e.printStackTrace();

			System.out.println("Klient zerwal " + userNumber + " sesje");
			int index = loggedUsers.getUserIndex(userNumber);
			
			loggedUsers.remove(index);
		}

	}

	/**
	 * Metoda analizuj�ca zapytania klient�w. Po dopasowaniu do wzorca zapytania
	 * wykonuje niezb�dne operacje.
	 * 
	 * Wykaz zapyta�: SET NUMBER userNumber - przypisanie numeru uzytkownika
	 * userNumber dla tego po��czenia; INIT clientNumber1 clientNumber2 ... -
	 * wys�anie do wszystkich wymienionych klient�w informacji o ch�ci
	 * rozpocz�cia nowej sesji z u�ytkownikami o numerach: number1, number2....;
	 * CLIENT recipientNumber sessionId PARAM message szyfrowana niesymetrycznie
	 * wiadomo�� o parametrach protoko�u CLIENT recipientNumber sessionId READY
	 * message informacja o gotowo�ci do rozpocz�cia protoko�u KeyAgreement
	 * CLIENT recipientNumber sessionId MESS message wysy�ana szyfrowana
	 * wiadomo�� do u�ytkownik�w
	 * 
	 * @param request
	 *            zapytanie klienta
	 */
	private int executeRequest(String request) {

		int number;
		User user;

		if (request != null) {
			String[] requestElements = request.split(" ");

			switch (requestElements[0]) {

			case "SET":
				switch (requestElements[1]) {
				case "NUMBER":
					// sprawdzenie czy taki ju� nie istnieje
					for(int i = 0; i < loggedUsers.size();i++) {
						if (loggedUsers.get(i).getUserNumber() == Integer.parseInt(requestElements[2])) {
							sendToClient("THIS USER IS LOGGED IN, CONNECTION ABORTED");
							// niepoprawnie
							return -1;
						}						
					}
					setUserNumber(requestElements);
					sendToClient("VERIFIED");
					// poprawne zako�czenie
					return 1;
			//		break;
				}
				break;

			case "INIT":
				initSession(requestElements);
				return 1;
			//	break;

			case "CLIENT":
				number = Integer.parseInt(requestElements[1]);
				user = loggedUsers.getUser(number);

				sendToClient(request, user);
				return 1;
			//	break;
				
			case "LOGOUT":
				// usuni�cie z listy zalogowanych u�ytkownik�w
				int index = loggedUsers.getUserIndex(userNumber);
												
				loggedUsers.remove(index);
				// poprawne zako�czenie ale trzeba pozamyk� strumieie i roz��czy�
				return 2;
		//		break;
			}
		}
		// brak dopasowania
		return 0;
	}

	/**
	 * Metoda wywo�ywana przy inicjalizacji nowej sesji przez zapytanie klienta.
	 * Sprawdza, czy wszyscy u�ytkownicy s� zalogowani. Je�li nie, wysy�ana jest
	 * wiadomo�� zwrotna do inicjuj�cego klienta o niepoprawnych numerach
	 * u�ytkownik�w. Je�li wszystkie numery nale�� do zalogowanych u�ytkownik�w,
	 * metoda losuje unikatowy identyfikator sesji, a nast�pnie wysy�a do
	 * u�ytkownik�w podanych w zapytaniu polecenie stworzenia nowej sesji wraz z
	 * wylosowanym identyfikatorem.
	 * 
	 * @param request
	 *            zapytanie klienta
	 */
	private void initSession(String[] requestElements) {

		// sprawdzenie czy wszyscy potencjalni uczestnicy s�
		// zalogowani
		UsersCollection currentUsers = new UsersCollection();

		// u�ytkownik inicjuj�cy sesj� musi nale�e� do zbioru u�ytkownik�w we
		// wiadomo�ci
		for (int i = 1; i < requestElements.length; i++) {

			int userNumber = Integer.parseInt(requestElements[i]);
			if (userNumber == this.userNumber)
				break;
			else if (i == requestElements.length - 1) {
				// je�li kto� pr�buje zainicjowa� sesj� bez uczestnictwa w niej
				sendToClient("LACK OF PERMISSION TO CREATE SESSION");
				return;
			}
		}

		// numery musz� nale�e� do zalogowanych uczestnik�w
		for (int i = 1; i < requestElements.length; i++) {

			int userNumber = Integer.parseInt(requestElements[i]);
			User user = loggedUsers.getUser(userNumber);

			if (user != null) {
				currentUsers.add(user);
			} else {
				// je�li, wykryty zostanie numer niezalogowanego u�ytkownika
				sendToClient("BAD NUMBERS INIT BROKEN");
				return;
			}
		}

		// ----- wylosowanie niepowtarzalnego identyfikatora sesji-------------
		Random rand = new Random();
		int identify;
		while (true) {

			// losowane 8-cyfrowe ID sesji
			int probableIdentify = rand.nextInt(9999);

			// sprawdzanie, czy taki identyfikator sesji zosta� ju� u�yty
			boolean usedId = false;
			for (int i = 0; i < sessionIds.size(); i++) {
				if (sessionIds.get(i).equals(probableIdentify)) {
					usedId = true;
					break;
				}
			}

			if (!usedId) {
				identify = probableIdentify;
				break;
			}
		}
		sessionIds.add(identify);

		// ---------- multicast wiadomo�ci INIT -------------
		// je�li wszyscy zadani uzytkownicy sa zalogowani, rozsy�ana jest
		// polecenie inicjalizacji
		String message = "INIT " + identify;
		for (int i = 1; i < requestElements.length; i++) {
			message = message + " " + requestElements[i];
		}

		for (int i = 0; i < currentUsers.size(); i++) {
			sendToClient(message, currentUsers.get(i));
		}

	}

	// ---------- METODY WYSY�AJ�CE KOMUNIKATY---------------------------
	/**
	 * Metoda wysy�a zapytanie do klienta o jegu numer. Jest to uproszczona
	 * procedura logowania na serwer.
	 */
	private void getUserNumber() {

		sendToClient("GET NUMBER");
		// TODO

		// je�li serwer nie mo�e si� dowiedzie� jaki numer ma klient
		// gniazdo musi byc zamkni�te zapytania klienta b�d� odrzucane
	}

	// ----------METODY SET OBS�UGUJ�CE ZAPYTANIA------------------
	/**
	 * Metoda przypisuje temu po��czeniu 8-cyfrowy numer u�ytkownika na
	 * podstawie odpowiedzi klienta oraz dodaje tego u�ytkownika do listy
	 * zalogowanych. Jest to uproszczony schemat logowania na serwer - bez
	 * potwierdzania autentyczno�ci przez funkcj� skr�tu.
	 * 
	 * @param requestElements
	 *            elementy zapytania klienta
	 */
	private void setUserNumber(String[] requestElements) {

		userNumber = Integer.parseInt(requestElements[2]);

		User user = new User(clientSocket, userNumber);
		loggedUsers.add(user);
	}

	// -----------METODY SIECIOWE---------------------------------------
	/**
	 * Metoda wysy�a wiadomo�� do klieta, z kt�rym po��czony jest bie��cy w�tek.
	 * 
	 * @param message
	 *            wiadomo��, kt�ra ma by� wys�ana
	 */
	private void sendToClient(String message) {

		PrintWriter out;
		try {
			out = new PrintWriter(clientSocket.getOutputStream(), true);

			out.println(message);

			System.out
					.println("Server -> User " + userNumber + " | " + message);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Metoda wysy�a wiadomo�� do klieta, okre�lonego gniazdem.
	 * 
	 * @param message
	 *            wiadomo��
	 * @param clientSocket
	 *            gniazdo klienta
	 */
	private void sendToClient(String message, User user) {

		PrintWriter out;
		try {
			out = new PrintWriter(user.getUserSocket().getOutputStream(), true);

			out.println(message);

			System.out.println("Server -> User " + user.getUserNumber() + " | "
					+ message);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			// je�li serwer nie mo�e si� dowiedzie� jaki numer ma klient
			// gniazdo musi byc zamkni�te zapytania klienta b�d� odrzucane
		}
	}
}
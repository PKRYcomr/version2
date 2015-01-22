import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Obiekt tej klasy obs³uguje w oddzielnym w¹tku pojedynczego klienta. Odpowiada
 * na zapytania klienta oraz wysy³a do niego komunikaty. Obiekt ten NIE inicjuje
 * komunikacji z klientem.
 * 
 * @author GRUPA 1AS
 * 
 */
public class ListeningThread extends Thread {

	// -------POLA--------------------------------------------------------------------------------------------------------

	// dla u³atwienia nie opakowujê userNumber oraz clientSocket w obiekt User
	/** 6-cyfrowy numer u¿ytkownika, którego obs³uguje bie¿¹cy w¹tek */
	private int userNumber;

	/** gniazdo, z którym bie¿¹cy w¹tek jest po³¹czony */
	private Socket clientSocket;

	/** pula u¿ytkowników obs³ugiwanych w ca³ym serwerze */
	private UsersCollection loggedUsers;

	/**
	 * zbiór u¿ywanych numerów sesji, które s¹ niepowtarzalne i przydziela je
	 * serwer
	 */
	private Vector<Integer> sessionIds;

	// -------KONSTRUKTORY---------------------------------------------------------------------------------------------------------
	/**
	 * Jedyny konstruktor w¹tku obs³uguj¹cego klienta.
	 * 
	 * @param clientSocket
	 *            gniazdo, odpowiadaj¹ce po³¹czeniu tego w¹tku
	 * @param loggedUsers
	 *            zbiór wszystkich aktualnie zalogowanych u¿ytkowników
	 */
	public ListeningThread(Socket clientSocket, Vector<Integer> sessionIds,
			UsersCollection loggedUsers) {

		this.clientSocket = clientSocket;
		this.loggedUsers = loggedUsers;
		this.sessionIds = sessionIds;

		getUserNumber();

		// TODO przed zamkniêciem gniazda usun¹æ z zalogowanych uzytkowników
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
	 * Metoda analizuj¹ca zapytania klientów. Po dopasowaniu do wzorca zapytania
	 * wykonuje niezbêdne operacje.
	 * 
	 * Wykaz zapytañ: SET NUMBER userNumber - przypisanie numeru uzytkownika
	 * userNumber dla tego po³¹czenia; INIT clientNumber1 clientNumber2 ... -
	 * wys³anie do wszystkich wymienionych klientów informacji o chêci
	 * rozpoczêcia nowej sesji z u¿ytkownikami o numerach: number1, number2....;
	 * CLIENT recipientNumber sessionId PARAM message szyfrowana niesymetrycznie
	 * wiadomoœæ o parametrach protoko³u CLIENT recipientNumber sessionId READY
	 * message informacja o gotowoœci do rozpoczêcia protoko³u KeyAgreement
	 * CLIENT recipientNumber sessionId MESS message wysy³ana szyfrowana
	 * wiadomoœæ do u¿ytkowników
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
					// sprawdzenie czy taki ju¿ nie istnieje
					for(int i = 0; i < loggedUsers.size();i++) {
						if (loggedUsers.get(i).getUserNumber() == Integer.parseInt(requestElements[2])) {
							sendToClient("THIS USER IS LOGGED IN, CONNECTION ABORTED");
							// niepoprawnie
							return -1;
						}						
					}
					setUserNumber(requestElements);
					sendToClient("VERIFIED");
					// poprawne zakoñczenie
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
				// usuniêcie z listy zalogowanych u¿ytkowników
				int index = loggedUsers.getUserIndex(userNumber);
												
				loggedUsers.remove(index);
				// poprawne zakoñczenie ale trzeba pozamykæ strumieie i roz³¹czyæ
				return 2;
		//		break;
			}
		}
		// brak dopasowania
		return 0;
	}

	/**
	 * Metoda wywo³ywana przy inicjalizacji nowej sesji przez zapytanie klienta.
	 * Sprawdza, czy wszyscy u¿ytkownicy s¹ zalogowani. Jeœli nie, wysy³ana jest
	 * wiadomoœæ zwrotna do inicjuj¹cego klienta o niepoprawnych numerach
	 * u¿ytkowników. Jeœli wszystkie numery nale¿¹ do zalogowanych u¿ytkowników,
	 * metoda losuje unikatowy identyfikator sesji, a nastêpnie wysy³a do
	 * u¿ytkowników podanych w zapytaniu polecenie stworzenia nowej sesji wraz z
	 * wylosowanym identyfikatorem.
	 * 
	 * @param request
	 *            zapytanie klienta
	 */
	private void initSession(String[] requestElements) {

		// sprawdzenie czy wszyscy potencjalni uczestnicy s¹
		// zalogowani
		UsersCollection currentUsers = new UsersCollection();

		// u¿ytkownik inicjuj¹cy sesjê musi nale¿eæ do zbioru u¿ytkowników we
		// wiadomoœci
		for (int i = 1; i < requestElements.length; i++) {

			int userNumber = Integer.parseInt(requestElements[i]);
			if (userNumber == this.userNumber)
				break;
			else if (i == requestElements.length - 1) {
				// jeœli ktoœ próbuje zainicjowaæ sesjê bez uczestnictwa w niej
				sendToClient("LACK OF PERMISSION TO CREATE SESSION");
				return;
			}
		}

		// numery musz¹ nale¿eæ do zalogowanych uczestników
		for (int i = 1; i < requestElements.length; i++) {

			int userNumber = Integer.parseInt(requestElements[i]);
			User user = loggedUsers.getUser(userNumber);

			if (user != null) {
				currentUsers.add(user);
			} else {
				// jeœli, wykryty zostanie numer niezalogowanego u¿ytkownika
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

			// sprawdzanie, czy taki identyfikator sesji zosta³ ju¿ u¿yty
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

		// ---------- multicast wiadomoœci INIT -------------
		// jeœli wszyscy zadani uzytkownicy sa zalogowani, rozsy³ana jest
		// polecenie inicjalizacji
		String message = "INIT " + identify;
		for (int i = 1; i < requestElements.length; i++) {
			message = message + " " + requestElements[i];
		}

		for (int i = 0; i < currentUsers.size(); i++) {
			sendToClient(message, currentUsers.get(i));
		}

	}

	// ---------- METODY WYSY£AJ¥CE KOMUNIKATY---------------------------
	/**
	 * Metoda wysy³a zapytanie do klienta o jegu numer. Jest to uproszczona
	 * procedura logowania na serwer.
	 */
	private void getUserNumber() {

		sendToClient("GET NUMBER");
		// TODO

		// jeœli serwer nie mo¿e siê dowiedzieæ jaki numer ma klient
		// gniazdo musi byc zamkniête zapytania klienta bêd¹ odrzucane
	}

	// ----------METODY SET OBS£UGUJ¥CE ZAPYTANIA------------------
	/**
	 * Metoda przypisuje temu po³¹czeniu 8-cyfrowy numer u¿ytkownika na
	 * podstawie odpowiedzi klienta oraz dodaje tego u¿ytkownika do listy
	 * zalogowanych. Jest to uproszczony schemat logowania na serwer - bez
	 * potwierdzania autentycznoœci przez funkcjê skrótu.
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
	 * Metoda wysy³a wiadomoœæ do klieta, z którym po³¹czony jest bie¿¹cy w¹tek.
	 * 
	 * @param message
	 *            wiadomoœæ, która ma byæ wys³ana
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
	 * Metoda wysy³a wiadomoœæ do klieta, okreœlonego gniazdem.
	 * 
	 * @param message
	 *            wiadomoœæ
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

			// jeœli serwer nie mo¿e siê dowiedzieæ jaki numer ma klient
			// gniazdo musi byc zamkniête zapytania klienta bêd¹ odrzucane
		}
	}
}
import java.net.*;

/**
 * Klasa ułatwiająca gromadzenie informacji o pojedynczym użytkowniku.
 * 
 * @author GRUPA 1AS
 * 
 */
public class User {

	// -------POLA----------------------------------------------------------------

	/** numer użytkownika */
	private int userNumber;

	/** gniazdo użytkownika */
	private Socket clientSocket;

	// -------KONSTRUKTORY------------------------------------------------------------

	public User(Socket userSocket, int userNumber) {

		this.userNumber = userNumber;
		this.clientSocket = userSocket;
	}

	// -------METODY--------------------------------------------------------------

	public int getUserNumber() {

		return userNumber;
	}

	public Socket getUserSocket() {

		return clientSocket;
	}

	public String toString() {

		return userNumber + " " + clientSocket.getInetAddress() + ":"
				+ clientSocket.getPort();
	}
}
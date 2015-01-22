import java.net.*;

/**
 * Klasa u³atwiaj¹ca gromadzenie informacji o pojedynczym u¿ytkowniku.
 * 
 * @author GRUPA 1AS
 * 
 */
public class User {

	// -------POLA----------------------------------------------------------------

	/** numer u¿ytkownika */
	private int userNumber;

	/** gniazdo u¿ytkownika */
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
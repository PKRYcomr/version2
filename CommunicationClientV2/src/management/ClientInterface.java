package management;

import java.io.*;
import java.math.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import network.Receiver;

/**
 * Klasa, która jest interfejsem aplikacji, a wiêc mo¿e byæ u¿yta jako ³¹cznik
 * miêdzy GUI, a logik¹.
 * 
 * @author GRUPA 1AS
 * 
 */
public class ClientInterface extends Thread {

	private ClientData data;

	// ------------KONSTRUKTORY------------------------------------------

	public ClientInterface() {

		this.data = ClientData.getInstance();
	}

	// ------------METODY---------------------------------------
	public void run() {

		while (true) {

			String command = getCommand();

			if (command != null)
				if (command.equals("exit"))
					break;
				else
					executeCommand(command);
		}
	}

	// metoda tymczasowa do obs³ugi aplikacji z konsoli
	private String getCommand() {

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));

			String command = in.readLine(); // zawieszenie
			return command;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Metoda wykonuj¹ca rozkazy interfejsu graficznego lub konsolowego.
	 * 
	 * @param command
	 *            komenda
	 */
	private void executeCommand(String command) {

		System.out.println("Command: " + command);
		String[] commandElements = command.split(" ");

		switch (commandElements[0]) {

		case "INIT":
			initialization(command);
			break;

		case "MESS":
			sendsCipheredMessage(command);
			break;

		case "LOGIN":
			connectWithServer();
			break;
		case "LOGOUT":
			sendToServer("LOGOUT");
			data.setLogged(false);
			data.resetSessionSet();
			break;
		}

	}

	/**
	 * Metoda wysy³a wiadomoœæ na serwer wyminy danych
	 * 
	 * @param message
	 *            wysy³ana wiadomoœæ
	 */
	private void sendToServer(String message) {

		PrintWriter out;
		try {
			out = new PrintWriter(data.getLocalSocket().getOutputStream(), true);

			out.println(message);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// ----- METODY INTERFEJSU---------------------------------

	/**
	 * Metoda zajmuje siê inicjalizacj¹ sesji. Wysy³a na serwer odpowiedni¹
	 * komendê
	 * 
	 * @param command
	 *            komenda z konsoli
	 */
	private void initialization(String command) {

		sendToServer(command);
	}

	/**
	 * Metoda szyfruje wiadomoœæ, któr¹ u¿ytkownik chce wys³aæ, a nastêpnie j¹
	 * rozsy³a do wszystkich uczestników format komendy:
	 * "MESS sessionId message"
	 * 
	 * @param command
	 *            komenda z konsoli
	 */
	private void sendsCipheredMessage(String command) {

		// format komendy: "MESS sessionId message"
		String[] comElements = command.split(" ");

		String message = "";
		for (int i = 2; i < comElements.length; i++)
			message = message + " " + comElements[i];
			
		int sessionId = Integer.parseInt(comElements[1]);
		Session session = data.getSessionSet().getSession(sessionId);

		BigInteger privateKey = session.getKap().getSessionKey();
		byte[] keyBytes = privateKey.toByteArray();

		SecretKeyFactory sf;
		try {
			// szyfrowanie wiadomoœci
			sf = SecretKeyFactory.getInstance("DES");

			byte[] messageBytes = message.getBytes("UTF-8");

			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE,
					sf.generateSecret(new DESKeySpec(keyBytes)));

			byte[] cipheredBytes = cipher.doFinal(messageBytes);
			System.out.println("SZYFROGRAM  |" + new BigInteger(cipheredBytes));

			// wysy³anie wiadomoœci do reszty
			int[] recipientNumbers = session.getKap().getTopology()
					.getParticipantsNumbers(data.getUserNumber());
			for (int i = 0; i < recipientNumbers.length; i++) {

				String completeMessage = "CLIENT " + recipientNumbers[i] + " "
						+ sessionId + " MESS " + new BigInteger(cipheredBytes);

				sendToServer(completeMessage);
			}

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Metoda ³¹czy klienta z serwerem i uruchamia nowy w¹tek do odbierania
	 * danych z serwera.
	 */
	private void connectWithServer() {

		if (data.isLogged() == false) {
			loadNumber();
			try {

				Socket localSocket = new Socket(data.getRemoteAddress(),
						data.getRemotePort());
				data.setLocalSocket(localSocket);

				new Receiver().start();
				
				data.setLogged(true);

			} catch (IOException e) {
				System.out.println(e.toString());
			}
		} else {
			System.out
					.println("Mo¿liwe jest tylko jednokrotne zalogowanie z jednej aplikacji klienta");
		}
	}
	
	// prawdopodobnie tymczasowa metoda
		private  void loadNumber() {

			while (true) {
				try {
					System.out.print("Podaj numer u¿ytkownika: ");
					BufferedReader in = new BufferedReader(new InputStreamReader(
							System.in));

					int number = Integer.parseInt(in.readLine());

					if (number < 1)
						throw (new IOException());

					data.setUserNumber(number);
					break;
				} catch (IOException e) {
					System.out.println("B³êdny numer, spróbuj ponownie");
				}
			}
		}
}
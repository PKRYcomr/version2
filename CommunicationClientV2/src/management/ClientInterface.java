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
 * Klasa, kt�ra jest interfejsem aplikacji, a wi�c mo�e by� u�yta jako ��cznik
 * mi�dzy GUI, a logik�.
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

	// metoda tymczasowa do obs�ugi aplikacji z konsoli
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
	 * Metoda wykonuj�ca rozkazy interfejsu graficznego lub konsolowego.
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
	 * Metoda wysy�a wiadomo�� na serwer wyminy danych
	 * 
	 * @param message
	 *            wysy�ana wiadomo��
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
	 * Metoda zajmuje si� inicjalizacj� sesji. Wysy�a na serwer odpowiedni�
	 * komend�
	 * 
	 * @param command
	 *            komenda z konsoli
	 */
	private void initialization(String command) {

		sendToServer(command);
	}

	/**
	 * Metoda szyfruje wiadomo��, kt�r� u�ytkownik chce wys�a�, a nast�pnie j�
	 * rozsy�a do wszystkich uczestnik�w format komendy:
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
			// szyfrowanie wiadomo�ci
			sf = SecretKeyFactory.getInstance("DES");

			byte[] messageBytes = message.getBytes("UTF-8");

			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE,
					sf.generateSecret(new DESKeySpec(keyBytes)));

			byte[] cipheredBytes = cipher.doFinal(messageBytes);
			System.out.println("SZYFROGRAM  |" + new BigInteger(cipheredBytes));

			// wysy�anie wiadomo�ci do reszty
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
	 * Metoda ��czy klienta z serwerem i uruchamia nowy w�tek do odbierania
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
					.println("Mo�liwe jest tylko jednokrotne zalogowanie z jednej aplikacji klienta");
		}
	}
	
	// prawdopodobnie tymczasowa metoda
		private  void loadNumber() {

			while (true) {
				try {
					System.out.print("Podaj numer u�ytkownika: ");
					BufferedReader in = new BufferedReader(new InputStreamReader(
							System.in));

					int number = Integer.parseInt(in.readLine());

					if (number < 1)
						throw (new IOException());

					data.setUserNumber(number);
					break;
				} catch (IOException e) {
					System.out.println("B��dny numer, spr�buj ponownie");
				}
			}
		}
}
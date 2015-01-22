import java.io.*;
import java.net.*;
import java.security.*;
import java.security.interfaces.*;
import java.util.*;

import management.*;
import network.*;

/**
 * G³ówna klasa aplikacji klienckiej komunikatora sieciowego. Na pocz¹tku
 * pobiera konfiguracjê, prosi o zalogowanie siê oraz ³¹czy siê z serwerem.
 * 
 * @author GRUPA 1AS
 * 
 */
public class Client {

	/** dane aplikacji */
	private static ClientData data = ClientData.getInstance();

	// --------MAIN-----------------------------------------------------------------
	public static void main(String[] args) {

		getConfiguration();

		generateKeys();

//		loadNumber();

//		connectWithServer();

		new ClientInterface().start();

	}

	// --------METODY------------------------------------
	/**
	 * Metoda ³¹czy klienta z serwerem i uruchamia nowy w¹tek do odbierania
	 * danych z serwera.
	 */
//	private static void connectWithServer() {
//
//		try {
//
//			Socket localSocket = new Socket(data.getRemoteAddress(),
//					data.getRemotePort());
//			data.setLocalSocket(localSocket);
//
//			new Receiver().start();
//
//		} catch (IOException e) {
//			System.out.println(e.toString());
//		}
//	}

	/**
	 * Metoda pobiera konfiguracjê z pliku.
	 */
	private static void getConfiguration() {

		try {

			File file = new File("config.conf");
			Scanner in = new Scanner(file);

			data.setRemoteAddress(in.nextLine());
			data.setRemotePort(Integer.parseInt(in.nextLine()));

			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Problem z pobraniem konfiguracji \n"
					+ "ustawiam domyœlny adres serwera: 127.0.0.1:20000");
		}
	}

	/**
	 * Metoda zajmuje siê wygenerowanie kluczy RSA do weryfikowania
	 * autentycznoœci
	 */
	private static void generateKeys() {

		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(1024);
			KeyPair kp = kpg.genKeyPair();
			RSAPublicKey pubKey = (RSAPublicKey) kp.getPublic();
			RSAPrivateKey privKey = (RSAPrivateKey) kp.getPrivate();

			data.setPublicKey(pubKey);
			data.setPrivateKey(privKey);

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// prawdopodobnie tymczasowa metoda
	private static void loadNumber() {

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
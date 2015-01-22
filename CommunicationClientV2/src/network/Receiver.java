package network;

import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;

import management.*;
import security.*;

/**
 * Klasa odpowiedzialna za odbieranie danych z serwera przez klienta i
 * analizowanie ich.
 * 
 * @author GRUPA 1AS
 * 
 */
public class Receiver extends Thread {

	// -------POLA----------------------------------------------------------
	/** dane aplikacji */
	private ClientData data;

	// -------KONSTRUKTORY--------------------------------------------------
	public Receiver() {

		this.data = ClientData.getInstance();
		setName("Receiver");
	}

	// -------RUN----------------------------------------------------------
	/**
	 * G³ówna metoda klasy Receiver. Odpowiada za odbieranie danych z serwera w
	 * oddzielnym w¹tku i analizowanie ich.
	 */
	public void run() {

		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(data
					.getLocalSocket().getInputStream()));

			while (true) {

				String communicate;

				communicate = in.readLine(); // zawieszenie

				System.out.println("From server | " + communicate);

				if (communicate == null) {
					in.close();
					data.getLocalSocket().close();
					data.setLogged(false);
					break;
				}
				// przeanalizowanie odebranych danych
				analyseCommunicate(communicate);
			}
			// TODO zamykanie gniazda
		} catch (IOException e) {
			System.out.println("Receiver.run, " + e.toString());
			e.printStackTrace();
		}
	}

	// ------- METODY-----------------------------------------------------
	/**
	 * Metoda anlizuje komunikat przys³any przez serwer i ewentualnie odpowiada
	 * na niego. Klient domyœlnie nie odpowiada serwerowi, chyba ¿e serwer wyda
	 * polecenie GET
	 * 
	 * @param communicate
	 *            komunikat przys³any przez serwer
	 */
	private void analyseCommunicate(String communicate) {

		int sessionId;
		int number;
		Session session;
		User user;
		BigInteger publicExponent;
		BigInteger publicModulus;

		Cipher cipher;

		if (communicate != null) {
			String[] comElements = communicate.split(" ");

			switch (comElements[0]) {

			case "VERIFIED":
				data.setLogged(true);
				break;
			case "GET":

				switch (comElements[1]) {

				case "NUMBER":
					sendNumber();
					break;
				}
				break;

			case "INIT": // polecenie od serwera do³¹czenia siê do sesji i
							// uporz¹dkowania danych
							// "SESSION id_sesji klient1 klient2 "...
							// nie bêdzie wys³ane dopóki wszyscy nie przyœl¹
							// omunikatuu READY

				new KeyAgree(comElements).start();
				break;

			case "CLIENT":

				sessionId = Integer.parseInt(comElements[2]);
				session = data.getSessionSet().getSession(sessionId);

				switch (comElements[3]) {
				case "ABORT":
					data.getSessionSet().removeSession(sessionId);
					System.out.println("Wykryto oszusta, sesja " + sessionId + " zostaje przerwana");
					break;
				case "READY":

					int readyUserNumber = Integer.parseInt(comElements[4]);
					publicExponent = new BigInteger(comElements[5]);
					publicModulus = new BigInteger(comElements[6]);

					while (true) {
						session = data.getSessionSet().getSession(sessionId);

						if (session != null)
							break;
						try {
							Thread.sleep(2);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					user = session.getKap().getTopology()
							.getUser(readyUserNumber);

					// przypisanie uzytkownikowi przys³anych kluczy publicznych
					user.setPublicExponent(publicExponent);
					user.setPublicModulus(publicModulus);

					session.getKap().getTopology().setReady(readyUserNumber);
					break;

				case "PARAM":

					session = data.getSessionSet().getSession(sessionId);

					BigInteger numberMessage = new BigInteger(comElements[4]);

					byte[] byteMessage = numberMessage.toByteArray();

					byte[] byteNumber = ByteOperations.divideByteTable(
							byteMessage, 1, 4);

					// konkatenacja paramType|userId|parameter
					byte[] publicMessage = ByteOperations.divideByteTable(
							byteMessage, 0, 17);
					// test
					System.out.println("PDU RECEIVED| M = 1|U|p: " + ByteOperations
							.toBinaryString(publicMessage));
					byte paramKind = byteMessage[0];
					byte[] param = ByteOperations.divideByteTable(byteMessage,
							5, 17);
					byte[] cipheredM = ByteOperations.divideByteTable(
							byteMessage, 18, byteMessage.length - 1);

					number = new BigInteger(byteNumber).intValue();

					user = session.getKap().getTopology().getUser(number);
					publicExponent = user.getPublicExponent();
					publicModulus = user.getPublicModulus();
					RSAPublicKeySpec keySpec = new RSAPublicKeySpec(
							publicModulus, publicExponent);

					BigInteger readyParameter = new BigInteger(param);

					try {
						KeyFactory kf = KeyFactory.getInstance("RSA");
						PublicKey pubKey = kf.generatePublic(keySpec);

						cipher = Cipher.getInstance("RSA");
						cipher.init(Cipher.DECRYPT_MODE, (Key) pubKey);

						byte[] decipferedMessage = cipher.doFinal(cipheredM);

						if (new BigInteger(decipferedMessage)
								.equals(new BigInteger(publicMessage)))
							System.out
									.println("UZYTKOWNIK AUTENTYCZNY");
						else {
							System.out.println("WYKRYTO OSZUSTA, PRZERYWAM");

							// ABORT
							int[] userTable = session.getKap().getTopology()
									.getParticipantsNumbers(data.getUserNumber());
							
							for (int i = 0; i < userTable.length; i++) {
								String message = "CLIENT " + userTable[i] + " "
										+ session.getSessionId() + " ABORT";
								sendToServer(message);
							}
							return;
						}

					} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidKeyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalBlockSizeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (BadPaddingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidKeySpecException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


					if (paramKind == 1) {

						// TODO dostaliœmy X
						if (session.getKap().getTopology()
								.isRightNeighbour(data.getUserNumber(), number))
							session.getKap().setRightNeighbourKey(
									readyParameter);
						else if (session.getKap().getTopology()
								.isLeftNeighbour(data.getUserNumber(), number))
							session.getKap()
									.setLeftNeighbourKey(readyParameter);
					} else if (paramKind == 2) {

						// sprawdzenie czy u¿ytkownik nale¿y do sesji
						if (session.getKap().getTopology().containsUser(number)) {

							session.getKap().getTopology().getUser(number)
									.setYParam(readyParameter);
						} else {

							// ABORT
							int[] userTable = session.getKap().getTopology()
									.getParticipantsNumbers(data.getUserNumber());
							
							for (int i = 0; i < userTable.length; i++) {
								String message = "CLIENT " + userTable[i] + " "
										+ session.getSessionId() + " ABORT";
								sendToServer(message);
							}
							return;
						}
					}

					break;

				case "MESS":

					sessionId = Integer.parseInt(comElements[2]);
					String message = comElements[4];
					BigInteger cipheredMessageNumber = new BigInteger(message);

					session = data.getSessionSet().getSession(sessionId);
					BigInteger sessionKey = session.getKap().getSessionKey();

					try {
						SecretKeyFactory sf = SecretKeyFactory
								.getInstance("DES");
						// byte[] messageBytes = message.getBytes("UTF-8");
						byte[] messageBytes = cipheredMessageNumber
								.toByteArray();

						cipher = Cipher.getInstance("DES");

						byte[] keyBytes = sessionKey.toByteArray();
						cipher.init(Cipher.DECRYPT_MODE,
								sf.generateSecret(new DESKeySpec(keyBytes)));

						byte[] deciphered = cipher.doFinal(messageBytes);

						System.out.println(new String(message));
						System.out.println(new String(deciphered));

					} catch (InvalidKeyException e) {

					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchPaddingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalBlockSizeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (BadPaddingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidKeySpecException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;
				}
			}
		}
	}

	/**
	 * Metoda wysy³a na serwer wiadomoœæ o swoim numerze podczas logowania.
	 * Metodê mo¿na rozwin¹æ o dodatkowe wysy³anie hashcode'u jako
	 * niezaprzeczalnego weryfikowania u¿ytkownika.
	 */
	private void sendNumber() {

		sendToServer("SET NUMBER " + data.getUserNumber());
	}

	/**
	 * Metoda wysy³¹ wiadomoœæ na serwer.
	 * 
	 * @param message
	 *            wysy³ana wiadomoœæ
	 */
	private void sendToServer(String message) {

		try {
			PrintWriter out = new PrintWriter(data.getLocalSocket()
					.getOutputStream(), true);

			out.println(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("To server   | " + message);
	}

}
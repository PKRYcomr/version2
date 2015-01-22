package security;

import java.io.*;
import java.math.*;
import java.security.*;

import javax.crypto.*;

import management.*;
import network.*;

/**
 * Klasa s�u�y do wykonywania procedury KeyAgree w oddzielnym w�tku. Zajmuje si�
 * jednak jedynie wysy�aniem odpowiednich wiadomo�ci i komend. Odbieraniem
 * wiadomo�ci z serwera zajmuje sie oddzielny w�tek klasy Receiver.
 * 
 * @author GRUPA 1AS
 * 
 */
public class KeyAgree extends Thread {

	// ----------POLA--------------------------------------------------------
	String[] comElements;
	ClientData data;
	/** sesja, dla kt�rej w�tek oblicza protok� */
	Session session;

	// -----------KONSTRUKTORY---------------------------------------------
	public KeyAgree(String[] communicateElements) {

		this.comElements = communicateElements;
		this.data = ClientData.getInstance();
		setName("KeyAgree");
	}

	// ----------METODY----------------------------------------------------------
	public void run() {

		int sessionId = Integer.parseInt(comElements[1]);
		session = new Session(sessionId, data.getUserNumber());

		initProcedure();

		executeRoundOne();

		executeRoundTwo();

		computeSessionKey();
	}

	/**
	 * Metoda wysy�a wiadomo�� na serwer.
	 * 
	 * @param message
	 *            wysy�ana wiadomo��
	 */
	private void sendToServer(String message) {

		try {
			PrintWriter out = new PrintWriter(data.getLocalSocket()
					.getOutputStream(), true);

			out.println(message);
		} catch (IOException e) {

			e.printStackTrace();
		}
		System.out.println("To server   | " + message);
	}

	/**
	 * Metoda zajmuje si� inicjalizacj� procedury KeyAgree. Inizjalizacja jest
	 * tutaj rozumiana jako przeanalizowanie wiadomo�ci INIT z serwera, na co
	 * sk�ada si� kilka krok�w: 1. stworzenie instancji klasy Session o
	 * przys�anym przez serwer ID sesji 2. przypisanie nowo stworzonej sesji
	 * listy numer�w jej uczestnik�w przys�an� przez serwer 3. wygenerowanie
	 * wiadomo�ci READY adresowanej do pozosta�ych uczestnik�w protoko�u, w
	 * kt�rej zawarta jest informacja o gotowo�ci do przej�cia do rundy
	 * pierwszej protoko�u oraz rozes�anie tej wiadomo�ci 4. ostatnim krokiem
	 * jest oczekiwanie na stan gotowo�ci ca�ego pier�cienia do przej�cia do
	 * kolejnego etapu podczas czego w�tek staje si� zablokowany, co wi��e si� z
	 * odebramiem od wszystkich uczestnik�w wiadomo�ci READY; po tym w�tek jest
	 * odblokowywany.
	 */
	private void initProcedure() {

		String message;
		int sessionId = Integer.parseInt(comElements[1]);

		// dodanie numer�w wszystkich uczestnik�w sesji
		// do topologii pier�cienia protoko�u
		for (int i = 2; i < comElements.length; i++) {
			int number = Integer.parseInt(comElements[i]);
			session.getKap().getTopology().addUser(number);
		}
		// dodanie sesji do danych aplikacji
		data.getSessionSet().add(session);

		// ustawienie w�asnego statusu na READY, co oznacza gotowo�� do
		// przej�cia do kolejnego etapu
		session.getKap().getTopology().setReady(data.getUserNumber());

		// wys�anie wiadomo�ci READY do reszty uczestnik�w
		int[] numbers = session.getKap().getTopology()
				.getParticipantsNumbers(data.getUserNumber());

		for (int i = 0; i < numbers.length; i++) {

			// CLIENT recipientNumber sessionId READY readyUserNumber
			// publicExponent publicModulus
			message = "CLIENT " + numbers[i] + " " + sessionId + " READY "
					+ data.getUserNumber() + " "
					+ data.getPublicKey().getPublicExponent() + " "
					+ data.getPublicKey().getModulus();

			sendToServer(message);
		}

		// czeka na odebranie wiadomo�ci READY od wszystkich uczestnik�w
		// protoko�u
		session.getKap().getTopology().waitForReadyStatus();
		System.out.println("-------- STATUS READY ----------------");
	}

	/**
	 * Metoda zajmuje si� wykonaniem pierwszej rundy procedury AuthKeyAgree,
	 * czyli oblicza klucz cz�ciowy sesji oraz rozsy�a go do pozosta�ych
	 * uczestnik�w protoko�u. Wysy�any komunikat wygl�da nast�puj�co:
	 * "CLIENT participantNumber sessionId PARAM 1|userId|X|ciphered(1|userId|X)"
	 * . W ostatnim s�owie komunikatu zaszyty jest tekst jawny oraz zaszyfrowany
	 * do weryfikacji autentyczno�ci.
	 */
	private void executeRoundOne() {

		System.out.println("-------- RUNDA 1 ----------------");
		try {
			String message;
			Cipher cipher = Cipher.getInstance("RSA");
			BigInteger numberMessage = BigInteger.valueOf(0);
			final KeyAgreementProtocol kap = session.getKap();

			// obliczenie parametru X - klucza cz�ciowego
			kap.computeX(data.getPrivateX(), ClientData.MODULE,
					ClientData.GENERATOR);

			// inicjalizacja szyfru RSA w trybie szyfrowania
			cipher.init(Cipher.ENCRYPT_MODE, data.getPrivateKey());

			// numer uczestnika
			BigInteger number = BigInteger.valueOf(data.getUserNumber());

			// klucz cz�ciowy uczestnika
			BigInteger x = kap.getPartialKey();

			// rozci�gni�cie numeru u�ytkownika do 4 bajt�w
			byte[] bytesNumber = ByteOperations.resizeToNumberOfBytes(
					number.toByteArray(), 4);
			// 1 jako bajt do podpisu typu parametru
			byte[] bytesOne = BigInteger.valueOf(1).toByteArray();

			// rozciagni�cie klucza cz�ciowego do 13 bajt�w
			byte[] byteX = ByteOperations.resizeToNumberOfBytes(
					x.toByteArray(), 13);

			// konkatenacja 1|U
			byte[] firstStep = ByteOperations.concatenation(bytesOne,
					bytesNumber);

			// elementy wysy�anej wiadomo�ci, konkatenacja 1|U|X = M
			byte[] M = ByteOperations.concatenation(firstStep, byteX);

			// zaszyfrowane bajty M
			byte[] cipherData = cipher.doFinal(M);

			// konkatenacja publicText|cipheredText
			byte[] byteMessage = ByteOperations.concatenation(M, cipherData);

			// wiadomo�� ko�cowa utworzona z konkatenacji:
			// 1|U|X|ciphered(1|U|X)
			numberMessage = new BigInteger(byteMessage);

			System.out
					.println("PDU SENT    | M = 1|U|X: " + ByteOperations.toBinaryString(M));

			message = "CLIENT "
					+ session.getKap().getTopology()
							.getLeftNeighbour(data.getUserNumber())
							.getUserNumber() + " " + session.getSessionId()
					+ " PARAM " + numberMessage;

			new Sender(message).start();

			message = "CLIENT "
					+ session.getKap().getTopology()
							.getRightNeighbour(data.getUserNumber())
							.getUserNumber() + " " + session.getSessionId()
					+ " PARAM " + numberMessage;

			new Sender(message).start();

		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("-------- KONIEC RUNDY 1 ----------------");
	}

	/**
	 * Metoda wykonuje drug� rund� procedury AuthKeyAgree, a dok�adniej jej
	 * cz�� aktywn�, poniewa� nie zajmuje si� odbieraniem wiadomo�ci z
	 * parametrem X. Metoda ta czeka jednak na pojawienie si� tych parametr�w w
	 * danych sesji (zablokowany w�tek dop�ki ich nie ma), a nast�pnie pos�uguje
	 * si� nimi do obliczenia parametru Y (po dotarciu parametr�w X z serwera od
	 * wszystkich uczestnik�w w�tek jest odblokowywany). Wysy�any komunikat
	 * wygl�da nast�puj�co:
	 * "CLIENT participantNumber sessionId PARAM 2|userId|Y|ciphered(2|userId|Y)"
	 * . W ostatnim s�owie komunikatu zaszyty jest tekst jawny oraz zaszyfrowany
	 * do weryfikacji autentyczno�ci.
	 */
	private void executeRoundTwo() {

		System.out.println("-------- RUNDA 2 ----------------");
		try {
			String message;
			Cipher cipher = Cipher.getInstance("RSA");
			BigInteger numberMessage = BigInteger.valueOf(0);
			final KeyAgreementProtocol kap = session.getKap();

			// obliczenie parametru Y
			kap.computeY(data.getPrivateX(), ClientData.MODULE);

			// inicjalizacja szyfru RSA w trybie szyfrowania
			cipher.init(Cipher.ENCRYPT_MODE, data.getPrivateKey());

			// numer uczestnika
			BigInteger number = BigInteger.valueOf(data.getUserNumber());

			// pobranie instancji do parametru Y tego uczestnika
			BigInteger y = kap.getYParameter();

			// rozci�gni�cie numeru u�ytkownika do 4 bajt�w
			byte[] bytesNumber = ByteOperations.resizeToNumberOfBytes(
					number.toByteArray(), 4);
			// 2 jako bajt - znacznik parametru Y
			byte[] bytesTwo = BigInteger.valueOf(2).toByteArray();

			// rozciagni�cie klucza do 13 bajt�w - 24?
			byte[] byteY = ByteOperations.resizeToNumberOfBytes(
					y.toByteArray(), 13);

			// konkatenacja 2|U
			byte[] firstStep = ByteOperations.concatenation(bytesTwo,
					bytesNumber);

			// elementy wysy�anej wiadomo�ci, konkatenacja 2|U|Y = M
			byte[] M = ByteOperations.concatenation(firstStep, byteY);

			// zaszyfrowane bajty M : 29 bajt�w?
			byte[] cipherData = cipher.doFinal(M);

			// konkatenacja publicText|cipheredText
			byte[] byteMessage = ByteOperations.concatenation(M, cipherData);

			// wiadomo�� ko�cowa utworzona z konkatenacji:
			// 2|U|Y|ciphered(2|U|Y)
			numberMessage = new BigInteger(byteMessage);

			System.out
					.println("PDU SENT    | M = 1|U|Y: " + ByteOperations.toBinaryString(M));

			// pobranie numer�w wszystkich uczestnik�w poza wskazanym
			int[] restParticipants = kap.getTopology().getParticipantsNumbers(
					data.getUserNumber());
			// wys�anie Y do reszty uczestnik�w
			for (int i = 0; i < restParticipants.length; i++) {
				message = "CLIENT " + restParticipants[i] + " "
						+ session.getSessionId() + " PARAM " + numberMessage;

				new Sender(message).start();
			}

		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("-------- KONIEC RUNDY 2 ----------------");
	}

	/**
	 * Metoda zajmuje si� ko�cowym etapem procedury AuthKeyAgree, a wi�c
	 * obliczeniem klucza grupowego sesji na podstawie otrzymanych danych od
	 * innych uzytkownik�w oraz w�asnego klucza prywatnego.
	 */
	private void computeSessionKey() {

		System.out.println("-------- KEY COMPUTATION ----------------");
		KeyAgreementProtocol kap = session.getKap();

		// obliczenie prawych kluczy wszystkich uczestnik�w
		int result = kap.computeRightKeys(ClientData.MODULE);

		if (result == -1) {
			System.out
					.println("USTALANIE KLUCZA NIE POWIODLO SIE, PRZERYWAM");
			
			int[] userTable = session.getKap().getTopology()
					.getParticipantsNumbers(data.getUserNumber());
			
			for (int i = 0; i < userTable.length; i++) {
				String message = "CLIENT " + userTable[i] + " "
						+ session.getSessionId() + " ABORT";
				sendToServer(message);
			}
			return;
		} else
			System.out.println("WERYFIKACJA POWIODLA SIE, OBLICZAM KLUCZ SESJI");

		// obliczenie grupowego klucza sesji
		kap.computeSessionKey(ClientData.MODULE);
	}
}
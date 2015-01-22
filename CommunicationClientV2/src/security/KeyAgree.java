package security;

import java.io.*;
import java.math.*;
import java.security.*;

import javax.crypto.*;

import management.*;
import network.*;

/**
 * Klasa s³u¿y do wykonywania procedury KeyAgree w oddzielnym w¹tku. Zajmuje siê
 * jednak jedynie wysy³aniem odpowiednich wiadomoœci i komend. Odbieraniem
 * wiadomoœci z serwera zajmuje sie oddzielny w¹tek klasy Receiver.
 * 
 * @author GRUPA 1AS
 * 
 */
public class KeyAgree extends Thread {

	// ----------POLA--------------------------------------------------------
	String[] comElements;
	ClientData data;
	/** sesja, dla której w¹tek oblicza protokó³ */
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
	 * Metoda wysy³a wiadomoœæ na serwer.
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

			e.printStackTrace();
		}
		System.out.println("To server   | " + message);
	}

	/**
	 * Metoda zajmuje siê inicjalizacj¹ procedury KeyAgree. Inizjalizacja jest
	 * tutaj rozumiana jako przeanalizowanie wiadomoœci INIT z serwera, na co
	 * sk³ada siê kilka kroków: 1. stworzenie instancji klasy Session o
	 * przys³anym przez serwer ID sesji 2. przypisanie nowo stworzonej sesji
	 * listy numerów jej uczestników przys³an¹ przez serwer 3. wygenerowanie
	 * wiadomoœci READY adresowanej do pozosta³ych uczestników protoko³u, w
	 * której zawarta jest informacja o gotowoœci do przejœcia do rundy
	 * pierwszej protoko³u oraz rozes³anie tej wiadomoœci 4. ostatnim krokiem
	 * jest oczekiwanie na stan gotowoœci ca³ego pierœcienia do przejœcia do
	 * kolejnego etapu podczas czego w¹tek staje siê zablokowany, co wi¹¿e siê z
	 * odebramiem od wszystkich uczestników wiadomoœci READY; po tym w¹tek jest
	 * odblokowywany.
	 */
	private void initProcedure() {

		String message;
		int sessionId = Integer.parseInt(comElements[1]);

		// dodanie numerów wszystkich uczestników sesji
		// do topologii pierœcienia protoko³u
		for (int i = 2; i < comElements.length; i++) {
			int number = Integer.parseInt(comElements[i]);
			session.getKap().getTopology().addUser(number);
		}
		// dodanie sesji do danych aplikacji
		data.getSessionSet().add(session);

		// ustawienie w³asnego statusu na READY, co oznacza gotowoœæ do
		// przejœcia do kolejnego etapu
		session.getKap().getTopology().setReady(data.getUserNumber());

		// wys³anie wiadomoœci READY do reszty uczestników
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

		// czeka na odebranie wiadomoœci READY od wszystkich uczestników
		// protoko³u
		session.getKap().getTopology().waitForReadyStatus();
		System.out.println("-------- STATUS READY ----------------");
	}

	/**
	 * Metoda zajmuje siê wykonaniem pierwszej rundy procedury AuthKeyAgree,
	 * czyli oblicza klucz czêœciowy sesji oraz rozsy³a go do pozosta³ych
	 * uczestników protoko³u. Wysy³any komunikat wygl¹da nastêpuj¹co:
	 * "CLIENT participantNumber sessionId PARAM 1|userId|X|ciphered(1|userId|X)"
	 * . W ostatnim s³owie komunikatu zaszyty jest tekst jawny oraz zaszyfrowany
	 * do weryfikacji autentycznoœci.
	 */
	private void executeRoundOne() {

		System.out.println("-------- RUNDA 1 ----------------");
		try {
			String message;
			Cipher cipher = Cipher.getInstance("RSA");
			BigInteger numberMessage = BigInteger.valueOf(0);
			final KeyAgreementProtocol kap = session.getKap();

			// obliczenie parametru X - klucza czêœciowego
			kap.computeX(data.getPrivateX(), ClientData.MODULE,
					ClientData.GENERATOR);

			// inicjalizacja szyfru RSA w trybie szyfrowania
			cipher.init(Cipher.ENCRYPT_MODE, data.getPrivateKey());

			// numer uczestnika
			BigInteger number = BigInteger.valueOf(data.getUserNumber());

			// klucz czêœciowy uczestnika
			BigInteger x = kap.getPartialKey();

			// rozci¹gniêcie numeru u¿ytkownika do 4 bajtów
			byte[] bytesNumber = ByteOperations.resizeToNumberOfBytes(
					number.toByteArray(), 4);
			// 1 jako bajt do podpisu typu parametru
			byte[] bytesOne = BigInteger.valueOf(1).toByteArray();

			// rozciagniêcie klucza czêœciowego do 13 bajtów
			byte[] byteX = ByteOperations.resizeToNumberOfBytes(
					x.toByteArray(), 13);

			// konkatenacja 1|U
			byte[] firstStep = ByteOperations.concatenation(bytesOne,
					bytesNumber);

			// elementy wysy³anej wiadomoœci, konkatenacja 1|U|X = M
			byte[] M = ByteOperations.concatenation(firstStep, byteX);

			// zaszyfrowane bajty M
			byte[] cipherData = cipher.doFinal(M);

			// konkatenacja publicText|cipheredText
			byte[] byteMessage = ByteOperations.concatenation(M, cipherData);

			// wiadomoœæ koñcowa utworzona z konkatenacji:
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
	 * Metoda wykonuje drug¹ rundê procedury AuthKeyAgree, a dok³adniej jej
	 * czêœæ aktywn¹, poniewa¿ nie zajmuje siê odbieraniem wiadomoœci z
	 * parametrem X. Metoda ta czeka jednak na pojawienie siê tych parametrów w
	 * danych sesji (zablokowany w¹tek dopóki ich nie ma), a nastêpnie pos³uguje
	 * siê nimi do obliczenia parametru Y (po dotarciu parametrów X z serwera od
	 * wszystkich uczestników w¹tek jest odblokowywany). Wysy³any komunikat
	 * wygl¹da nastêpuj¹co:
	 * "CLIENT participantNumber sessionId PARAM 2|userId|Y|ciphered(2|userId|Y)"
	 * . W ostatnim s³owie komunikatu zaszyty jest tekst jawny oraz zaszyfrowany
	 * do weryfikacji autentycznoœci.
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

			// rozci¹gniêcie numeru u¿ytkownika do 4 bajtów
			byte[] bytesNumber = ByteOperations.resizeToNumberOfBytes(
					number.toByteArray(), 4);
			// 2 jako bajt - znacznik parametru Y
			byte[] bytesTwo = BigInteger.valueOf(2).toByteArray();

			// rozciagniêcie klucza do 13 bajtów - 24?
			byte[] byteY = ByteOperations.resizeToNumberOfBytes(
					y.toByteArray(), 13);

			// konkatenacja 2|U
			byte[] firstStep = ByteOperations.concatenation(bytesTwo,
					bytesNumber);

			// elementy wysy³anej wiadomoœci, konkatenacja 2|U|Y = M
			byte[] M = ByteOperations.concatenation(firstStep, byteY);

			// zaszyfrowane bajty M : 29 bajtów?
			byte[] cipherData = cipher.doFinal(M);

			// konkatenacja publicText|cipheredText
			byte[] byteMessage = ByteOperations.concatenation(M, cipherData);

			// wiadomoœæ koñcowa utworzona z konkatenacji:
			// 2|U|Y|ciphered(2|U|Y)
			numberMessage = new BigInteger(byteMessage);

			System.out
					.println("PDU SENT    | M = 1|U|Y: " + ByteOperations.toBinaryString(M));

			// pobranie numerów wszystkich uczestników poza wskazanym
			int[] restParticipants = kap.getTopology().getParticipantsNumbers(
					data.getUserNumber());
			// wys³anie Y do reszty uczestników
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
	 * Metoda zajmuje siê koñcowym etapem procedury AuthKeyAgree, a wiêc
	 * obliczeniem klucza grupowego sesji na podstawie otrzymanych danych od
	 * innych uzytkowników oraz w³asnego klucza prywatnego.
	 */
	private void computeSessionKey() {

		System.out.println("-------- KEY COMPUTATION ----------------");
		KeyAgreementProtocol kap = session.getKap();

		// obliczenie prawych kluczy wszystkich uczestników
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
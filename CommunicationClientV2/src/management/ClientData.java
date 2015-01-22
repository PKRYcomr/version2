package management;

import java.math.*;
import java.net.*;
import java.security.interfaces.*;

import security.*;

/**
 * Klasa zawieraj¹ce informacje charakterystyczne aplikacji klienckiej.
 * 
 * @author GRUPA 1AS
 * 
 */
public class ClientData {

	// ------POLA--------------------------------------------------------
	/** do singleton */
	private static ClientData data;

	/** adres IP serwera, domyœlnie localhost */
	private String remoteAddress;
	/** port serwera, na którym nas³uchuje on klientów, domyœlnie 20000 */
	private int remotePort;
	/**
	 * 6-cyfrowy numer uzytkownika, dla uproszczenia ka¿da aplikacja mo¿e byæ
	 * uto¿samiana tylko zjednym numerem
	 */
	private int userNumber;
	/** pula wszystkich sesji tego klienta */
	private SessionsCollection sessions;
	/** gnizado, przez które klient ³¹czy siê z serwerem */
	private Socket localSocket;
	
	/** status u¿ytkownika - logged/unlogged*/
	private boolean logged;

	// kryptografia ------------------
	// Group Key Agreement
	/**
	 * klucz prywatny na potrzeby grupowego ustalania klucza, jedna instancja do
	 * ustalania klucza grupowego we wszystkich sesjach.
	 */
	private BigInteger privateX;

	/** generator grupy cyklicznej u¿ywanej w protokole */
	public static long GENERATOR = 2;
	/** modu³ grupy cyklicznej u¿ywanej w protokole */
	public static BigInteger MODULE = new BigInteger(
			"1218846000056379267273391500379");
	// public static BigInteger MODULE = new
	// BigInteger("5065146778403821292967743109935929550701867739185292261543");

	// RSA
	RSAPublicKey pubKey;
	RSAPrivateKey privKey;

	// -----KONSTRUKTORY-------------------------------------------------------------------------

	private ClientData() {

		// domyœlne wartoœci
		remoteAddress = "127.0.0.1";
		remotePort = 20000;
		userNumber = 000000;

		logged = false;
		privateX = KeyAgreementProtocol.chooseRandomPrivateKey();
		sessions = new SessionsCollection();
	}

	// -----METODY GET SET-----------------------------------------

	
	public void setPublicKey(RSAPublicKey pubKey) {
		this.pubKey = pubKey;
	}
	
	public boolean isLogged() {
		return logged;
	}

	public void setLogged(boolean logged) {
		this.logged = logged;
	}

	public void setPrivateKey(RSAPrivateKey privKey) {
		this.privKey = privKey;
	}

	public RSAPublicKey getPublicKey() {
		return pubKey;
	}

	public RSAPrivateKey getPrivateKey() {
		return privKey;
	}

	public BigInteger getPrivateX() {
		return privateX;
	}

	public void setLocalSocket(Socket socket) {
		this.localSocket = socket;
	}

	public Socket getLocalSocket() {
		return localSocket;
	}

	public SessionsCollection getSessionSet() {
		return sessions;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public int getUserNumber() {
		return userNumber;
	}

	public void setUserNumber(int userNumber) {
		this.userNumber = userNumber;
	}

	public void addSession(Session session) {
		this.sessions.add(session);
	}

	/**
	 * Usuwa sesjê o podanym ID z danych klienta.
	 * 
	 * @param sessionId
	 *            ID sesji
	 */
	public void removeSession(int sessionId) {
		for (int i = 0; i < sessions.size(); i++)
			if (sessions.get(i).getSessionId() == sessionId)
				sessions.remove(i);
	}

	// do singleton
	public static ClientData getInstance() {

		if (data == null) {
			data = new ClientData();
		}
		return data;
	}

	public void resetSessionSet() {
		
		sessions = new SessionsCollection();
	}
}
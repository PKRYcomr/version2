package management;

import security.*;

/**
 * Klasa reprezentuje sesjê widzian¹ przez klienta.
 * 
 * @author GRUPA 1AS
 * 
 */
public class Session {

	// -----------POLA-------------------------------------------------------
	/** Niepowtarzalny identyfikator sesji nadawany przez serwer - deprecated */
	private int sessionId;

	/** Niepowtarzalny 3-cyfrowy identyfikator sesji dla jednego u¿ytkownika */
	private KeyAgreementProtocol kap;

	// -----------KONSTRUKTORY-----------------------------------------------
	/**
	 * Konstruktor domyœlny klasy Session.
	 * 
	 * @param sessionId
	 *            czêœciowy identifikator sesji dla tego uzytkownika
	 * @param userNumber
	 *            numer u¿ytkownika
	 */
	public Session(int sessionId, int userNumber) {

		this.sessionId = sessionId;

		kap = new KeyAgreementProtocol(userNumber);
	}

	// -----------METODY---------------------------------------
	public int getSessionId() {
		return sessionId;
	}

	/**
	 * Metoda daje dostep do obiektu KeyAgreementProtocol dla tej sesji.
	 * 
	 * @return obiekt KeyAgreementProtocol
	 */
	public KeyAgreementProtocol getKap() {

		return kap;
	}
}
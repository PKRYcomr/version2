package management;

import java.util.*;

/**
 * Klasa pomagaj�ca zarz�dza� istniej�cymi sesjami obs�ugiwanymi przez serwer.
 * 
 * @author GRUPA 1AS
 * 
 */
public class SessionsCollection extends Vector<Session> {

	// -------POLA--------------------------------------------------------------
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// -------KONSTRUKTORY------------------------------------------------------
	public SessionsCollection() {

		super();
	}

	// -------METODY------------------------------------------------------------
	/**
	 * Metoda zwraca obiekt klasy Session z puli sesji ca�ego serwera na
	 * podstawie ID sesji.
	 * 
	 * @param sessionId
	 *            ID sesji
	 * @return obiekt reprezentuj�cy sesj� komunikacyjn�
	 */
	public Session getSession(int sessionId) {

		for (int i = 0; i < this.size(); i++) {

			if (this.get(i).getSessionId() == sessionId)
				return this.get(i);
		}

		return null;
	}
	
	/**
	 * Metoda usuwa sesj� o wskazanym id
	 * @param sessionId
	 */
	public void removeSession(int sessionId) {
		
		for (int i = 0; i < this.size(); i++) {

			if (this.get(i).getSessionId() == sessionId)
				remove(i);
		}
	}

	/**
	 * Metoda zwraca true, je�li sesja o podanym ID ju� istnieje. W przeciwnym
	 * przypadku zwraca false.
	 * 
	 * @param sessionId
	 *            ID sesji
	 * @return rezultat sprawdzenia typu boolean
	 */
	public boolean existSessionId(int sessionId) {

		for (int i = 0; i < this.size(); i++) {

			if (this.get(i).getSessionId() == sessionId)
				return true;
		}

		return false;
	}
}
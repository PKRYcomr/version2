import java.util.*;
import java.net.*;

/**
 * Klasa u³atwia zbieranie numerów aktywnych u¿ytkowników.
 * 
 * @author GRUPA 1AS
 * 
 */
public class UsersCollection extends Vector<User> {

	// -------POLA---------------------------------------------------------------------
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// -------KONSTRUKTORY--------------------------------------------------------------
	public UsersCollection() {

		super();
	}

	// -------METODY-------------------------------------------------------------------------

	/**
	 * Zwraca true, jeœli u¿ytkownik o podanym numerze istnieje. W przeciwnym
	 * wypadku false.
	 * 
	 * @param number
	 *            6-cyfrowy numer u¿ytkownika
	 * @return boolean okreœlaj¹cy czy numer znajduje siê w tabeli
	 */
	public boolean isUserNumber(int number) {

		for (int i = 0; i < this.size(); i++) {

			if (this.get(i).getUserNumber() == number)
				return true;
		}

		return false;
	}

	/**
	 * Metoda s³u¿y do odnalezienia gniazda, przez które po³¹czony jest
	 * u¿ytkownik o podanym numerze.
	 * 
	 * @param number
	 *            6-cyfrowy numer identyfikacyjny u¿ytkownika
	 * @return gniazdo u¿ytkownika
	 */
	public Socket getUserSocket(int number) {

		for (int i = 0; i < this.size(); i++) {

			if (this.get(i).getUserNumber() == number)
				return this.get(i).getUserSocket();
		}

		return null;
	}

	/**
	 * Metoda s³u¿y do odnalezienia obiektu User o zadanym numerze
	 * identyfikacyjnym.
	 * 
	 * @param number
	 *            numer identyfikacyjny
	 * @return obiekt klasy User jesli takowy o zadanym numerze istnieje, jesli
	 *         nie zwraca null
	 */
	public User getUser(int number) {

		for (int i = 0; i < this.size(); i++) {

			if (this.get(i).getUserNumber() == number)
				return this.get(i);
		}

		return null;
	}

	/**
	 * Metoda znajduje indeks u¿ytkownika w kolekcji na podstawie jegu numeru.
	 * 
	 * @param number
	 *            numer u¿ytkownika
	 * @return indeks u¿ytkownika w tej kolekcji, -1 jeœli u¿ytkownik o podanym
	 *         numerze nie istnieje
	 */
	public int getUserIndex(int number) {

		for (int i = 0; i < this.size(); i++) {

			if (this.get(i).getUserNumber() == number)
				return i;
		}

		return -1;
	}
}
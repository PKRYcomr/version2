package security;

import java.util.*;

/**
 * Klasa stanowi kolekcj� uczestnik�w w ramach protoko�u uzgadniania klucza.
 * 
 * @author GRUPA 1AS
 * 
 */
public class UsersCollection extends Vector<User> {

	// -------POLA------------------------------------------------
	private static final long serialVersionUID = 1L;

	// -------KONSTRUKTORY-----------------------------------------
	public UsersCollection() {

		super();
	}

	// -------METODY ----------------------------------------------
	/**
	 * Wyklucza z pier�cienia u�ytkownika o podanym numerze.
	 * 
	 * @param number
	 *            numer uczestnika
	 * @return zwraca true, je�li operacja si� powiod�a, false w przeciwnym
	 *         wypadku
	 */
	public boolean removeUser(int number) {

		int index = getUserIndex(number);

		if (index != -1) {
			remove(index);
			return true;
		} else
			return false;
	}

	/**
	 * Zwraca true, je�li u�ytkownik o podanym numerze istnieje. W przeciwnym
	 * wypadku false.
	 * 
	 * @param number
	 *            6-cyfrowy numer u�ytkownika
	 * @return boolean okre�laj�cy czy numer znajduje si� w tabeli
	 */
	public boolean containsUser(int number) {

		for (int i = 0; i < this.size(); i++) {

			if (this.get(i).getUserNumber() == number)
				return true;
		}

		return false;
	}

	/**
	 * Metoda s�u�y do odnalezienia obiektu User o zadanym numerze
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
	 * Metoda znajduje indeks u�ytkownika w kolekcji na podstawie jegu numeru.
	 * 
	 * @param number
	 *            numer u�ytkownika
	 * @return indeks u�ytkownika w tej kolekcji, -1 je�li u�ytkownik o podanym
	 *         numerze nie istnieje
	 */
	protected int getUserIndex(int number) {

		for (int i = 0; i < this.size(); i++) {

			if (this.get(i).getUserNumber() == number)
				return i;
		}

		return -1;
	}
}
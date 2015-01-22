package security;

/**
 * Klasa u�atwia dost�p do uczestnik�w pier�cienia.
 * 
 * @author GRUPA 1AS
 * 
 */
public class RingTopology extends UsersCollection {

	// -----------POLA-------------------------------------------------------
	private static final long serialVersionUID = 1L;
	/**
	 * Status gotowo�ci ca�ego pier�cienia uczestnik�w do rozpocz�cia obliczania
	 * grupowego klucza
	 */
	boolean ringStatus;

	// -----------KONSTRUKTORY----------------------------------------------
	public RingTopology() {

		ringStatus = false;
	}

	// -----------METODY------------------------------------------------------
	/**
	 * Metoda dodaje uczestnika o podanym numerze do zbioru uczestnik�w.
	 * 
	 * @param number
	 *            numer uczestnika
	 */
	public void addUser(int number) {

		User user = new User(number);
		add(user);
	}

	/**
	 * Metoda pozwala odnale�� lewego s�siada.
	 * 
	 * @param number
	 *            numer uczestnika, kt�rego s�siada szukamy
	 * @return User, lewy s�siad podanego
	 */
	public User getLeftNeighbour(int number) {

		int userIndex = getUserIndex(number);

		int neighbourIndex = (userIndex + size() - 1) % size();

		return get(neighbourIndex);
	}

	/**
	 * Metoda pozwala odnale�� prawego s�siada.
	 * 
	 * @param number
	 *            numer uczestnika, kt�rego s�siada szukamy
	 * @return User, prawy s�siad podanego
	 */
	public User getRightNeighbour(int number) {

		int userIndex = getUserIndex(number);

		int neighbourIndex = (userIndex + 1) % size();

		return get(neighbourIndex);
	}

	public boolean isRightNeighbour(int userNumber, int neighbourNumber) {

		User user = getRightNeighbour(userNumber);
		if (user.getUserNumber() == neighbourNumber)
			return true;
		else
			return false;
	}

	public boolean isLeftNeighbour(int userNumber, int neighbourNumber) {

		User user = getLeftNeighbour(userNumber);
		if (user.getUserNumber() == neighbourNumber)
			return true;
		else
			return false;
	}

	/**
	 * Daje dost�p do listy numer�w uczestnik�w pier�cienia.
	 * 
	 * @param thisUserNumber
	 *            numer, kt�rego ma nie by� na zwracanej li�cie
	 * @return lista numer�w w typie Integer
	 */
	public int[] getParticipantsNumbers(int userNumber) {

		int[] numbers = new int[size() - 1];
		int index = 0;

		for (int i = 0; i < size(); i++) {
			if (get(i).getUserNumber() == userNumber) {
				continue;
			} else {
				numbers[index] = get(i).getUserNumber();
				index++;
			}
		}

		return numbers;
	}

	/**
	 * Ustawia stan uczestnika o podanym numerze na READY
	 * 
	 * @param userNumber
	 *            numer u�ytkownika
	 * @return true, jesli operacja si� powiod�a, false w przeciwnym wypadku
	 */
	public synchronized boolean setReady(int userNumber) {

		int position = getUserIndex(userNumber);

		if (position != -1) {
			get(position).setReadyStatus();
			// sprawdzanie czy wszyscy gotowi
			for (int i = 0; i < size(); i++) {
				if (get(i).getReadyStatus() == false)
					return true; // jesli wykryty jest false metoda nie
									// przechodzi do zmiany warto�ci pola
									// ringStatus
			}
			ringStatus = true;
			notifyAll();

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Ustawia stan uczestnika o podanym numerze na UNREADY
	 * 
	 * @param userNumber
	 *            numer u�ytkownika
	 * @return true, jesli operacja si� powiod�a, false w przeciwnym wypadku
	 */
	public boolean setUnready(int userNumber) {

		int position = getUserIndex(userNumber);

		if (position != -1) {
			get(position).unsetReadyStatus();
			// sprawdzanie czy wszyscy gotowi
			for (int i = 0; i < size(); i++) {
				if (get(i).getReadyStatus() == false)
					ringStatus = false; // jesli wykryty jest false metoda
										// przechodzi zmienia warto�� userStatus
										// na false
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * U�ycie tej metody zatrzymuje w�tek je�li obiekt RingTopology nie ma
	 * statusu READY
	 */
	public synchronized void waitForReadyStatus() {
		if (ringStatus == false)
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public boolean getRingStatus() {
		return ringStatus;
	}

	public int getUserPosition(int number) {
		return getUserIndex(number);
	}

	public String toString() {

		String text = "";
		for (int i = 0; i < size(); i++)
			text = text + get(i).toString() + " ";

		return text;
	}
}
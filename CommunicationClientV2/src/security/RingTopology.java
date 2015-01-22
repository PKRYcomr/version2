package security;

/**
 * Klasa u³atwia dostêp do uczestników pierœcienia.
 * 
 * @author GRUPA 1AS
 * 
 */
public class RingTopology extends UsersCollection {

	// -----------POLA-------------------------------------------------------
	private static final long serialVersionUID = 1L;
	/**
	 * Status gotowoœci ca³ego pierœcienia uczestników do rozpoczêcia obliczania
	 * grupowego klucza
	 */
	boolean ringStatus;

	// -----------KONSTRUKTORY----------------------------------------------
	public RingTopology() {

		ringStatus = false;
	}

	// -----------METODY------------------------------------------------------
	/**
	 * Metoda dodaje uczestnika o podanym numerze do zbioru uczestników.
	 * 
	 * @param number
	 *            numer uczestnika
	 */
	public void addUser(int number) {

		User user = new User(number);
		add(user);
	}

	/**
	 * Metoda pozwala odnaleŸæ lewego s¹siada.
	 * 
	 * @param number
	 *            numer uczestnika, którego s¹siada szukamy
	 * @return User, lewy s¹siad podanego
	 */
	public User getLeftNeighbour(int number) {

		int userIndex = getUserIndex(number);

		int neighbourIndex = (userIndex + size() - 1) % size();

		return get(neighbourIndex);
	}

	/**
	 * Metoda pozwala odnaleŸæ prawego s¹siada.
	 * 
	 * @param number
	 *            numer uczestnika, którego s¹siada szukamy
	 * @return User, prawy s¹siad podanego
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
	 * Daje dostêp do listy numerów uczestników pierœcienia.
	 * 
	 * @param thisUserNumber
	 *            numer, którego ma nie byæ na zwracanej liœcie
	 * @return lista numerów w typie Integer
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
	 *            numer u¿ytkownika
	 * @return true, jesli operacja siê powiod³a, false w przeciwnym wypadku
	 */
	public synchronized boolean setReady(int userNumber) {

		int position = getUserIndex(userNumber);

		if (position != -1) {
			get(position).setReadyStatus();
			// sprawdzanie czy wszyscy gotowi
			for (int i = 0; i < size(); i++) {
				if (get(i).getReadyStatus() == false)
					return true; // jesli wykryty jest false metoda nie
									// przechodzi do zmiany wartoœci pola
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
	 *            numer u¿ytkownika
	 * @return true, jesli operacja siê powiod³a, false w przeciwnym wypadku
	 */
	public boolean setUnready(int userNumber) {

		int position = getUserIndex(userNumber);

		if (position != -1) {
			get(position).unsetReadyStatus();
			// sprawdzanie czy wszyscy gotowi
			for (int i = 0; i < size(); i++) {
				if (get(i).getReadyStatus() == false)
					ringStatus = false; // jesli wykryty jest false metoda
										// przechodzi zmienia wartoœæ userStatus
										// na false
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * U¿ycie tej metody zatrzymuje w¹tek jeœli obiekt RingTopology nie ma
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
package security;

import java.math.*;
import java.util.*;

/**
 * Klasa implementuj�ca protok� grupowego ustalania klucza. Zawiera metody,
 * kt�re s�u�� do ustalenia klucza sesji wraz z potwierdzeniem autentyczno�ci
 * uczestnik�w oraz dynamicznego do��czania do konferencji i opuszczania jej.
 * 
 * @author GRUPA 1AS
 * 
 */
public class KeyAgreementProtocol {

	// -------POLA--------------------------------------------------------------

	// parametry topologiczne
	/** numer uczestnika w pier�cieniu */
	private int userNumber;

	/** topologia uczestnik�w protoko�u */
	private RingTopology topology;

	// dla operacji leave
	/** lewy klucz */
	private BigInteger leftKey;
	/** prawy klucz */
	private BigInteger rightKey;

	// ------------ parametry nowej wersji
	/** losowany klucz prywatny */
	// private BigInteger privateKey;
	/** klucz cz�ciowey obliczany na podstawie klucza prywatnego */
	private BigInteger partialKey;
	/** wsp�lny klucz sesji */
	BigInteger sessionKey;

	// -------KONSTRUKTORY--------------------------------------------
	/**
	 * 
	 * @param userNumber
	 *            numer TEGO uczestnika protoko�a
	 */
	public KeyAgreementProtocol(int userNumber) {

		this.leftKey = null;
		this.rightKey = null;

		this.userNumber = userNumber;

		this.topology = new RingTopology();
	}

	// --------METODY PROTOKO�U-------------------------------------------
	/**
	 * Metoda losuje klucz prywatny TEGO uczestnika.
	 */
	public static BigInteger chooseRandomPrivateKey() {

		Random random = new Random();

		return new BigInteger(100, random);
	}

	/**
	 * Metoda oblicza cz�ciowy klucz sesji TEGO uczestnika.
	 * 
	 * @param privateKey
	 *            klucz prywanty
	 * @param module
	 *            modu� grupy cyklicznej protoko�u
	 * @param generator
	 *            generator grupy cyklicznej protoko�u
	 */
	public void computeX(BigInteger privateKey, BigInteger module,
			long generator) {

		BigInteger big = BigInteger.valueOf(generator);

		partialKey = big.modPow(privateKey, module);
	}

	/**
	 * Metoda oblicza lewy klucz tego uczestnika, gdy ju� pobrany zostanie klucz
	 * cz�ciowy lewego s�siada. Na czas braku tych danych zatrzymuje w�tek
	 * obliczaj�cy klucz.
	 * 
	 * @param privateKey
	 *            klucz prywatny
	 * @param module
	 *            modu� grupy cyklicznej protoko�u
	 */
	private void computeLeftKey(BigInteger privateKey, BigInteger module) {

		BigInteger X;
		X = topology.getLeftNeighbour(userNumber).getPartialKey();
		leftKey = X.modPow(privateKey, module);
	}

	/**
	 * Metoda oblicza prawy klucz tego uczestnika, gdy ju� pobrany zostanie
	 * klucz cz�ciowy prawego s�siada. Na czas braku tych danych zatrzymuje
	 * w�tek obliczaj�cy klucz.
	 * 
	 * @param privateKey
	 *            klucz prywatny
	 * @param module
	 *            modu� grupy cyklicznej protoko�u
	 */
	private void computeRightKey(BigInteger privateKey, BigInteger module) {

		BigInteger X;
		X = topology.getRightNeighbour(userNumber).getPartialKey();
		rightKey = X.modPow(privateKey, module);
	}

	/**
	 * Metoda oblicza klucze lewy i prawy, a nast�pnie parametr Y =
	 * rightKey/leftKey protoko�u dla TEGO uczestnika. Jest to mo�liwe, gdy
	 * wcze�niej zosta�y wykonane operacje: computeRightKey oraz computeLeftKey.
	 * 
	 * @param privateKey
	 *            klucz prywatny
	 * @param module
	 *            modu� grupy cyklicznej protoko�u
	 */
	public void computeY(BigInteger privateKey, BigInteger module) {

		computeLeftKey(privateKey, module);
		computeRightKey(privateKey, module);

		BigInteger reversedLeftKey = leftKey.modInverse(module);
		BigInteger y = (rightKey.multiply(reversedLeftKey)).mod(module);

		topology.getUser(userNumber).setYParam(y);
	}

	/**
	 * Metoda oblicza wszystkie prawe klucze cz�ciowe, a nast�pnie weryfikuje
	 * poprawno�� oblicze�.
	 * 
	 * @param module
	 *            modu� grupy cyklicznej protoko�u
	 * @return -1 je�li si� nie powiod�, 1 w przeciwnym wypadku
	 */
	public int computeRightKeys(BigInteger module) {

		int firstUserPosition = topology.getUserPosition(userNumber);
		// obliczenie cyklicznego indeksu prawego s�siada
		int cyclicIndex = (firstUserPosition + 1) % topology.size();

		topology.get(firstUserPosition).setRightKey(rightKey);

		// obliczenie prawego klucza prawego s�siada
		BigInteger K = topology.get(cyclicIndex).getYParam().multiply(rightKey)
				.mod(module);
		topology.get(cyclicIndex).setRightKey(K);

		// p�tla po wszystkich uczestnikach poza TYM,
		// pocz�wszy od TEGO uczestnika
		for (int i = firstUserPosition + 1; i < firstUserPosition
				+ topology.size(); i++) {

			// obliczenie cyklicznych indeks�w
			int nextIndex = (i + 1) % topology.size();
			int index = i % topology.size();

			K = (topology.get(nextIndex).getYParam()).multiply(
					topology.get(index).getRightKey()).mod(module);
			topology.get(nextIndex).setRightKey(K);
		}
		// jesli ostatni prawy i pierwszy lewy nie s� r�wne, to znaczy,
		// �e pier�cie� jest niesp�jny lub obliczenia b��dne, wi�c raczej to
		// drugie
		// ABORT protocol
		int lastRightIndex = (firstUserPosition + topology.size() - 1)
				% topology.size();
		BigInteger lastRight = topology.get(lastRightIndex).getRightKey();
		boolean verification = leftKey.equals(lastRight);
		
		if (verification == false)
			// niepowodzenie
			return -1;
		else
			// powodzenie
			return 1;
	}

	/**
	 * Metoda oblicza klucz sesji na podstawie obliczonych prawych kluczy.
	 * 
	 * @param module
	 *            modu� grupy cyklicznej protoko�u
	 */
	public void computeSessionKey(BigInteger module) {

		BigInteger sessionKey = topology.get(0).getRightKey();

		for (int i = 1; i < topology.size(); i++) {

			sessionKey = sessionKey.multiply(topology.get(i).getRightKey())
					.mod(module);
		}
		this.sessionKey = sessionKey;

		System.out.println("sessionKey: " + sessionKey);
	}

	// -------METODY POMOCNICZE---------------------------------------------

	public BigInteger getPartialKey() {
		return partialKey;
	}

	public void setLeftNeighbourKey(BigInteger key) {
		topology.getLeftNeighbour(userNumber).setPartialKey(key);
	}

	public void setRightNeighbourKey(BigInteger key) {
		topology.getRightNeighbour(userNumber).setPartialKey(key);
	}

	public BigInteger getYParameter() {
		return topology.getUser(userNumber).getYParam();
	}

	public RingTopology getTopology() {
		return topology;
	}

	public BigInteger getSessionKey() {
		return sessionKey;
	}
}
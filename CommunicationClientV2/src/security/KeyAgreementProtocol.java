package security;

import java.math.*;
import java.util.*;

/**
 * Klasa implementuj¹ca protokó³ grupowego ustalania klucza. Zawiera metody,
 * które s³u¿¹ do ustalenia klucza sesji wraz z potwierdzeniem autentycznoœci
 * uczestników oraz dynamicznego do³¹czania do konferencji i opuszczania jej.
 * 
 * @author GRUPA 1AS
 * 
 */
public class KeyAgreementProtocol {

	// -------POLA--------------------------------------------------------------

	// parametry topologiczne
	/** numer uczestnika w pierœcieniu */
	private int userNumber;

	/** topologia uczestników protoko³u */
	private RingTopology topology;

	// dla operacji leave
	/** lewy klucz */
	private BigInteger leftKey;
	/** prawy klucz */
	private BigInteger rightKey;

	// ------------ parametry nowej wersji
	/** losowany klucz prywatny */
	// private BigInteger privateKey;
	/** klucz czêœciowey obliczany na podstawie klucza prywatnego */
	private BigInteger partialKey;
	/** wspólny klucz sesji */
	BigInteger sessionKey;

	// -------KONSTRUKTORY--------------------------------------------
	/**
	 * 
	 * @param userNumber
	 *            numer TEGO uczestnika protoko³a
	 */
	public KeyAgreementProtocol(int userNumber) {

		this.leftKey = null;
		this.rightKey = null;

		this.userNumber = userNumber;

		this.topology = new RingTopology();
	}

	// --------METODY PROTOKO£U-------------------------------------------
	/**
	 * Metoda losuje klucz prywatny TEGO uczestnika.
	 */
	public static BigInteger chooseRandomPrivateKey() {

		Random random = new Random();

		return new BigInteger(100, random);
	}

	/**
	 * Metoda oblicza czêœciowy klucz sesji TEGO uczestnika.
	 * 
	 * @param privateKey
	 *            klucz prywanty
	 * @param module
	 *            modu³ grupy cyklicznej protoko³u
	 * @param generator
	 *            generator grupy cyklicznej protoko³u
	 */
	public void computeX(BigInteger privateKey, BigInteger module,
			long generator) {

		BigInteger big = BigInteger.valueOf(generator);

		partialKey = big.modPow(privateKey, module);
	}

	/**
	 * Metoda oblicza lewy klucz tego uczestnika, gdy ju¿ pobrany zostanie klucz
	 * czêœciowy lewego s¹siada. Na czas braku tych danych zatrzymuje w¹tek
	 * obliczaj¹cy klucz.
	 * 
	 * @param privateKey
	 *            klucz prywatny
	 * @param module
	 *            modu³ grupy cyklicznej protoko³u
	 */
	private void computeLeftKey(BigInteger privateKey, BigInteger module) {

		BigInteger X;
		X = topology.getLeftNeighbour(userNumber).getPartialKey();
		leftKey = X.modPow(privateKey, module);
	}

	/**
	 * Metoda oblicza prawy klucz tego uczestnika, gdy ju¿ pobrany zostanie
	 * klucz czêœciowy prawego s¹siada. Na czas braku tych danych zatrzymuje
	 * w¹tek obliczaj¹cy klucz.
	 * 
	 * @param privateKey
	 *            klucz prywatny
	 * @param module
	 *            modu³ grupy cyklicznej protoko³u
	 */
	private void computeRightKey(BigInteger privateKey, BigInteger module) {

		BigInteger X;
		X = topology.getRightNeighbour(userNumber).getPartialKey();
		rightKey = X.modPow(privateKey, module);
	}

	/**
	 * Metoda oblicza klucze lewy i prawy, a nastêpnie parametr Y =
	 * rightKey/leftKey protoko³u dla TEGO uczestnika. Jest to mo¿liwe, gdy
	 * wczeœniej zosta³y wykonane operacje: computeRightKey oraz computeLeftKey.
	 * 
	 * @param privateKey
	 *            klucz prywatny
	 * @param module
	 *            modu³ grupy cyklicznej protoko³u
	 */
	public void computeY(BigInteger privateKey, BigInteger module) {

		computeLeftKey(privateKey, module);
		computeRightKey(privateKey, module);

		BigInteger reversedLeftKey = leftKey.modInverse(module);
		BigInteger y = (rightKey.multiply(reversedLeftKey)).mod(module);

		topology.getUser(userNumber).setYParam(y);
	}

	/**
	 * Metoda oblicza wszystkie prawe klucze czêœciowe, a nastêpnie weryfikuje
	 * poprawnoœæ obliczeñ.
	 * 
	 * @param module
	 *            modu³ grupy cyklicznej protoko³u
	 * @return -1 jeœli siê nie powiod³, 1 w przeciwnym wypadku
	 */
	public int computeRightKeys(BigInteger module) {

		int firstUserPosition = topology.getUserPosition(userNumber);
		// obliczenie cyklicznego indeksu prawego s¹siada
		int cyclicIndex = (firstUserPosition + 1) % topology.size();

		topology.get(firstUserPosition).setRightKey(rightKey);

		// obliczenie prawego klucza prawego s¹siada
		BigInteger K = topology.get(cyclicIndex).getYParam().multiply(rightKey)
				.mod(module);
		topology.get(cyclicIndex).setRightKey(K);

		// pêtla po wszystkich uczestnikach poza TYM,
		// pocz¹wszy od TEGO uczestnika
		for (int i = firstUserPosition + 1; i < firstUserPosition
				+ topology.size(); i++) {

			// obliczenie cyklicznych indeksów
			int nextIndex = (i + 1) % topology.size();
			int index = i % topology.size();

			K = (topology.get(nextIndex).getYParam()).multiply(
					topology.get(index).getRightKey()).mod(module);
			topology.get(nextIndex).setRightKey(K);
		}
		// jesli ostatni prawy i pierwszy lewy nie s¹ równe, to znaczy,
		// ¿e pierœcieñ jest niespójny lub obliczenia b³êdne, wiêc raczej to
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
	 *            modu³ grupy cyklicznej protoko³u
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
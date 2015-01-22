package sync;

import java.math.*;

/**
 * Klasa pozwala przechowywaæ jeden obiekt typu BigInteger, którego brak blokuje
 * w¹tek próbuj¹cy uzyskaæ jego instancjê.
 * 
 * @author GRUPA 1AS
 * 
 */
public class SyncBigInteger {

	//---------------POLA----------------------------------------------------
	private BigInteger value;

	//-----------KONSTRUKTORY----------------------------------------
	public SyncBigInteger() {

		this.value = null;
	}

	//----------METODY-----------------------------------------------
	public SyncBigInteger(BigInteger value) {
		this.value = value;
	}

	/**
	 * Metoda przypisuje wartoœæ do przechowywanego obiektu BigInteger
	 * jednoczeœnie odblokowuj¹c wszystkie w¹tki zablokowane po próbie odwo³ania
	 * siê do niego.
	 * 
	 * @param value
	 *            wartoœæ przypisywana typu BigInteger
	 */
	public synchronized void setValue(BigInteger value) {
		this.value = value;
		// System.out.println("Uruchamiam wszystkie watki w obiekcie " +
		// toString());
		notifyAll();
	}

	/**
	 * Metoda synchroniczna. Podczas próby jej wywo³ania sprawdzany jest stan
	 * obiektu BigInteger, który chcemy pobraæ. Jeœli jest on równy null, w¹tek
	 * dokonuj¹cy próby zostaje zatrzymany do momentu, gdy jego wartoœæ bêdzie
	 * ró¿na od null tj. zostanie wywo³ana metoda setValue.
	 * 
	 * @return wartoœæ obiektu BigInteger
	 */
	public synchronized BigInteger getValue() {
		if (value == null) {
			try {
				// System.out.println("Zatrzymuje watek: " +
				// Thread.currentThread().getName() + " w obiekcie " +
				// toString());
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return value;
	}

	public String toString() {
		if (value == null)
			return "sync null";
		else
			return "sync" + value.toString();
	}
}
package sync;

import java.math.*;

/**
 * Klasa pozwala przechowywa� jeden obiekt typu BigInteger, kt�rego brak blokuje
 * w�tek pr�buj�cy uzyska� jego instancj�.
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
	 * Metoda przypisuje warto�� do przechowywanego obiektu BigInteger
	 * jednocze�nie odblokowuj�c wszystkie w�tki zablokowane po pr�bie odwo�ania
	 * si� do niego.
	 * 
	 * @param value
	 *            warto�� przypisywana typu BigInteger
	 */
	public synchronized void setValue(BigInteger value) {
		this.value = value;
		// System.out.println("Uruchamiam wszystkie watki w obiekcie " +
		// toString());
		notifyAll();
	}

	/**
	 * Metoda synchroniczna. Podczas pr�by jej wywo�ania sprawdzany jest stan
	 * obiektu BigInteger, kt�ry chcemy pobra�. Je�li jest on r�wny null, w�tek
	 * dokonuj�cy pr�by zostaje zatrzymany do momentu, gdy jego warto�� b�dzie
	 * r�na od null tj. zostanie wywo�ana metoda setValue.
	 * 
	 * @return warto�� obiektu BigInteger
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
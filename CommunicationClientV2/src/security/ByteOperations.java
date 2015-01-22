package security;

/**
 * Klasa oferuje szereg operacji bitowych jak z��czenia, wy�wietlanie ci�g�w
 * itp. Pomaga w konwersji ci�g�w bitowych na tekstowe i odwrotnie.
 * 
 * @author GRUPA 1AS
 * 
 */
public class ByteOperations {

	/**
	 * Metoda pozwala wyswietlic pojedynczy bajt jako ci�g zer i jedynek
	 * @param singleByte pojedynczy bajt
	 * @return String w postaci ciagu zer i jedynek
	 */
	public static String toBinaryString(byte singleByte) {

		int power2 = 64;
		String binary = "";

		if (singleByte < 0) {
			int bajt_tmp = -128;
			binary = binary + "1";
			for (int i = 0; i < 7; i++) {

				if (singleByte >= (bajt_tmp + power2)) {
					binary = binary + "1";
					bajt_tmp = bajt_tmp + power2;
				} else {
					binary = binary + "0";
				}
				power2 = power2 / 2;
			}
		} else {
			binary = binary + "0";
			for (int i = 0; i < 7; i++) {

				if (singleByte <= (power2 - 1)) {
					binary = binary + "0";
				} else {
					binary = binary + "1";
					singleByte = (byte) (singleByte - power2);
				}
				power2 = power2 / 2;
			}
		}
		return binary;
	}

	/**
	 * Metoda pozawala wy�wietla� tablice bajt�w jako ci�gi zer i jedynek.
	 * @param byteTable tablica bajt�w
	 * @return String w postaci ci�gu zer i jedynek
	 */
	public static String toBinaryString(byte[] byteTable) {

		String binaryString = "";

		for (int i = 0; i < byteTable.length; i++) {

			binaryString = binaryString
					+ ByteOperations.toBinaryString(byteTable[i]) + " ";
		}

		return binaryString;
	}

	/**
	 * Metoda pozwala zmienia� rozmiar tablic bajt�w. Przydaje si� to
	 * gdy chcemy dopisa� zera po lewej stronie ciagu bit�w, aby zapewni� zgodno�� ze standardem
	 * @param byteTable tablica bajt�w
	 * @param numberOfBytes liczba bajt�w na kt�rych ma by� zapisana wyj�ciowa tablica
	 * @return wyj�ciowa tablica bajt�w
	 */
	public static byte[] resizeToNumberOfBytes(byte[] byteTable,
			int numberOfBytes) {

		byte[] resizedByteTable = new byte[numberOfBytes];

		int i;
		int index = numberOfBytes - 1;
		for (i = byteTable.length - 1; i >= 0; i--) {
			resizedByteTable[index] = byteTable[i];
			index--;
		}
		for (int j = index; j >= 0; j--) {
			resizedByteTable[j] = (byte) 0;
		}

		return resizedByteTable;
	}

	/**
	 * Metoda daje dost�p do operacji konkatenacji na dw�ch tablicach bajt�w
	 * @param leftBytes bajty z lewej strony docelowego ci�gu
	 * @param rightBytes bajty z prawej strony docelowego ci�gu
	 * @return z��czona tablica bajt�w
	 */
	public static byte[] concatenation(byte[] leftBytes, byte[] rightBytes) {

		byte[] newTable = new byte[leftBytes.length + rightBytes.length];

		int index = 0;

		for (int i = 0; i < leftBytes.length; i++) {
			newTable[index] = leftBytes[i];
			index++;
		}
		for (int i = 0; i < rightBytes.length; i++) {
			newTable[index] = rightBytes[i];
			index++;
		}

		return newTable;
	}

	/**
	 * Metoda s�u�y do fragmentacji tablic bajtowych
	 * @param byteTable tablica, kt�r� chcemy podzieli�
	 * @param firstIndex pierwszy indeks fragmentu
	 * @param lastIndex ostatni indeks fragmentu
	 * @return tablica bajt�w wyci�ta z tablicy wej�ciowej, ograniczona argumentami
	 */
	public static byte[] divideByteTable(byte[] byteTable, int firstIndex,
			int lastIndex) {

		byte[] newTable = new byte[lastIndex - firstIndex + 1];

		int index = 0;
		for (int i = firstIndex; i <= lastIndex; i++) {
			newTable[index] = byteTable[i];
			index++;
		}

		return newTable;
	}
}
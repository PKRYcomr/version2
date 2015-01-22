package security;

/**
 * Klasa oferuje szereg operacji bitowych jak z³¹czenia, wyœwietlanie ci¹gów
 * itp. Pomaga w konwersji ci¹gów bitowych na tekstowe i odwrotnie.
 * 
 * @author GRUPA 1AS
 * 
 */
public class ByteOperations {

	/**
	 * Metoda pozwala wyswietlic pojedynczy bajt jako ciêg zer i jedynek
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
	 * Metoda pozawala wyœwietlaæ tablice bajtów jako ci¹gi zer i jedynek.
	 * @param byteTable tablica bajtów
	 * @return String w postaci ci¹gu zer i jedynek
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
	 * Metoda pozwala zmieniaæ rozmiar tablic bajtów. Przydaje siê to
	 * gdy chcemy dopisaæ zera po lewej stronie ciagu bitów, aby zapewniæ zgodnoœæ ze standardem
	 * @param byteTable tablica bajtów
	 * @param numberOfBytes liczba bajtów na których ma byæ zapisana wyjœciowa tablica
	 * @return wyjœciowa tablica bajtów
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
	 * Metoda daje dostêp do operacji konkatenacji na dwóch tablicach bajtów
	 * @param leftBytes bajty z lewej strony docelowego ci¹gu
	 * @param rightBytes bajty z prawej strony docelowego ci¹gu
	 * @return z³¹czona tablica bajtów
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
	 * Metoda s³u¿y do fragmentacji tablic bajtowych
	 * @param byteTable tablica, któr¹ chcemy podzieliæ
	 * @param firstIndex pierwszy indeks fragmentu
	 * @param lastIndex ostatni indeks fragmentu
	 * @return tablica bajtów wyciêta z tablicy wejœciowej, ograniczona argumentami
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
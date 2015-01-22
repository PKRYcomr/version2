package security;

import java.math.*;

import sync.*;

/**
 * Klasa reprezentuje model informacyjny uczestnika widzianego przez klienta.
 * 
 * @author GRUPA 1AS
 * 
 */
public class User {

	// -------POLA-------------------------------------------------------
	/** unikalny numer u¿ytkownika komunikatora */
	private int userNumber;
	/** status gotowoœci do obliczania grupowego klucza */
	private boolean readyStatus;
	/** publiczny klucz czêœciowy uczestnika */
	private SyncBigInteger partialKey;
	/** publiczny parametr Y uczestnika */
	private SyncBigInteger yParam;
	/** prawy klucz danego uczestnika */
	private SyncBigInteger rightKey;

	// RSA
	private BigInteger publicExponent;
	private BigInteger publicModulus;

	// -------KONSTRUKTORY-----------------------------------------------------
	public User(int userNumber) {

		this.userNumber = userNumber;
		this.readyStatus = false;

		partialKey = new SyncBigInteger();
		yParam = new SyncBigInteger();
		rightKey = new SyncBigInteger();
	}

	// -------METODY--------------------------------------------------------------
	public void setReadyStatus() {
		readyStatus = true;
	}

	public void unsetReadyStatus() {
		readyStatus = false;
	}

	public String toString() {
		String summary = "";
		summary = summary + userNumber;
		if (readyStatus == true)
			summary = summary + " R ";
		else
			summary = summary + " U ";
		summary = summary + ", ";

		return summary;
	}

	// -------GETTER SETTER--------------------------------------------------
	public int getUserNumber() {
		return userNumber;
	}

	public void setPartialKey(BigInteger partialKey) {
		this.partialKey.setValue(partialKey);
	}

	public void setYParam(BigInteger yParam) {
		this.yParam.setValue(yParam);
	}

	public void setRightKey(BigInteger rightKey) {
		this.rightKey.setValue(rightKey);
	}

	public BigInteger getRightKey() {
		return this.rightKey.getValue();
	}

	public BigInteger getPartialKey() {
		return partialKey.getValue();
	}

	public BigInteger getYParam() {
		return yParam.getValue();
	}

	public boolean getReadyStatus() {
		return readyStatus;
	}

	public BigInteger getPublicExponent() {
		return publicExponent;
	}

	public void setPublicExponent(BigInteger publicExponent) {
		this.publicExponent = publicExponent;
	}

	public BigInteger getPublicModulus() {
		return publicModulus;
	}

	public void setPublicModulus(BigInteger publicModulus) {
		this.publicModulus = publicModulus;
	}
}
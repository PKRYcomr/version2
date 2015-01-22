package network;

import java.io.*;

import management.*;

/**
 * Klasa s�u�y do wysy�ania wiadomo�ci w oddzielnym w�tku.
 * 
 * @author GRUPA 1AS
 * 
 */
public class Sender extends Thread {

	ClientData data;

	String message;

	public Sender(String message) {

		this.data = ClientData.getInstance();
		this.message = message;

		setName("Sender");
	}

	public void run() {

		try {
			PrintWriter out = new PrintWriter(data.getLocalSocket()
					.getOutputStream(), true);

			out.println(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("To server   | " + message);
	}
}
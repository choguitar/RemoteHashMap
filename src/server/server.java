package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class server {

	private void connectClient() {
		
	}
	
	public void fileReceive() {
		
	}
	
	public static void main(String[] args) {
		BufferedReader in = null;
		PrintWriter out = null;
		
		ServerSocket serverSocket = null;
		Socket socket = null;
		
		try {
			serverSocket = new ServerSocket(8000);
			socket = serverSocket.accept();
			
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

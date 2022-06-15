import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

class ConnectionHandler implements Runnable {

	private Socket client;
	private BufferedReader in;
	private PrintWriter out;
	private String nickname;
	private ArrayList<ConnectionHandler> connections;

	public ConnectionHandler(Socket client, ArrayList<ConnectionHandler> connections) {
		this.client = client;
		this.connections = connections;
	}

	public void broadCast(String message) {
		for (ConnectionHandler connection : connections) {
			if (connection != null) {
				connection.sendMessage(message);
			}
		}
	}

	@Override
	public void run() {

		try {
			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out.println("Please enter your username: ");
			nickname = in.readLine();

			while (nickname.isEmpty()) {
				out.println("User name cannot be empty!!! \nPlease enter valid name...");
				nickname = in.readLine();
			}

			System.out.println(nickname + ": " + "connected");
			broadCast(nickname + " entered the chat!");

			String message;
			while ((message = in.readLine()) != null) {
				userInputs(message);
			}
		} catch (IOException e) {
			shutdown();
		}
	}

	public void userInputs(String message) {
		if (message.startsWith("/uname")) {
			String[] messageSplit = message.split(" ", 2);
			if (messageSplit.length == 2) {
				broadCast(nickname + " changed his name to " + messageSplit[1]);
				nickname = messageSplit[1];
				out.println("Successfully change name to " + nickname);
			} else {
				out.println("No name provided");
			}
		} else if (message.startsWith("/q")) {
			broadCast(nickname + ": left the chat");
			shutdown();
		} else {
			broadCast(nickname + ": " + message);
		}
	}

	public void sendMessage(String message) {
		out.println(message);
	}

	public void shutdown() {
		try {
			in.close();
			out.close();
			if (!client.isClosed()) {
				client.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
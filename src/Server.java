import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

	private ServerSocket server;
	private ArrayList<ConnectionHandler> connections;
	private boolean done;
	private ExecutorService pool;

	public Server() {
		connections = new ArrayList<>();
		done = false;
	}

	@Override
	public void run() {

		try {
			server = new ServerSocket(8989);
			pool = Executors.newCachedThreadPool();
			while (!done) {
				Socket client = server.accept();
				ConnectionHandler handler = new ConnectionHandler(client);
				connections.add(handler);
				pool.execute(handler);
			}

		} catch (Exception e) {
			shutdown();
		}

	}

	public void broadCast(String message) {
		for (ConnectionHandler connection : connections) {
			if (connection != null) {
				connection.sendMessage(message);
			}
		}
	}

	public void shutdown() {

		try {
			done = true;
			if (!server.isClosed()) {
				server.close();
			}

			for (ConnectionHandler ch : connections) {
				ch.shutdown();
			}
		} catch (Exception e) {
			// Ignore - can't handle
		}
	}

	class ConnectionHandler implements Runnable {

		private Socket client;
		private BufferedReader in;
		private PrintWriter out;
		private String nickName;

		public ConnectionHandler(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {

			try {
				out = new PrintWriter(client.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				out.println("Please enter a username: ");
				nickName = in.readLine();

				while (nickName.isEmpty()) {
					out.println("user name cannot be empty \nPlease enter valid name.");
					nickName = in.readLine();
				}
				System.out.println(nickName + ": " + "connected");

				broadCast(nickName + " entered the chat!");
				String message;

				while ((message = in.readLine()) != null) {
					if (message.startsWith("/uname")) {

						String[] messageSplit = message.split(" ", 2);
						if (messageSplit.length == 2) {
							broadCast(nickName + " renamed themselves to " + messageSplit[1]);
							System.out.println(nickName + " renamed themselves to " + messageSplit[1]);
							nickName = messageSplit[1];
							out.println("Successfully change name to " + nickName);
						} else {
							out.println("No name provided");
						}

					} else if (message.startsWith("/q")) {

						broadCast(nickName + ": left the chat");
						shutdown();

					} else {
						broadCast(nickName + ": " + message);
					}
				}
			} catch (IOException e) {
				shutdown();
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
				// Ignore
			}
		}
	}

	public static void main(String[] args) {

		Server server = new Server();
		server.run();
	}
}

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* 
 * TODO : How many are there in the chat room 
 */
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
				ConnectionHandler handler = new ConnectionHandler(client, connections);
				connections.add(handler);
				pool.execute(handler);
			}
		} catch (Exception e) {
			shutdown();
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
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}
}

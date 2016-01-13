package pw.tud.rcon.client;

import pw.tud.rcon.protocol.RconClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RconClientLauncher implements Runnable, AutoCloseable {
    private RconClient client;

    public RconClientLauncher(String host, int port) {
        client = new RconClient(host, port);
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java -jar <jarfile> <host> <port> <password>");
            return;
        }

        try {
            RconClientLauncher program = new RconClientLauncher(args[0], Integer.parseInt(args[1]));
            program.connectAndAuthenticate(args[2]);
            new Thread(program, "Console thread").start();
            program.readPackets();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectAndAuthenticate(String password) throws IOException {
        client.connect();
        if (!client.authenticate(password)) {
            System.out.println("Auth failed.");
        } else {
            System.out.println("Logged in.");
        }
    }

    public void readPackets() throws IOException {
        while (client.connected()) {
            System.out.println(client.read().getBody());
        }
    }

    @Override
    public void run() {
        try {
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = console.readLine()) != null) {
                if (line.toLowerCase().equals("disconnect")) {
                    close();
                    break;
                }
                client.executeCommand(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException
    {
        client.close();
    }
}

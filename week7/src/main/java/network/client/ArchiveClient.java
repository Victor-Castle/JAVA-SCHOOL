package network.client;

import network.Request;
import network.Response;

import java.io.*;
import java.net.Socket;

public class ArchiveClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public ArchiveClient() throws IOException {
        socket = new Socket(SERVER_IP, SERVER_PORT);
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
    }

    public Response sendRequest(Request request) throws IOException, ClassNotFoundException {
        outputStream.writeObject(request);
        outputStream.flush();
        return (Response) inputStream.readObject();
    }

    public void close() throws IOException {
        if (inputStream != null) inputStream.close();
        if (outputStream != null) outputStream.close();
        if (socket != null) socket.close();
    }
}
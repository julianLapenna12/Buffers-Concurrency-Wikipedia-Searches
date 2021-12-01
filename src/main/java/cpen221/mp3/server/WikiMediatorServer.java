package cpen221.mp3.server;

import com.google.gson.Gson;
import cpen221.mp3.wikimediator.WikiMediator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class WikiMediatorServer {

    private ServerSocket serverSocket;
    private Gson gson;
    private WikiMediator mediator;
    private int maxThreads;
    private int numThread;

    /**
     * Start a server at a given port number, with the ability to process
     * upto n requests concurrently.
     *
     * @param port the port number to bind the server to, 9000 <= {@code port} <= 9999
     * @param n the number of concurrent requests the server can handle, 0 < {@code n} <= 32
     * @param wikiMediator the WikiMediator instance to use for the server, {@code wikiMediator} is not {@code null}
     */
    public WikiMediatorServer(int port, int n, WikiMediator wikiMediator) {
        try{
            serverSocket = new ServerSocket(port);
            maxThreads = n;
            mediator = wikiMediator;
            //TODO read the state of saved data from disk
            //Start the server
            serve();
        }
        catch (IOException e){
            System.out.println("Could Not Connect!");
        }
    }

    private void serve () throws IOException {
        while(true){
            final Socket socket = serverSocket.accept();
            Thread handler = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        try{
                            handle(socket);
                        }
                        finally{
                            socket.close();
                        }
                    }
                    catch (IOException ioe){
                        ioe.printStackTrace();
                    }
                }
            });
        }
    }

    private void handle(Socket socket) throws IOException{
        System.err.println("client connected");
        //Multithreaded Function to handle requests
        Gson gsonReader = new Gson();
        //Try-with-resources declaring the input and output stream
        try(BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()), true))
        {
            for (String line = in.readLine(); line != null;
                 line = in.readLine()) {
                    WikiRequest request = gsonReader.fromJson(line, WikiRequest.class);
                    out.println(handleRequest(request, gsonReader));
            }
        }
    }

    private String handleRequest(WikiRequest request, Gson gson){
        WikiResponse response;
        if(request.type == "search"){

        }
        else if (request.type == "getPage"){

        }

        else if (request.type == "stop"){

        }

        return gson.toJson(request, WikiRequest.class);
    }

    //Handles shutdown by writing state of Wikimediator to disk
    private void shutdown() throws IOException {
        //TODO Write the state of WikiMediator to disk
        serverSocket.close();
    }

}

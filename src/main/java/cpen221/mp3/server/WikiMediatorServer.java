package cpen221.mp3.server;

import com.google.gson.Gson;
import cpen221.mp3.wikimediator.WikiMediator;
import jdk.jfr.StackTrace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.*;

public class WikiMediatorServer {

    private ServerSocket serverSocket;
    private WikiMediator mediator;
    private int maxThreads;
    private int numThread = 0;
    private boolean shutdown = false;

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
        }
        catch (IOException e){
            throw new RuntimeException();
        }

    }

    public void serve () {
        shutdown = false;
        while(!serverSocket.isClosed()) {
            if(shutdown){
                shutdown();
            }
            try{
                final Socket socket = serverSocket.accept();
                numThread++;
                if(numThread <= maxThreads){
                    Thread handler = new Thread(() -> {
                        try {
                            try {
                                handle(socket);
                            } finally {
                                socket.close();
                            }
                        } catch (IOException ioe) {
                            throw new RuntimeException();
                        }
                    });
                    handler.start();
                }
            }
            catch (IOException ioe){
                throw new RuntimeException();
            }
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
                    //Handles the specific case of stopping the server
                    if(request.type.equals("stop") ){
                        WikiResponse response = new WikiResponse(request.id, "bye");
                        out.println(gsonReader.toJson(response));
                        shutdown = true;
                    }
                    else out.println(handleRequest(request, gsonReader));
            }
        }
    }

    private String handleRequest(WikiRequest request, Gson gson){
        WikiResponse response = new WikiResponse();
        response.id = request.id;
        //Creates new Thread to allow for timeout
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Callable() {
            @Override
            public String call() throws Exception {
                switch (request.type){
                    case "search":
                        response.response = mediator.search(request.query, request.maxItems);
                        return "success";
                    case "getPage":
                        response.response = mediator.getPage(request.pageTitle);
                        return "success";
                    case "zeitgeist":
                        response.response = mediator.zeitgeist(request.limit);
                        return "success";
                    default:
                        response.response = "command not found";
                        return "failed";
                }

            }
        });
        try{
            if(request.timeout != null){
                response.status = future.get(2, TimeUnit.SECONDS);
            }
            else{
                response.status = future.get();
            }
        }
        catch(TimeoutException e){
            response.status = "failed";
            response.response = "Operation timed out";
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return gson.toJson(response, WikiResponse.class);
    }

    //Handles shutdown by writing state of Wikimediator to disk
    private void shutdown() {
        //TODO Write the state of WikiMediator to disk
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

}

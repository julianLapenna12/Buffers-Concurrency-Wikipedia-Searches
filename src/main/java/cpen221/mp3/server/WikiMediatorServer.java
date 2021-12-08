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
import java.util.concurrent.*;

public class WikiMediatorServer {

    private final ServerSocket serverSocket;
    private final WikiMediator mediator;
    private boolean shutdown = false;
    private Semaphore blocker;

    /**
     * Start a server at a given port number, with the ability to process
     * upto n clients concurrently.
     * if greater than n clients are connected, then the server will block until a client disconnects,
     * creating space for another clients request to be handled.
     * @param port the port number to bind the server to, 9000 <= {@code port} <= 9999
     * @param n the number of concurrent requests the server can handle, 0 < {@code n} <= 32
     * @param wikiMediator the WikiMediator instance to use for the server, {@code wikiMediator} is not {@code null}
     */
    public WikiMediatorServer(int port, int n, WikiMediator wikiMediator) {
        try{
            serverSocket = new ServerSocket(port);
            mediator = wikiMediator;
            blocker = new Semaphore(n);
        }
        catch (IOException e){
            throw new RuntimeException();
        }

    }

    /**
     *Calling Serve causes the server to start listening for client sockets,
     * blocking the thread on which serve was called.
     * Each request sent to the server by a client should be a JSON formatted String
     * Containing an ID for each request, a request parameter specifying which method
     * from the mediator is being called, the required parameters for the respective
     * method from WikiMediator, and optionally a Timeout parameter specifying how long
     * to try the request for before timing out.
     */
    public void serve () {
        shutdown = false;
        while(!serverSocket.isClosed()) {
            if(shutdown){
                shutdown();
            }
            try{
                blocker.acquire();
                final Socket socket = serverSocket.accept();
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
                blocker.release();
            }
            catch (IOException | InterruptedException ioe){
                throw new RuntimeException();
            }
        }
    }

    /**
     * When a Client opens a socket to the server, handle() handles the requests from
     * the client and returns the expected information
     * @param socket the socket whose inputs will be handled and to whom the output is written
     * @throws IOException
     */
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

    /**
     * Given a request converted from a JSON formatted string,
     * creates a JSON formatted string as a response, calling the
     * function from WikiMediator specified in the request
     * @param request the request specifying which method from WikiMediator
     *               to be called, the required parameters, and the ID of the request
     * @param gson the instance of GSON used to convert the output of WikiMediator to a JSON formatted string
     * @return a JSON formatted String with the ID corresponding to the request,
     * the status whether the request succeeded or failed, and the information from the mediator
     */
    private String handleRequest(WikiRequest request, Gson gson){
        WikiResponse response = new WikiResponse();
        response.id = request.id;
        //Creates new Thread to allow for timeout
        //Code In part take from:
        //https://stackoverflow.com/questions/17233038/how-to-implement-synchronous-method-timeouts-in-java
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Callable() {
            @Override
            public String call() throws Exception {
                switch (request.type){
                    case "search":
                        response.response = mediator.search(request.query, request.limit);
                        return "success";
                    case "getPage":
                        response.response = mediator.getPage(request.pageTitle);
                        return "success";
                    case "zeitgeist":
                        response.response = mediator.zeitgeist(request.limit);
                        return "success";
                    case "shortestPath":
                        response.response = mediator.shortestPath(request.pageTitle1, request.pageTitle2, request.timeout);
                        return "success";
                    case "trending":
                        response.response = mediator.trending(request.timeLimitInSeconds, request.maxItems);
                        return "success";
                    case "windowedPeakLoad":
                        if(request.timeWindowInSeconds != null){
                            response.response = mediator.windowedPeakLoad(request.timeWindowInSeconds);
                        }
                        else response.response = mediator.windowedPeakLoad();
                        return "success";
                    default:
                        response.response = "command not found";
                        return "failed";
                }
            }
        });
        //Run thread and check for timeout
        try{
            if(request.timeout != null){
                response.status = future.get(request.timeout, TimeUnit.SECONDS);
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
    /**
     * Shuts down the server, writing the current state of mediator to disk at /local
     */
    private void shutdown() {
        mediator.writeToFile();
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

}

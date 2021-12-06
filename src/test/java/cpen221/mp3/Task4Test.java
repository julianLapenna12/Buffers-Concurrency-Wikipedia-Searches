package cpen221.mp3;


import com.google.gson.Gson;
import cpen221.mp3.server.WikiMediatorServer;
import cpen221.mp3.server.WikiRequest;
import cpen221.mp3.server.WikiResponse;
import cpen221.mp3.wikimediator.WikiMediator;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.Socket;

public class Task4Test {
    private static Gson gson;
    private static TestClient client;

    @BeforeAll
    public static void setUpServer(){

        WikiMediator mediator = new WikiMediator(10, 10);
        WikiMediatorServer server = new WikiMediatorServer(6666, 32, mediator);
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                server.serve();
            }
        });
        serverThread.start();
        gson = new Gson();
        client = new TestClient();
        client.startConnection("127.0.0.1", 6666);
    }

    @Test
    public void testShutdown(){
        WikiRequest req = new WikiRequest();
        req.id = "1";
        req.type = "stop";
        String message = gson.toJson(req);
        String response = client.sendMessage(message);
        System.out.println(response);
        Assertions.assertEquals("bye", gson.fromJson(response, WikiResponse.class).response);
    }

    @Test
    public void testGetPage(){
        WikiRequest req = buildReq("1", "getPage");
        req.pageTitle = "Barack Obama";
        String message = gson.toJson(req);
        String response = client.sendMessage(message);
        Assertions.assertEquals("success", gson.fromJson(response, WikiResponse.class).status);
    }

    public WikiRequest buildReq(String id, String type){
        WikiRequest req = new WikiRequest();
        req.id = id;
        req.type = type;
        return req;
    }
}

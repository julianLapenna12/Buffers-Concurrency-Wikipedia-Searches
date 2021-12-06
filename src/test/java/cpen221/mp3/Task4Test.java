package cpen221.mp3;


import com.google.gson.Gson;
import cpen221.mp3.server.WikiMediatorServer;
import cpen221.mp3.server.WikiRequest;
import cpen221.mp3.wikimediator.WikiMediator;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.Socket;

public class Task4Test {
    private static Gson gson;

    @BeforeAll
    public static void setUpServer(){
        WikiMediator mediator = new WikiMediator(100, 32);
        WikiMediatorServer server = new WikiMediatorServer(6666, 32, mediator);
        server.serve();
        gson = new Gson();
    }

    @Test
    public static void testClient(){
        TestClient client = new TestClient();
        client.startConnection("127.0.0.1", 6666);
        WikiRequest req = new WikiRequest();
        req.id = "1";
        req.type = "stop";
        String message = gson.toJson(req);
        System.out.println(client.sendMessage(message));
        Assertions.assertTrue(true);
    }
}

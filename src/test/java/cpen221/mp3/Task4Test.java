package cpen221.mp3;


import com.google.gson.Gson;
import cpen221.mp3.server.WikiMediatorServer;
import cpen221.mp3.server.WikiRequest;
import cpen221.mp3.server.WikiResponse;
import cpen221.mp3.wikimediator.WikiMediator;
import org.fastily.jwiki.core.Wiki;
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
        WikiMediatorServer server = new WikiMediatorServer(6666, 4, mediator);
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                server.serve();
            }
        });
        serverThread.start();
        gson = new Gson();
        client = buildClient("127.0.0.1", 6666);
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

    @Test
    public void testMultiReq(){
        WikiResponse mes = makeRequest(client, buildPageRequest("1", "Abraham Lincoln"));
        WikiResponse mes2 = makeRequest(client, buildPageRequest("2", "Mark Van Raamsdonk"));
        Assertions.assertEquals("success", mes.status);
        Assertions.assertEquals("success", mes2.status);
    }

    @Test
    public void searchReq(){
        WikiResponse mes = makeRequest(client, buildSearchRequest("1", "Barack Obama", 5));
        Assertions.assertEquals("success", mes.status);
    }

    @Test
    public void zeitReq(){
        WikiResponse mes = makeRequest(client, buildZeitgeist("2", 10));
        Assertions.assertEquals("success", mes.status);
    }

    @Test
    public void shortestReq(){
        WikiResponse mes= makeRequest(client, buildShortestPath("1", "Mark Van Raamsdonk", "George Smoot", 100));
        Assertions.assertEquals("success", mes.status);
    }

    @Test
    public void trendingReq(){
        WikiResponse mes= makeRequest(client, buildTrending("1", 10, 10));
        Assertions.assertEquals("success", mes.status);
    }

    @Test
    public void peakLoadReq(){
        WikiResponse mes= makeRequest(client, buildWindowedPeakLoad("1", 10));
        Assertions.assertEquals("success", mes.status);
    }

    @Test
    public void searchReqManyClient(){
        TestClient client2 = buildClient("127.0.0.1", 6666);
        TestClient client3 = buildClient("127.0.0.1", 6666);
        TestClient client4 = buildClient("127.0.0.1", 6666);
        TestClient client5 = buildClient("127.0.0.1", 6666);
        WikiResponse mes = makeRequest(client, buildSearchRequest("1", "Barack Obama", 5));
        WikiResponse mes2 = makeRequest(client2, buildSearchRequest("1", "Barack Obama", 5));
        WikiResponse mes3 = makeRequest(client3, buildSearchRequest("1", "Barack Obama", 5));
        WikiResponse mes4 = makeRequest(client4, buildSearchRequest("1", "Barack Obama", 5));
        client2.stopConnection();
        WikiResponse mes5 = makeRequest(client5, buildSearchRequest("1", "Barack Obama", 5));

        Assertions.assertEquals("success", mes.status);
        Assertions.assertEquals("success", mes2.status);
        Assertions.assertEquals("success", mes3.status);
        Assertions.assertEquals("success", mes4.status);
        Assertions.assertEquals("success", mes5.status);

    }

    @Test
    public void searchReqManyClientSD(){
        TestClient client2 = buildClient("127.0.0.1", 6666);
        TestClient client3 = buildClient("127.0.0.1", 6666);
        TestClient client4 = buildClient("127.0.0.1", 6666);
        TestClient client5 = buildClient("127.0.0.1", 6666);
        WikiResponse mes = makeRequest(client, buildSearchRequest("1", "Barack Obama", 5));
        WikiResponse mes2 = makeRequest(client2, buildSearchRequest("1", "Mark Van Raamsdonk", 5));
        WikiResponse mes3 = makeRequest(client3, buildSearchRequest("1", "Cancer", 5));
        WikiResponse mes4 = makeRequest(client4, buildSearchRequest("1", "Dollar", 5));
        client2.stopConnection();
        WikiResponse mes5 = makeRequest(client5, buildSearchRequest("1", "Computer", 5));
        WikiResponse mes7 = makeRequest(client5, buildTrending("Trending", 10, 5));
        WikiResponse mes6 = makeRequest(client4, buildShutdown("1"));

        Assertions.assertEquals("success", mes.status);
        Assertions.assertEquals("success", mes2.status);
        Assertions.assertEquals("success", mes3.status);
        Assertions.assertEquals("success", mes4.status);
        Assertions.assertEquals("success", mes5.status);
    }

    @Test
    public void multiShortestReq(){
        TestClient client2 = buildClient("127.0.0.1", 6666);
        WikiResponse mes= makeRequest(client, buildShortestPath("1", "Mark Van Raamsdonk", "George Smoot", 100));
        WikiResponse mes2= makeRequest(client2, buildShortestPath("2", "Mark Van Raamsdonk", "Philosophy", 100));
        WikiResponse mes3= makeRequest(client, buildShortestPath("3", "John Horton Conway", "American Civil War", 100));
        WikiResponse mes4 = makeRequest(client, buildShutdown("Shutdown"));
        Assertions.assertEquals("success", mes.status);
        Assertions.assertEquals("success", mes2.status);
        Assertions.assertEquals("success", mes3.status);
        Assertions.assertEquals("success", mes4.status);
    }

    public WikiRequest buildReq(String id, String type){
        WikiRequest req = new WikiRequest();
        req.id = id;
        req.type = type;
        return req;
    }

    public WikiRequest buildReq(String id, String type, Integer timeout){
        WikiRequest req = new WikiRequest();
        req.id = id;
        req.type = type;
        req.timeout = timeout;
        return req;
    }

    public WikiRequest buildPageRequest(String id, String page){
        WikiRequest req = buildReq(id, "getPage");
        req.pageTitle = page;
        req.timeout = 20;
        return req;
    }

    public WikiRequest buildSearchRequest(String id, String query, int limit){
        WikiRequest req = buildReq(id, "search");
        req.query = query;
        req.limit = limit;
        return req;
    }

    public WikiRequest buildZeitgeist(String id, int limit){
        WikiRequest req = buildReq(id, "zeitgeist");
        req.limit = limit;
        return req;
    }

    public WikiRequest buildTrending(String id, int timeLimit, int maxItems){
        WikiRequest req = buildReq(id, "trending");
        req.timeLimitInSeconds = timeLimit;
        req.maxItems = maxItems;
        return req;
    }

    public WikiRequest buildShutdown(String id){
        WikiRequest req = buildReq(id, "stop");
        return req;
    }

    public WikiRequest buildWindowedPeakLoad(String id, int timeWindow){
        WikiRequest req = buildReq(id, "windowedPeakLoad");
        req.timeWindowInSeconds = timeWindow;
        return req;
    }

    public WikiRequest buildShortestPath(String id, String page1, String page2, int timeout){
        WikiRequest req = buildReq(id, "shortestPath");
        req.pageTitle1 = page1;
        req.pageTitle2 = page2;
        req.timeout = timeout;
        return req;
    }


    public static TestClient buildClient(String ip, int port){
        TestClient client = new TestClient();
        client.startConnection(ip, port);
        return client;
    }

    public WikiResponse makeRequest(TestClient client, WikiRequest req){
        String message = gson.toJson(req);
        String response = client.sendMessage(message);
        System.out.println(response);
        return gson.fromJson(response, WikiResponse.class);
    }
}

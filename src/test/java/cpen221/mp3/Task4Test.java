package cpen221.mp3;


import com.google.gson.Gson;
import cpen221.mp3.server.WikiMediatorServer;
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
        WikiRequestT req = new WikiRequestT();
        req.id = "1";
        req.type = "stop";
        String message = gson.toJson(req);
        String response = client.sendMessage(message);
        System.out.println(response);
        Assertions.assertEquals("bye", gson.fromJson(response, WikiResponseT.class).response);
    }

    @Test
    public void testGetPage(){
        WikiRequestT req = buildReq("1", "getPage");
        req.pageTitle = "Barack Obama";
        String message = gson.toJson(req);
        String response = client.sendMessage(message);
        Assertions.assertEquals("success", gson.fromJson(response, WikiResponseT.class).status);
    }

    @Test
    public void testMultiReq(){
        WikiResponseT mes = makeRequest(client, buildPageRequest("1", "Abraham Lincoln"));
        WikiResponseT mes2 = makeRequest(client, buildPageRequest("2", "Mark Van Raamsdonk"));
        Assertions.assertEquals("success", mes.status);
        Assertions.assertEquals("success", mes2.status);
    }

    @Test
    public void searchReq(){
        WikiResponseT mes = makeRequest(client, buildSearchRequest("1", "Barack Obama", 5));
        Assertions.assertEquals("success", mes.status);
    }

    @Test
    public void zeitReq(){
        WikiResponseT mes = makeRequest(client, buildZeitgeist("2", 10));
        Assertions.assertEquals("success", mes.status);
    }

    @Test
    public void shortestReq(){
        WikiResponseT mes= makeRequest(client, buildShortestPath("1", "Mark Van Raamsdonk", "George Smoot", 100));
        Assertions.assertEquals("success", mes.status);
    }

    @Test
    public void trendingReq(){
        WikiResponseT mes= makeRequest(client, buildTrending("1", 10, 10));
        Assertions.assertEquals("success", mes.status);
    }

    @Test
    public void peakLoadReq(){
        WikiResponseT mes= makeRequest(client, buildWindowedPeakLoad("1", 10));
        Assertions.assertEquals("success", mes.status);
    }

    @Test
    public void searchReqManyClient(){
        TestClient client2 = buildClient("127.0.0.1", 6666);
        TestClient client3 = buildClient("127.0.0.1", 6666);
        TestClient client4 = buildClient("127.0.0.1", 6666);
        TestClient client5 = buildClient("127.0.0.1", 6666);
        WikiResponseT mes = makeRequest(client, buildSearchRequest("1", "Barack Obama", 5));
        WikiResponseT mes2 = makeRequest(client2, buildSearchRequest("1", "Barack Obama", 5));
        WikiResponseT mes3 = makeRequest(client3, buildSearchRequest("1", "Barack Obama", 5));
        WikiResponseT mes4 = makeRequest(client4, buildSearchRequest("1", "Barack Obama", 5));
        client2.stopConnection();
        WikiResponseT mes5 = makeRequest(client5, buildSearchRequest("1", "Barack Obama", 5));

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
        WikiResponseT mes = makeRequest(client, buildSearchRequest("1", "Barack Obama", 5));
        WikiResponseT mes2 = makeRequest(client2, buildSearchRequest("1", "Mark Van Raamsdonk", 5));
        WikiResponseT mes3 = makeRequest(client3, buildSearchRequest("1", "Cancer", 5));
        WikiResponseT mes4 = makeRequest(client4, buildSearchRequest("1", "Dollar", 5));
        client2.stopConnection();
        WikiResponseT mes5 = makeRequest(client5, buildSearchRequest("1", "Computer", 5));
        WikiResponseT mes7 = makeRequest(client5, buildTrending("Trending", 10, 5));
        WikiResponseT mes6 = makeRequest(client4, buildShutdown("1"));

        Assertions.assertEquals("success", mes.status);
        Assertions.assertEquals("success", mes2.status);
        Assertions.assertEquals("success", mes3.status);
        Assertions.assertEquals("success", mes4.status);
        Assertions.assertEquals("success", mes5.status);
    }

    @Test
    public void multiShortestReq(){
        TestClient client2 = buildClient("127.0.0.1", 6666);
        WikiResponseT mes= makeRequest(client, buildShortestPath("1", "Mark Van Raamsdonk", "George Smoot", 100));
        WikiResponseT mes2= makeRequest(client2, buildShortestPath("2", "Mark Van Raamsdonk", "Philosophy", 100));
        WikiResponseT mes3= makeRequest(client, buildShortestPath("3", "John Horton Conway", "American Civil War", 100));
        WikiResponseT mes4 = makeRequest(client, buildShutdown("Shutdown"));
        Assertions.assertEquals("success", mes.status);
        Assertions.assertEquals("success", mes2.status);
        Assertions.assertEquals("success", mes3.status);
        Assertions.assertEquals("success", mes4.status);
    }

    @Test
    public void timeoutTest(){
        WikiResponseT mes= makeRequest(client, buildShortestPath("1", "Mark Van Raamsdonk", "Philosophy", 1));
        Assertions.assertEquals("failed", mes.status);
    }


    @Test
    public void testManyThread(){
        TestClient client2 = buildClient("127.0.0.1", 6666);
        TestClient client3 = buildClient("127.0.0.1", 6666);

        Thread clientThread1 = new Thread(()->{
            WikiResponseT mes2 = makeRequest(client2, buildSearchRequest("1", "Barack Obama", 5));
            WikiResponseT mes6 = makeRequest(client2, buildShutdown("2"));
            Assertions.assertEquals("success", mes6.status);
        });
        Thread clientThread2 = new Thread(()->{
            WikiResponseT mes2 = makeRequest(client, buildSearchRequest("3", "Barack Obama", 5));
            WikiResponseT mes3= makeRequest(client, buildShortestPath("4", "John Horton Conway", "American Civil War", 100));
            Assertions.assertEquals("success", mes2.status);
            Assertions.assertEquals("failed", mes3.status);
        });

        clientThread1.start();
        clientThread2.start();
        WikiResponseT mes = makeRequest(client3, buildSearchRequest("5", "String Theory", 5, 5));
        Assertions.assertEquals("success", mes);

    }

    public WikiRequestT buildReq(String id, String type){
        WikiRequestT req = new WikiRequestT();
        req.id = id;
        req.type = type;
        return req;
    }

    public WikiRequestT buildReq(String id, String type, Integer timeout){
        WikiRequestT req = new WikiRequestT();
        req.id = id;
        req.type = type;
        req.timeout = timeout;
        return req;
    }

    public WikiRequestT buildPageRequest(String id, String page){
        WikiRequestT req = buildReq(id, "getPage");
        req.pageTitle = page;
        req.timeout = 20;
        return req;
    }

    public WikiRequestT buildSearchRequest(String id, String query, int limit){
        WikiRequestT req = buildReq(id, "search");
        req.query = query;
        req.limit = limit;
        return req;
    }

    public WikiRequestT buildSearchRequest(String id, String query, int limit, int timeout){
        WikiRequestT req = buildReq(id, "search", timeout);
        req.query = query;
        req.limit = limit;
        return req;
    }

    public WikiRequestT buildZeitgeist(String id, int limit){
        WikiRequestT req = buildReq(id, "zeitgeist");
        req.limit = limit;
        return req;
    }

    public WikiRequestT buildTrending(String id, int timeLimit, int maxItems){
        WikiRequestT req = buildReq(id, "trending");
        req.timeLimitInSeconds = timeLimit;
        req.maxItems = maxItems;
        return req;
    }

    public WikiRequestT buildShutdown(String id){
        WikiRequestT req = buildReq(id, "stop");
        return req;
    }

    public WikiRequestT buildWindowedPeakLoad(String id, int timeWindow){
        WikiRequestT req = buildReq(id, "windowedPeakLoad");
        req.timeWindowInSeconds = timeWindow;
        return req;
    }

    public WikiRequestT buildShortestPath(String id, String page1, String page2, int timeout){
        WikiRequestT req = buildReq(id, "shortestPath");
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

    public WikiResponseT makeRequest(TestClient client, WikiRequestT req){
        String message = gson.toJson(req);
        String response = client.sendMessage(message);
        System.out.println(response);
        return gson.fromJson(response, WikiResponseT.class);
    }
}

class WikiRequestT {
    public String id;
    public String type;
    public String query;
    public String pageTitle;
    public int timeLimitInSeconds;
    public int maxItems;
    public Integer timeWindowInSeconds;
    public String pageTitle1;
    public int limit;
    public String pageTitle2;
    public Integer timeout;
}

class WikiResponseT {
    public String id;
    public String status;
    public Object response;

    /**
     *
     * @param id
     * @param response
     */

    public WikiResponseT(String id, Object response){
        this.id = id;
        this.response = response;
    }
}

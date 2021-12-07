package cpen221.mp3;

import com.google.gson.Gson;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import cpen221.mp3.fsftbuffer.TestObject;
import cpen221.mp3.wikimediator.WikiMediator;
import org.junit.Assert;
import org.junit.Test;
import java.io.FileWriter;
import java.io.Writer;
import java.rmi.NoSuchObjectException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import cpen221.mp3.server.WikiMediatorServer;
import cpen221.mp3.server.WikiRequest;
import cpen221.mp3.server.WikiResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;


public class Tests {

    /*
        You can add your tests here.
        Remember to import the packages that you need, such
        as cpen221.mp3.fsftbuffer.
     */

    @Test
    public void testConstruction() {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>();
        Assert.assertEquals(t.getSize(), 0);
    }

    @Test
    public void testConstruction2() {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>(10, 10);
        Assert.assertEquals(t.getSize(), 0);
    }

    @Test
    public void testAdding() {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>();
        TestObject ro0 = new TestObject("0");
        t.put(ro0);

        Assert.assertTrue(t.touch("0"));
    }

    @Test
    public void testAddingObjects() {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>();

        TestObject ro0 = new TestObject("0");
        TestObject ro2 = new TestObject("2");

        t.put(ro0);

        Assert.assertTrue(t.touch("0"));
        Assert.assertTrue(t.update(ro0));
        Assert.assertFalse(t.touch("1"));
        Assert.assertFalse(t.update(ro2));
    }


    @Test
    public void testSet() {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>();

        TestObject t0 = new TestObject("0");
        TestObject t1 = new TestObject("1");
        TestObject t2 = new TestObject("2");

        HashSet<TestObject> testSet = new HashSet<>();

        testSet.add(t0);
        testSet.add(t1);
        testSet.add(t2);

        for (TestObject testObj : testSet) {
            Assert.assertTrue(t.put(testObj));
        }

        try {
            for (TestObject testObj : testSet) {
                Assert.assertEquals(t.get(testObj.id()), testObj);
            }
        } catch (NoSuchObjectException e) {
            // this should not happen
            Assert.fail();
        }
    }

    @Test
    public void testMaxCap() throws InterruptedException {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>(4, 100);

        ArrayList<TestObject> testList = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            testList.add(new TestObject(Integer.toString(i)));
            t.put(testList.get(i));
            Thread.sleep(10);
        }

        try {
            for (int i = 3; i < 7; i++) {
                Assert.assertEquals(t.get(Integer.toString(i)).id(), Integer.toString(i));
            }
        } catch (NoSuchObjectException e) {
            // this should never reach here
            Assert.fail();
        }

        Assert.assertEquals(4, t.getSize());
    }

    @Test
    public void testMaxTimeout() throws InterruptedException {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>(4, 5);

        ArrayList<TestObject> testList = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            testList.add(new TestObject(Integer.toString(i)));
            t.put(testList.get(i));
        }

        Thread.sleep(6000);

        Assert.assertEquals(0, t.getSize());
    }

    @Test
    public void testParams() {
        FSFTBuffer<TestObject>[] t;
        t = new FSFTBuffer[4];
        t[0] = new FSFTBuffer<>(0, 0);
        t[1] = new FSFTBuffer<>(1, -1);
        t[2] = new FSFTBuffer<>(-1, 1);
        t[3] = new FSFTBuffer<>(-10, -10);

        TestObject[] testObj = new TestObject[]{new TestObject("0"),
                new TestObject("1"), new TestObject("2")};

        for (FSFTBuffer<TestObject> t0 : t) {
            for (TestObject T : testObj) {
                t0.put(T);
            }
        }

        for (FSFTBuffer<TestObject> t0 : t) {
            Assert.assertEquals(0, t0.getSize());
        }

        for (FSFTBuffer<TestObject> t0 : t) {
            for (TestObject T : testObj) {
                try {
                    t0.get(T.id());
                    Assert.fail("expected exception was not thrown");
                } catch (NoSuchObjectException e) {
                    // if execution reaches here,
                    // it indicates this exception was thrown
                    // which is what we want
                    Assert.assertTrue(true);
                }
            }
        }
    }


    @Test
    public void testTimeouts() throws InterruptedException {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>(4, 4);

        TestObject[] r = new TestObject[]{new TestObject("a"),
                new TestObject("b"), new TestObject("c"),
                new TestObject("d"), new TestObject("e"),
                new TestObject("f")};

        try {
            // Add a, b, c
            Assert.assertTrue(t.put(r[0]));
            Assert.assertTrue(t.put(r[1]));
            Assert.assertTrue(t.put(r[2]));

            Assert.assertEquals(t.getSize(), 3);
            for (int i = 0; i < 2; i++) {
                Assert.assertEquals(t.get(r[i].id()), r[i]);
            }

            Thread.sleep(3000);

            Assert.assertEquals(t.getSize(), 3);
            for (int i = 0; i < 2; i++) {
                Assert.assertEquals(t.get(r[i].id()), r[i]);
            }

            // Add b, c, d
            Assert.assertTrue(t.put(r[1]));
            Assert.assertTrue(t.put(r[2]));
            Assert.assertTrue(t.put(r[3]));

            Assert.assertEquals(t.getSize(), 4);
            for (int i = 0; i < 4; i++) {
                Assert.assertEquals(t.get(r[i].id()), r[i]);
            }

            Thread.sleep(1200); // a has expired

            Assert.assertFalse(t.touch("a"));
            Assert.assertEquals(t.getSize(), 3);
            for (int i = 1; i < 4; i++) {
                Assert.assertEquals(t.get(r[i].id()), r[i]);
            }

            // touch c, update d
            Assert.assertTrue(t.touch("c"));
            Assert.assertTrue(t.update(r[3]));

            Thread.sleep(1500);

            // Add e, f (b should be removed as oldest)
            Assert.assertTrue(t.put(r[4]));
            Assert.assertTrue(t.put(r[5]));

            Assert.assertEquals(t.getSize(), 4);
            for (int i = 2; i < 6; i++) {
                Assert.assertEquals(t.get(r[i].id()), r[i]);
            }

            Thread.sleep(3000); // c, d have expired

            // Add f
            Assertions.assertTrue(t.put(r[5]));
            Assertions.assertEquals(t.getSize(), 2);
            for (int i = 4; i < 6; i++) {
                Assert.assertEquals(t.get(r[i].id()), r[i]);
            }

            Thread.sleep(1500); // e expired

            Assert.assertEquals(t.getSize(), 1);
            Assert.assertEquals(t.get("f"), r[5]);

            Thread.sleep(3000); // f expired

            Assert.assertEquals(t.getSize(), 0);

            try {
                t.get("I love cpen221 :)");
                // this should throw an exception
                Assert.fail();
            } catch (NoSuchObjectException e) {
                // this should be caught
            }

        } catch (NoSuchObjectException e) {
            // should never reach here
            Assert.fail();
        }
    }

    @Test
    public void testTimeouts2() throws InterruptedException {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>(4, 4);

        TestObject[] r = new TestObject[]{new TestObject("0"),
                new TestObject("1"), new TestObject("2"),
                new TestObject("3"), new TestObject("4"),
                new TestObject("5")};

        // Add 0, 1
        Assert.assertTrue(t.put(r[0]));
        Assert.assertTrue(t.put(r[1]));

        Thread.sleep(2000);

        Assert.assertTrue(t.touch("0"));
        Assert.assertTrue(t.update(r[0]));
        Assert.assertFalse(t.update(r[4]));

        Thread.sleep(2100); // 1 expires

        Assert.assertFalse(t.update(r[1]));
        Assert.assertFalse(t.touch("1"));
        Assert.assertTrue(t.update(r[0]));

        Thread.sleep(10);

        Assert.assertTrue(t.put(r[1])); // Add 1

        Thread.sleep(50);

        Assert.assertTrue(t.put(r[2])); // Add 2

        Thread.sleep(50);

        Assert.assertEquals(t.getSize(), 3);
        Assert.assertTrue(t.put(r[4])); // Add 4
        Assert.assertEquals(t.getSize(), 4);

        Thread.sleep(50);

        Assert.assertTrue(t.put(r[5])); // Add 5, 0 removed as max capacity reached
        Assert.assertEquals(t.getSize(), 4);

        try {
            for (int i = 1; i < 6; i++) {
                if (i !=3) Assert.assertEquals(t.get(r[i].id()), r[i]);
            }

            Thread.sleep(50);

            Assert.assertTrue(t.put(r[3])); // Add 3, 1 removed as max capacity reached
            Assert.assertEquals(t.getSize(), 4);
            for (int i = 2; i < 6; i++) {
                Assert.assertEquals(t.get(r[i].id()), r[i]);
            }

            Thread.sleep(4000);

            Assert.assertEquals(t.getSize(), 0);
        } catch (NoSuchObjectException e) {
            // this should not execute
            Assert.fail();
        }
    }

    @Test
    public void testNullValues() {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>(4, 4);

        TestObject r = new TestObject("0");

        Assert.assertFalse(t.put(null));
        Assert.assertEquals(t.getSize(), 0);

        Assert.assertTrue(t.put(r));
        Assert.assertFalse(t.put(null));
        Assert.assertEquals(t.getSize(), 1);

        try {
            t.get(null);
            Assert.fail();
        } catch (NoSuchObjectException e) {
            // this should execute
        }

        Assert.assertFalse(t.touch(null));
        Assert.assertFalse(t.update(null));
    }

    @Test
    public void testSameId() {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>();

        TestObject a = new TestObject("i love cpen221");
        TestObject b = new TestObject("i love cpen221");
        TestObject c = new TestObject("i love cpen221");

        Assert.assertTrue(t.put(a));
        Assert.assertTrue(t.put(b));
        Assert.assertTrue(t.put(c));

        Assert.assertEquals(1, t.getSize());
    }

    @Test
    public void testZeitgeistLimit() {
        WikiMediator testingWikiMediator = new WikiMediator(5, 3);

        for (int i = 0; i < 3; i++) {
            testingWikiMediator.getPage("Dog");
        }
        for (int i = 0; i < 10; i++) {
            testingWikiMediator.search("grapple", 3);
        }
        for (int i = 0; i < 5; i++) {
            testingWikiMediator.getPage("Car");
        }
        for (int i = 0; i < 2; i++) {
            testingWikiMediator.getPage("Hamster");
            testingWikiMediator.getPage("Cat");
        }

        Assert.assertEquals(Arrays.asList("grapple", "Car", "Dog"), testingWikiMediator.zeitgeist(3));
    }

    @Test
    public void testZeitgeistSimple() {
        WikiMediator testingWikiMediator = new WikiMediator(5, 3);

        for (int i = 0; i < 3; i++) {
            testingWikiMediator.getPage("Dog");
        }
        for (int i = 0; i < 10; i++) {
            testingWikiMediator.search("grapple", 3);
        }
        for (int i = 0; i < 5; i++) {
            testingWikiMediator.getPage("Car");
        }
        for (int i = 0; i < 2; i++) {
            testingWikiMediator.getPage("Hamster");
            testingWikiMediator.getPage("Cat");
        }

        Assert.assertEquals(Arrays.asList("grapple", "Car", "Dog", "Hamster", "Cat"), testingWikiMediator.zeitgeist(6));
    }

    @Test
    public void testZeitgeistSameNumberSearches(){
        WikiMediator testingWikiMediator = new WikiMediator(5, 3);

        testingWikiMediator.getPage("A");
        testingWikiMediator.getPage("B");
        testingWikiMediator.getPage("C");
        testingWikiMediator.getPage("D");
        testingWikiMediator.getPage("E");
        testingWikiMediator.getPage("F");
        testingWikiMediator.getPage("G");
        testingWikiMediator.getPage("H");

        Assert.assertEquals(Arrays.asList("H", "G", "F", "E", "D", "C", "B", "A"), testingWikiMediator.zeitgeist(10));
    }

    @Test
    public void testZeitgeistSingleElement() {
        WikiMediator testingWikiMediator = new WikiMediator(5, 3);

        testingWikiMediator.search("key", 3);

        Assert.assertEquals(Arrays.asList("key"), testingWikiMediator.zeitgeist(3));
    }

    @Test
    public void windowedPeakLoadTest() {
        WikiMediator mediator = new WikiMediator(10, 30);
        mediator.getPage("Dog");
        mediator.getPage("Cat");
        mediator.getPage("Mouse");
        mediator.zeitgeist(2);
        mediator.trending(10, 5);
        try {
            Thread.sleep(1 * 5500);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        mediator.search("Giraffe", 1);
        mediator.search("Zebra", 1);
        mediator.search("Lion", 1);
        //mediator.windowedPeakLoad();

        Assert.assertEquals(5, mediator.windowedPeakLoad(5));

    }

    @Test
    public void windowedPeakLoadTest_endingLoad() {
        WikiMediator mediator = new WikiMediator(10, 30);
        mediator.search("Giraffe", 1);
        mediator.search("Zebra", 1);
        mediator.search("Lion", 1);
        mediator.windowedPeakLoad();

        try {
            Thread.sleep(8 * 1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        mediator.getPage("Dog");
        mediator.getPage("Cat");
        mediator.getPage("Bird");
        mediator.getPage("Rat");
        mediator.getPage("Hamster");
        mediator.getPage("Porcupine");
        mediator.getPage("Ant");
        mediator.getPage("Snake");

        Assert.assertEquals(9, mediator.windowedPeakLoad(6));

    }

    @Test
    public void testingSimpleOperations() {
        WikiMediator testingWikiMediator  =new WikiMediator(10, 90);

        for (int i =0; i < 5; i++) {
            testingWikiMediator.getPage("Dog");
        }
        try{
            Thread.sleep(50);
        }
        catch(Exception e) {
            System.out.println("error sleeping thread");
        }

        testingWikiMediator.search("ubama", 5);


    }

    @Test
    public void testWithinTimeWindow1() {
        WikiMediator testingWikiMediator = new WikiMediator(10, 10);

        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");

        try {
            Thread.sleep(4*1000);
        }
        catch (Exception e) {
            System.out.println("Cannot sleep device");
        }

        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Cat");

        Assert.assertEquals(9, testingWikiMediator.windowedPeakLoad(3));
    }

    @Test
    public void testWithinTimeWindow2() {
        WikiMediator testingWikiMediator = new WikiMediator(10, 10);

        testingWikiMediator.search("ubama", 8);
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.search("excellent", 1);

        Assert.assertEquals(8, testingWikiMediator.windowedPeakLoad(3));
    }

    @Test
    public void testTrending1() {
        WikiMediator testingWikiMediator = new WikiMediator(10, 10);

        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Cat");

        try {
            Thread.sleep(4*1000);
        }
        catch(Exception e) {

        }

        testingWikiMediator.getPage("Hamster");
        testingWikiMediator.getPage("Hamster");
        testingWikiMediator.getPage("Hamster");
        testingWikiMediator.getPage("Hamster");
        testingWikiMediator.getPage("Rat");
        testingWikiMediator.getPage("Rat");

        Assert.assertEquals(Arrays.asList("Hamster", "Rat"), testingWikiMediator.trending(3, 2));
    }

    @Test
    public void testTrending3() {
        WikiMediator testingWikiMediator = new WikiMediator(10, 10);

        testingWikiMediator.getPage("Hamster");
        testingWikiMediator.search("f", 2);
        testingWikiMediator.getPage("Cat");

        try {
            Thread.sleep(4*1000);
        }
        catch (Exception e) {
            System.out.println("Unable to sleep thread");
        }

        testingWikiMediator.search("f", 10);
        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");

        Assert.assertEquals(Arrays.asList("Dog", "Cat", "f"), testingWikiMediator.trending(3, 3));
    }


    @Test
    public void testReadingFromJson() {

        List<Long> testList = new ArrayList<Long>();
        testList.add(System.currentTimeMillis());

        Map<String, List<Long>> testMap = new HashMap<String, List<Long>>();
        testMap.put("Dog", testList);

        try {
            Gson gson = new Gson();

            Writer writerMap = new FileWriter("local/dataMap.json");
            new Gson().toJson(testMap, writerMap);
            writerMap.close();
        }
        catch (Exception e) {
            System.out.println("Test has Failed - unable to write to map json");
        }

        try  {
            Gson gson = new Gson();
            Writer writerList = new FileWriter("local/dataList.json");
            new Gson().toJson(testList, writerList);
            writerList.close();
        }
        catch (Exception e) {

        }

        WikiMediator testingMediator = new WikiMediator(10, 90);

        System.out.println(testingMediator.trending(100, 50).get(0));
    }

    @Test
    public void testSinglePagePath() {
        List<String> path;
        WikiMediator wikiM = new WikiMediator(100, 60 * 60);

        try {
            path = wikiM.shortestPath("Water", "1,3,5-Trithiane", 60 * 5);
            Assert.assertEquals(2, path.size());
        } catch (TimeoutException e) {
            // hopefully this doesn't time out
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testAnotherSinglePagePath() {
        List<String> path;
        WikiMediator wikiM = new WikiMediator(100, 60 * 60);

        try {
            path = wikiM.shortestPath("AA Tauri", "10 Tauri", 60 * 5);
            Assert.assertEquals(2, path.size());
        } catch (TimeoutException e) {
            // hopefully this doesn't time out
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void multiplePageTest() {
        List<String> path;
        WikiMediator wikiM = new WikiMediator(100, 60 * 60);

        try {
            path = wikiM.shortestPath("The Autumn Republic", "The Wheel of Time", 60 * 5);
            Assert.assertEquals(3, path.size());
        } catch (TimeoutException e) {
            // hopefully this doesn't time out
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testLexicographical() {
        List<String> path = null;
        ArrayList<String> expected = new ArrayList<>(Arrays.asList(
                "Sequoia High School (Redwood City, California)",
                "Advancement Via Individual Determination",
                "Dallas"));
        WikiMediator wikiM = new WikiMediator(100, 60 * 60);


        try {
            path = wikiM.shortestPath("Sequoia High School (Redwood City, California)",
                    "Dallas", 60 * 5);
        } catch (TimeoutException e) {
            // hopefully this doesn't time out
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(3, path.size());
        Assert.assertEquals(expected, path);
    }

    @Test
    public void testLexicographical2() {
        List<String> path = null;
        ArrayList<String> expected = new ArrayList<>(Arrays.asList(
                "Harbord Collegiate Institute",
                "Adult high school",
                "Continuing education"));
        WikiMediator wikiM = new WikiMediator(100, 60 * 60);

        try {
            path = wikiM.shortestPath("Harbord Collegiate Institute",
                    "Continuing education", 60 * 20);
        } catch (TimeoutException e) {
            // hopefully this doesn't time out
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(3, path.size());
        Assert.assertEquals(expected, path);
    }

    @Test
    public void testPhilObama() {
        List<String> path = null;
        WikiMediator wikiM = new WikiMediator(100, 60 * 60);

        try {
            path = wikiM.shortestPath("Philosophy",
                    "Barack Obama", 60 * 60);
        } catch (TimeoutException e) {
            // hopefully this doesn't time out
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(3, path.size());
        System.out.println(path);
    }

    @Test
    public void testSamePage() {
        List<String> path = null;
        WikiMediator wikiM = new WikiMediator(100, 60 * 60);

        try {
            path = wikiM.shortestPath("Philosophy",
                    "Philosophy", 60 * 60);
        } catch (TimeoutException e) {
            // hopefully this doesn't time out
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(1, path.size());
        System.out.println(path);
    }

    @Test
    public void testUnitedTravis() {
        List<String> path = null;
        WikiMediator wikiM = new WikiMediator(100, 60 * 60);

        try {
            path = wikiM.shortestPath("United States",
                    "Travis Scott", 60 * 60);
        } catch (TimeoutException e) {
            // hopefully this doesn't time out
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(3, path.size());
        System.out.println(path);
    }

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
        client = buildClient("127.0.0.1", 6666);
    }

    @org.junit.jupiter.api.Test
    public void testShutdown(){
        WikiRequest req = new WikiRequest();
        req.id = "1";
        req.type = "stop";
        String message = gson.toJson(req);
        String response = client.sendMessage(message);
        System.out.println(response);
        Assertions.assertEquals("bye", gson.fromJson(response, WikiResponse.class).response);
    }

    @org.junit.jupiter.api.Test
    public void testGetPage(){
        WikiRequest req = buildReq("1", "getPage");
        req.pageTitle = "Barack Obama";
        String message = gson.toJson(req);
        String response = client.sendMessage(message);
        Assertions.assertEquals("success", gson.fromJson(response, WikiResponse.class).status);
    }

    @org.junit.jupiter.api.Test
    public void testMultiReq(){
        WikiResponse mes = makeRequest(client, buildPageRequest("1", "Abraham Lincoln"));
        WikiResponse mes2 = makeRequest(client, buildPageRequest("2", "Mark Van Raamsdonk"));
        Assertions.assertEquals("success", mes.status);
        Assertions.assertEquals("success", mes2.status);
    }

    @org.junit.jupiter.api.Test
    public void searchReq(){
        WikiResponse mes = makeRequest(client, buildSearchRequest("1", "Barack Obama", 5));
        Assertions.assertEquals("success", mes.status);
    }

    @org.junit.jupiter.api.Test
    public void zeitReq(){
        WikiResponse mes = makeRequest(client, buildZeitgeist("2", 10));
        Assertions.assertEquals("success", mes.status);
    }

    @org.junit.jupiter.api.Test
    public void shortestReq(){
        WikiResponse mes= makeRequest(client, buildShortestPath("1", "Mark Van Raamsdonk", "George Smoot", 100));
        Assertions.assertEquals("success", mes.status);
    }

    @org.junit.jupiter.api.Test
    public void trendingReq(){
        WikiResponse mes= makeRequest(client, buildTrending("1", 10, 10));
        Assertions.assertEquals("success", mes.status);
    }

    @org.junit.jupiter.api.Test
    public void peakLoadReq(){
        WikiResponse mes= makeRequest(client, buildWindowedPeakLoad("1", 10));
        Assertions.assertEquals("success", mes.status);
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
        WikiRequest req = buildReq(id, "zeitgeist");
        req.timeLimitInSeconds = timeLimit;
        req.maxItems = maxItems;
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

package cpen221.mp3;

import com.google.gson.Gson;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import cpen221.mp3.fsftbuffer.TestObject;
import cpen221.mp3.wikimediator.WikiMediator;
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
        Assertions.assertEquals(t.getSize(), 0);
    }

    @Test
    public void testConstruction2() {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>(10, 10);
        Assertions.assertEquals(t.getSize(), 0);
    }

    @Test
    public void testAdding() {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>();
        TestObject ro0 = new TestObject("0");
        t.put(ro0);

        Assertions.assertTrue(t.touch("0"));
    }

    @Test
    public void testAddingObjects() {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>();

        TestObject ro0 = new TestObject("0");
        TestObject ro2 = new TestObject("2");

        t.put(ro0);

        Assertions.assertTrue(t.touch("0"));
        Assertions.assertTrue(t.update(ro0));
        Assertions.assertFalse(t.touch("1"));
        Assertions.assertFalse(t.update(ro2));
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
            Assertions.assertTrue(t.put(testObj));
        }

        try {
            for (TestObject testObj : testSet) {
                Assertions.assertEquals(t.get(testObj.id()), testObj);
            }
        } catch (NoSuchObjectException e) {
            // this should not happen
            Assertions.fail();
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
                Assertions.assertEquals(t.get(Integer.toString(i)).id(), Integer.toString(i));
            }
        } catch (NoSuchObjectException e) {
            // this should never reach here
            Assertions.fail();
        }

        Assertions.assertEquals(4, t.getSize());
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

        Assertions.assertEquals(0, t.getSize());
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
            Assertions.assertEquals(0, t0.getSize());
        }

        for (FSFTBuffer<TestObject> t0 : t) {
            for (TestObject T : testObj) {
                try {
                    t0.get(T.id());
                    Assertions.fail("expected exception was not thrown");
                } catch (NoSuchObjectException e) {
                    // if execution reaches here,
                    // it indicates this exception was thrown
                    // which is what we want
                    Assertions.assertTrue(true);
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
            Assertions.assertTrue(t.put(r[0]));
            Assertions.assertTrue(t.put(r[1]));
            Assertions.assertTrue(t.put(r[2]));

            Assertions.assertEquals(t.getSize(), 3);
            for (int i = 0; i < 2; i++) {
                Assertions.assertEquals(t.get(r[i].id()), r[i]);
            }

            Thread.sleep(3000);

            Assertions.assertEquals(t.getSize(), 3);
            for (int i = 0; i < 2; i++) {
                Assertions.assertEquals(t.get(r[i].id()), r[i]);
            }

            // Add b, c, d
            Assertions.assertTrue(t.put(r[1]));
            Assertions.assertTrue(t.put(r[2]));
            Assertions.assertTrue(t.put(r[3]));

            Assertions.assertEquals(t.getSize(), 4);
            for (int i = 0; i < 4; i++) {
                Assertions.assertEquals(t.get(r[i].id()), r[i]);
            }

            Thread.sleep(1200); // a has expired

            Assertions.assertFalse(t.touch("a"));
            Assertions.assertEquals(t.getSize(), 3);
            for (int i = 1; i < 4; i++) {
                Assertions.assertEquals(t.get(r[i].id()), r[i]);
            }

            // touch c, update d
            Assertions.assertTrue(t.touch("c"));
            Assertions.assertTrue(t.update(r[3]));

            Thread.sleep(1500);

            // Add e, f (b should be removed as oldest)
            Assertions.assertTrue(t.put(r[4]));
            Assertions.assertTrue(t.put(r[5]));

            Assertions.assertEquals(t.getSize(), 4);
            for (int i = 2; i < 6; i++) {
                Assertions.assertEquals(t.get(r[i].id()), r[i]);
            }

            Thread.sleep(3000); // c, d have expired

            // Add f
            Assertions.assertTrue(t.put(r[5]));
            Assertions.assertEquals(t.getSize(), 2);
            for (int i = 4; i < 6; i++) {
                Assertions.assertEquals(t.get(r[i].id()), r[i]);
            }

            Thread.sleep(1500); // e expired

            Assertions.assertEquals(t.getSize(), 1);
            Assertions.assertEquals(t.get("f"), r[5]);

            Thread.sleep(3000); // f expired

            Assertions.assertEquals(t.getSize(), 0);

            try {
                t.get("I love cpen221 :)");
                // this should throw an exception
                Assertions.fail();
            } catch (NoSuchObjectException e) {
                // this should be caught
            }

        } catch (NoSuchObjectException e) {
            // should never reach here
            Assertions.fail();
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
        Assertions.assertTrue(t.put(r[0]));
        Assertions.assertTrue(t.put(r[1]));

        Thread.sleep(2000);

        Assertions.assertTrue(t.touch("0"));
        Assertions.assertTrue(t.update(r[0]));
        Assertions.assertFalse(t.update(r[4]));

        Thread.sleep(2100); // 1 expires

        Assertions.assertFalse(t.update(r[1]));
        Assertions.assertFalse(t.touch("1"));
        Assertions.assertTrue(t.update(r[0]));

        Thread.sleep(10);

        Assertions.assertTrue(t.put(r[1])); // Add 1

        Thread.sleep(50);

        Assertions.assertTrue(t.put(r[2])); // Add 2

        Thread.sleep(50);

        Assertions.assertEquals(t.getSize(), 3);
        Assertions.assertTrue(t.put(r[4])); // Add 4
        Assertions.assertEquals(t.getSize(), 4);

        Thread.sleep(50);

        Assertions.assertTrue(t.put(r[5])); // Add 5, 0 removed as max capacity reached
        Assertions.assertEquals(t.getSize(), 4);

        try {
            for (int i = 1; i < 6; i++) {
                if (i !=3) Assertions.assertEquals(t.get(r[i].id()), r[i]);
            }

            Thread.sleep(50);

            Assertions.assertTrue(t.put(r[3])); // Add 3, 1 removed as max capacity reached
            Assertions.assertEquals(t.getSize(), 4);
            for (int i = 2; i < 6; i++) {
                Assertions.assertEquals(t.get(r[i].id()), r[i]);
            }

            Thread.sleep(4000);

            Assertions.assertEquals(t.getSize(), 0);
        } catch (NoSuchObjectException e) {
            // this should not execute
            Assertions.fail();
        }
    }

    @Test
    public void testNullValues() {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>(4, 4);

        TestObject r = new TestObject("0");

        Assertions.assertFalse(t.put(null));
        Assertions.assertEquals(t.getSize(), 0);

        Assertions.assertTrue(t.put(r));
        Assertions.assertFalse(t.put(null));
        Assertions.assertEquals(t.getSize(), 1);

        try {
            t.get(null);
            Assertions.fail();
        } catch (NoSuchObjectException e) {
            // this should execute
        }

        Assertions.assertFalse(t.touch(null));
        Assertions.assertFalse(t.update(null));
    }

    @Test
    public void testSameId() {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>();

        TestObject a = new TestObject("i love cpen221");
        TestObject b = new TestObject("i love cpen221");
        TestObject c = new TestObject("i love cpen221");

        Assertions.assertTrue(t.put(a));
        Assertions.assertTrue(t.put(b));
        Assertions.assertTrue(t.put(c));

        Assertions.assertEquals(1, t.getSize());
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

        Assertions.assertEquals(Arrays.asList("grapple", "Car", "Dog"), testingWikiMediator.zeitgeist(3));
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

        Assertions.assertEquals(Arrays.asList("grapple", "Car", "Dog", "Hamster", "Cat"), testingWikiMediator.zeitgeist(6));
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

        Assertions.assertEquals(Arrays.asList("H", "G", "F", "E", "D", "C", "B", "A"), testingWikiMediator.zeitgeist(10));
    }

    @Test
    public void testZeitgeistSingleElement() {
        WikiMediator testingWikiMediator = new WikiMediator(5, 3);

        testingWikiMediator.search("key", 3);

        Assertions.assertEquals(Collections.singletonList("key"), testingWikiMediator.zeitgeist(3));
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
            Thread.sleep(5500);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        mediator.search("Giraffe", 1);
        mediator.search("Zebra", 1);
        mediator.search("Lion", 1);
        //mediator.windowedPeakLoad();

        Assertions.assertEquals(5, mediator.windowedPeakLoad(5));

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

        Assertions.assertEquals(9, mediator.windowedPeakLoad(6));

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

        Assertions.assertEquals(9, testingWikiMediator.windowedPeakLoad(3));
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

        Assertions.assertEquals(8, testingWikiMediator.windowedPeakLoad(3));
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
        catch(Exception ignored) {
        }

        testingWikiMediator.getPage("Hamster");
        testingWikiMediator.getPage("Hamster");
        testingWikiMediator.getPage("Hamster");
        testingWikiMediator.getPage("Hamster");
        testingWikiMediator.getPage("Rat");
        testingWikiMediator.getPage("Rat");

        Assertions.assertEquals(Arrays.asList("Hamster", "Rat"), testingWikiMediator.trending(3, 2));
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

        Assertions.assertEquals(Arrays.asList("Dog", "Cat", "f"), testingWikiMediator.trending(3, 3));
    }

    @Test
    public void testDefaultTimeWindow() {
        WikiMediator testingWikiMediator = new WikiMediator(10, 10);

        testingWikiMediator.search("f", 10);
        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.search("f", 10);
        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Cat");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");

        try {
            Thread.sleep(31*1000);
        }
        catch(Exception ignored) {

        }

        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");

        Assertions.assertEquals(12, testingWikiMediator.windowedPeakLoad());
    }

    @Test
    public void testWritingToFiles() {
        WikiMediator testingWikiMediator = new WikiMediator(10, 10);

        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");
        testingWikiMediator.getPage("Dog");

        testingWikiMediator.writeToFile();

        WikiMediator testingWikiMediator2 = new WikiMediator(10, 10);

        testingWikiMediator2.getPage("Cat");
        testingWikiMediator2.getPage("Cat");
        testingWikiMediator2.getPage("Cat");
        testingWikiMediator2.getPage("Cat");
        testingWikiMediator2.getPage("Dog");
        testingWikiMediator2.getPage("Dog");

        Assertions.assertEquals(Arrays.asList("Dog", "Cat"), testingWikiMediator2.zeitgeist(2));
    }

    @Test
    public void testReadingFromJson() {

        List<Long> testList = new ArrayList<>();
        testList.add(System.currentTimeMillis());

        Map<String, List<Long>> testMap = new HashMap<>();
        testMap.put("Dog", testList);

        try {
            Writer writerMap = new FileWriter("local/dataMap.json");
            new Gson().toJson(testMap, writerMap);
            writerMap.close();
        }
        catch (Exception e) {
            System.out.println("Test has Failed - unable to write to map json");
        }

        try  {
            Writer writerList = new FileWriter("local/dataList.json");
            new Gson().toJson(testList, writerList);
            writerList.close();
        }
        catch (Exception ignored) {
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
            Assertions.assertEquals(2, path.size());
        } catch (TimeoutException e) {
            // hopefully this doesn't time out
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void testAnotherSinglePagePath() {
        List<String> path;
        WikiMediator wikiM = new WikiMediator(100, 60 * 60);

        try {
            path = wikiM.shortestPath("AA Tauri", "10 Tauri", 60 * 5);
            Assertions.assertEquals(2, path.size());
        } catch (TimeoutException e) {
            // hopefully this doesn't time out
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void multiplePageTest() {
        List<String> path;
        WikiMediator wikiM = new WikiMediator(100, 60 * 60);

        try {
            path = wikiM.shortestPath("The Autumn Republic", "The Wheel of Time", 60 * 5);
            Assertions.assertEquals(3, path.size());
        } catch (TimeoutException e) {
            // hopefully this doesn't time out
            Assertions.fail(e.getMessage());
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
            Assertions.fail(e.getMessage());
        }

        Assertions.assertEquals(3, path.size());
        Assertions.assertEquals(expected, path);
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
            Assertions.fail(e.getMessage());
        }

        Assertions.assertEquals(3, path.size());
        Assertions.assertEquals(expected, path);
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
            Assertions.fail(e.getMessage());
        }

        Assertions.assertEquals(3, path.size());
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
            Assertions.fail(e.getMessage());
        }

        Assertions.assertEquals(1, path.size());
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
            Assertions.fail(e.getMessage());
        }

        Assertions.assertEquals(3, path.size());
        System.out.println(path);
    }

    private static Gson gson;
    private static TestClient client;

    @BeforeAll
    public static void setUpServer(){

        WikiMediator mediator = new WikiMediator(10, 10);
        WikiMediatorServer server = new WikiMediatorServer(6666, 32, mediator);
        Thread serverThread = new Thread(server::serve);
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

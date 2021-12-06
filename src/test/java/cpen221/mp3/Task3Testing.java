package cpen221.mp3;

import com.google.gson.Gson;
import cpen221.mp3.wikimediator.WikiMediator;
import org.fastily.jwiki.core.Wiki;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileWriter;
import java.io.Writer;
import java.util.*;

public class Task3Testing {

    @Test
    public void testZeitgeist1() {
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
}

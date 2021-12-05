package cpen221.mp3;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import cpen221.mp3.fsftbuffer.TestObject;
import cpen221.mp3.wikimediator.WikiMediator;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Task5Testing {

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

}

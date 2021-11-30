package cpen221.mp3;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import cpen221.mp3.fsftbuffer.RandomObject;
import org.junit.Assert;
import org.junit.Test;

import java.rmi.NoSuchObjectException;
import java.util.Arrays;
import java.util.HashSet;

// Main Class that creates and starts the threads
public class Task2Testing {

    private static final int THREADS = 4; // Number of threads

    public static void main(String[] args)
    {
        for (int i = 0; i < THREADS; i++) {
            Thread object = new Thread(new TestThread());
            object.start();
        }
    }


}


class TestThread implements Runnable {
    public void run() {
        try {
            testThreads();
        } catch (Exception e) {
            System.out.println("Exception " + e + " was thrown.");
        }
    }

    public void testThreads() throws InterruptedException, NoSuchObjectException {
        FSFTBuffer<RandomObject> t = new FSFTBuffer<>(3, 5);

        RandomObject r0 = new RandomObject("0");
        RandomObject r1 = new RandomObject("1");
        RandomObject r2 = new RandomObject("2");
        RandomObject r3 = new RandomObject("3");
        RandomObject r4 = new RandomObject("4");
        RandomObject r5 = new RandomObject("5");

        Assert.assertTrue(t.put(r0));
        Assert.assertTrue(t.put(r1));
        Assert.assertTrue(t.put(r2));

        Thread.sleep(10);

        Assert.assertEquals(t.get("1"), r1);

        Thread.sleep(10);

        Assert.assertEquals(t.get("2"), r2);
        Assert.assertTrue(t.put(r4));
        Assert.assertTrue(t.put(r3));

        Thread.sleep(10);

        Assert.assertTrue(t.update(r2));

        Thread.sleep(10);

        Assert.assertEquals(t.get("4"), r4);
        Assert.assertTrue(t.put(r5));
        Assert.assertTrue(t.put(r0));

        Assert.assertEquals(t.getCurrentSet(), new HashSet<>(Arrays.asList(r0, r4, r5)));

    }
}
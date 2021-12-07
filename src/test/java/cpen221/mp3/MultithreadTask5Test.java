package cpen221.mp3;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import cpen221.mp3.fsftbuffer.TestObject;
import org.junit.Assert;
import org.junit.Test;

import java.rmi.NoSuchObjectException;

public class MultithreadTask5Test {

    private static final int THREADS = 8; // Number of threads

    public static void main(String[] args)
    {
        for (int i = 0; i < THREADS; i++) {
            Thread object = new Thread(new Task5Thread());
            object.start();
        }
    }
}

class Task5Thread implements Runnable {
    public void run() {
        try {
            testThreads();
        } catch (Exception e) {
            System.out.println("Exception " + e + " was thrown.");
        }
    }

    @Test
    public void testThreads() throws InterruptedException, NoSuchObjectException {
        FSFTBuffer<TestObject> t = new FSFTBuffer<>(3, 5);

        TestObject r0 = new TestObject("0");
        TestObject r1 = new TestObject("1");
        TestObject r2 = new TestObject("2");
        TestObject r3 = new TestObject("3");
        TestObject r4 = new TestObject("4");
        TestObject r5 = new TestObject("5");

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

        Assert.assertEquals(t.getSize(), 3);
        Assert.assertEquals(t.get(r0.id()), r0);
        Assert.assertEquals(t.get(r4.id()), r4);
        Assert.assertEquals(t.get(r5.id()).id(), "5");
    }
}

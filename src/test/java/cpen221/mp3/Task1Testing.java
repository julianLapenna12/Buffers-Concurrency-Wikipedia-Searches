package cpen221.mp3;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import cpen221.mp3.fsftbuffer.RandomObject;
import org.junit.Assert;
import org.junit.Test;

import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class Task1Testing {

    @Test
    public void testConstruction() {
        FSFTBuffer<RandomObject> t = new FSFTBuffer<>();
    }

    @Test
    public void testConstruction2() {
        FSFTBuffer<RandomObject> t = new FSFTBuffer<>(10, 10);
    }

    @Test
    public void testAdding() {
        FSFTBuffer<RandomObject> t = new FSFTBuffer<>();
        RandomObject ro0 = new RandomObject("0");
        t.put(ro0);

        Assert.assertTrue(t.touch("0"));
    }

    @Test
    public void testAddingObjects() {
        FSFTBuffer<RandomObject> t = new FSFTBuffer<>();

        RandomObject ro0 = new RandomObject("0");
        RandomObject ro2 = new RandomObject("2");

        t.put(ro0);

        Assert.assertTrue(t.touch("0"));
        Assert.assertTrue(t.update(ro0));
        Assert.assertFalse(t.touch("1"));
        Assert.assertFalse(t.update(ro2));
    }

    @Test
    public void testSet() {
        FSFTBuffer<RandomObject> t = new FSFTBuffer<>();

        RandomObject ro0 = new RandomObject("0");
        RandomObject ro1 = new RandomObject("1");
        RandomObject ro2 = new RandomObject("2");

        HashSet<RandomObject> testSet = new HashSet<>();

        testSet.add(ro0);
        testSet.add(ro1);
        testSet.add(ro2);

        for (RandomObject ro : testSet) {
            t.put(ro);
        }
        Assert.assertEquals(t.getCurrentSet(), testSet);
    }

    @Test
    public void testMaxCap() {
        FSFTBuffer<RandomObject> t = new FSFTBuffer<>(4, 100);

        ArrayList<RandomObject> testList = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            testList.add(new RandomObject(Integer.toString(i)));
            t.put(testList.get(i));
        }

        Assert.assertEquals(t.getCurrentSet(), t.getCurrentSet());
        Assert.assertNotEquals(t.getCurrentSet(), new HashSet<>(testList));
        Assert.assertEquals(4, t.getSize());
    }

    @Test
    public void testMaxTimeout() throws InterruptedException {
        FSFTBuffer<RandomObject> t = new FSFTBuffer<>(4, 5);

        ArrayList<RandomObject> testList = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            testList.add(new RandomObject(Integer.toString(i)));
            t.put(testList.get(i));
        }

        Thread.sleep(6000);

        Assert.assertEquals(0, t.getSize());
    }

    @Test
    public void testParams() {
        FSFTBuffer<RandomObject>[] t = new FSFTBuffer[4];
        t[0] = new FSFTBuffer<>(0, 0);
        t[1] = new FSFTBuffer<>(1, -1);
        t[2] = new FSFTBuffer<>(-1, 1);
        t[3] = new FSFTBuffer<>(-10, -10);

        RandomObject[] r = new RandomObject[]{new RandomObject("0"),
                new RandomObject("1"), new RandomObject("2")};

        for (FSFTBuffer<RandomObject> t0 : t) {
            for (RandomObject r0 : r) {
                t0.put(r0);
            }
        }

        for (FSFTBuffer<RandomObject> t0 : t) {
            Assert.assertEquals(0, t0.getSize());
        }

        for (FSFTBuffer<RandomObject> t0 : t) {
            for (RandomObject r0 : r) {
                try {
                    t0.get(r0.id());
                    Assert.fail("expected exception was not thrown");
                } catch (NoSuchObjectException e) {
                    //if execution reaches here,
                    //it indicates this exception was thrown
                    //so we need not handle it.
                }
            }
        }
    }

    @Test
    public void testTimeouts() throws InterruptedException {
        FSFTBuffer<RandomObject> t = new FSFTBuffer<>(4, 4);

        RandomObject[] r = new RandomObject[]{new RandomObject("a"),
                new RandomObject("b"), new RandomObject("c"),
                new RandomObject("d"), new RandomObject("e"),
                new RandomObject("f")};

        // Add a, b, c
        t.put(r[0]);
        t.put(r[1]);
        t.put(r[2]);

        Assert.assertEquals(t.getSize(), 3);
        Assert.assertEquals(t.getCurrentSet(), new HashSet<>(Arrays.asList(r[0], r[1], r[2])));

        Thread.sleep(3000);

        Assert.assertEquals(t.getSize(), 3);
        Assert.assertEquals(t.getCurrentSet(), new HashSet<>(Arrays.asList(r[0], r[1], r[2])));

        // Add b, c, d
        t.put(r[1]);
        t.put(r[2]);
        t.put(r[3]);

        Assert.assertEquals(t.getSize(), 4);
        Assert.assertEquals(t.getCurrentSet(), new HashSet<>(Arrays.asList(r[0], r[1], r[2], r[3])));

        Thread.sleep(1200); // a has expired

        Assert.assertFalse(t.touch("a"));
        Assert.assertEquals(t.getSize(), 3);
        Assert.assertEquals(t.getCurrentSet(), new HashSet<>(Arrays.asList(r[1], r[2], r[3])));

        // touch c, update d
        t.touch("c");
        t.update(r[3]);

        Thread.sleep(1500);

        // Add e, f (b should be removed as oldest)
        t.put(r[4]);
        t.put(r[5]);

        Assert.assertEquals(t.getSize(), 4);
        Assert.assertEquals(t.getCurrentSet(), new HashSet<>(Arrays.asList(r[2], r[3], r[4], r[5])));

        Thread.sleep(3000); // c, d have expired

        // Add f
        t.put(r[5]);
        Assert.assertEquals(t.getSize(), 2);
        Assert.assertEquals(t.getCurrentSet(), new HashSet<>(Arrays.asList(r[4], r[5])));

        Thread.sleep(1500); // e expired

        Assert.assertEquals(t.getSize(), 1);
        Assert.assertEquals(t.getCurrentSet(), new HashSet<>(Collections.singletonList(r[5])));

        Thread.sleep(3000); // f expired

        Assert.assertEquals(t.getSize(), 0);
    }

    @Test
    public void testTimeouts2() throws InterruptedException {
        FSFTBuffer<RandomObject> t = new FSFTBuffer<>(4, 4);

        RandomObject[] r = new RandomObject[]{new RandomObject("0"),
                new RandomObject("1"), new RandomObject("2"),
                new RandomObject("3"), new RandomObject("4"),
                new RandomObject("5")};

        // Add 0, 1
        t.put(r[0]);
        t.put(r[1]);

        Thread.sleep(2000);

        t.touch("0");
        t.update(r[0]);

        Thread.sleep(2100); // 1 expires

        Assert.assertFalse(t.update(r[1]));
        Assert.assertFalse(t.touch("1"));
        Assert.assertTrue(t.update(r[0]));

        Thread.sleep(10);

        t.put(r[1]); // Add 1

        Thread.sleep(50);

        t.put(r[2]); // Add 2

        Thread.sleep(50);

        Assert.assertEquals(t.getSize(), 3);
        t.put(r[4]); // Add 4
        Assert.assertEquals(t.getSize(), 4);

        Thread.sleep(50);

        t.put(r[5]); // Add 5, 0 removed as max capacity reached
        Assert.assertEquals(t.getSize(), 4);
        Assert.assertEquals(t.getCurrentSet(), new HashSet<>(Arrays.asList(r[1], r[2], r[4], r[5])));

        Thread.sleep(50);

        t.put(r[3]); // Add 3, 1 removed as max capacity reached
        Assert.assertEquals(t.getSize(), 4);
        Assert.assertEquals(t.getCurrentSet(), new HashSet<>(Arrays.asList(r[3], r[2], r[4], r[5])));

        Thread.sleep(4000);

        Assert.assertEquals(t.getSize(), 0);
        Assert.assertEquals(t.getCurrentSet(), new HashSet<RandomObject>());
    }

    /*
     * TODO:
     **  test negative capacity and timeout vals
     **  test capacity and timeout
     **  test removing objects (when object doesn't exist)
     **  test replacing objects (when the oldest one is expired)
     **  test updating/touching an expired object
     **  test adding, updating, then prune and updated objects are still there, others have expired
     **  test get method (catching and confirming objects)
     **  test duplicate ids and duplicate puts
     */

}

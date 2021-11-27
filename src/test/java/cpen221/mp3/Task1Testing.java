package cpen221.mp3;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import cpen221.mp3.fsftbuffer.RandomObject;
import org.hamcrest.Factory;
import org.junit.Assert;
import org.junit.Test;

import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.HashSet;

public class Task1Testing {

    @Test
    public void testConstruction() {
        FSFTBuffer t = new FSFTBuffer();
    }

    @Test
    public void testConstruction2() {
        FSFTBuffer t = new FSFTBuffer(10, 10);
    }

    @Test
    public void testAdding() {
        FSFTBuffer t = new FSFTBuffer();
        RandomObject ro0 = new RandomObject("0");
        t.put(ro0);

        Assert.assertEquals(true, t.touch("0"));
    }

    @Test
    public void testAddingObjects() {
        FSFTBuffer t = new FSFTBuffer();

        RandomObject ro0 = new RandomObject("0");
        RandomObject ro1 = new RandomObject("1");
        RandomObject ro2 = new RandomObject("2");

        t.put(ro0);

        Assert.assertEquals(true, t.touch("0"));
        Assert.assertEquals(true, t.update(ro0));
        Assert.assertEquals(false, t.touch("1"));
        Assert.assertEquals(false, t.update(ro2));
    }

    @Test
    public void testSet() {
        FSFTBuffer t = new FSFTBuffer();

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
        FSFTBuffer t = new FSFTBuffer(4, 100);

        ArrayList<RandomObject> testList = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            testList.add(new RandomObject(Integer.toString(i)));
            t.put(testList.get(i));
        }

        Assert.assertEquals(true, t.getCurrentSet().equals(t.getCurrentSet()));
        Assert.assertEquals(false, t.getCurrentSet().equals(testList));
        Assert.assertEquals(4, t.getSize());
    }

    @Test
    public void testMaxTimeout() throws InterruptedException {
        FSFTBuffer t = new FSFTBuffer(4, 10);

        ArrayList<RandomObject> testList = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            testList.add(new RandomObject(Integer.toString(i)));
            t.put(testList.get(i));
        }

        Thread.sleep(20);

        Assert.assertEquals(0, t.getSize());
    }

    @Test
    public void testParams() throws NoSuchObjectException {
        FSFTBuffer[] t = new FSFTBuffer[]{new FSFTBuffer(0, 0),
                new FSFTBuffer(1, -1),
                new FSFTBuffer(-1, 1),
                new FSFTBuffer(-10, -10)};

        RandomObject[] r = new RandomObject[]{new RandomObject("0"),
                new RandomObject("1"), new RandomObject("2")};

        for (FSFTBuffer t0 : t) {
            for (RandomObject r0 : r) {
                t0.put(r0);
            }
        }

        for (FSFTBuffer t0 : t) {
            Assert.assertTrue(t0.getSize() == 0);
        }

        for (FSFTBuffer t0 : t) {
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

    /*
     * TODO:
     *  test negative capacity and timeout vals
     **  test capacity and timeout
     *  test removing objects (when object doesn't exist)
     *  test replacing objects (when the oldest one is expired)
     *  test updating/touching an expired object
     *  test adding, updating, then prune and updated objects are still there, others have expired
     *  test get method (catching and confirming objects)
     */

}

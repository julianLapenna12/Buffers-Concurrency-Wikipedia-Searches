package cpen221.mp3;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import cpen221.mp3.fsftbuffer.TestObject;
import org.junit.Assert;
import org.junit.Test;

import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.HashSet;

public class Task1Testing {

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
            Assert.assertTrue(t.put(r[5]));
            Assert.assertEquals(t.getSize(), 2);
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
}

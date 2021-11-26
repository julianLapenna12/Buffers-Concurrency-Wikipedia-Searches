package cpen221.mp3;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import cpen221.mp3.fsftbuffer.RandomObject;
import org.junit.Assert;
import org.junit.Test;

public class Tests {

    /*
        You can add your tests here.
        Remember to import the packages that you need, such
        as cpen221.mp3.fsftbuffer.
     */

    @Test
    public void testConstruction() {
        FSFTBuffer t = new FSFTBuffer();
        RandomObject ro0 = new RandomObject("0");
        t.put(ro0);


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
}

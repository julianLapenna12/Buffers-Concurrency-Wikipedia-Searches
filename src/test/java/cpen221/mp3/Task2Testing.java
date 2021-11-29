package cpen221.mp3;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import cpen221.mp3.fsftbuffer.RandomObject;
import org.hamcrest.Factory;
import org.junit.Assert;
import org.junit.Test;

import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

// Main Class that creates and starts the threads
public class Task2Testing {

    private static final int THREADS = 4; // Number of threads


    public static void main(String[] args)
    {
        for (int i = 0; i < THREADS; i++) {
            Thread object
                    = new Thread(new TestThread());
            object.start();
        }
    }
}

class TestThread implements Runnable {
    public void run() {

    }
}
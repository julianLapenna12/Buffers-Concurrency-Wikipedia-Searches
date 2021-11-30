package cpen221.mp3.fsftbuffer;

import java.rmi.NoSuchObjectException;
import java.util.*;
import java.util.stream.Collectors;

public class FSFTBuffer<T extends Bufferable> {

    private final Map<T, Long> masterMap;
    private final int timeout;
    private final int cap;

    /* the default buffer size is 32 objects */
    public static final int DSIZE = 32;

    /* the default timeout value is 3600s */
    public static final int DTIMEOUT = 3600;

    /* TODO: Implement this datatype */

    /**
     * Create a buffer with a fixed capacity and a timeout value.
     * Objects in the buffer that have not been refreshed within the
     * timeout period are removed from the cache.
     *
     * @param capacity the number of objects the buffer can hold
     * @param timeout  the duration, in seconds, an object should
     *                 be in the buffer before it times out
     */
    public FSFTBuffer(int capacity, int timeout) {
        masterMap = new HashMap<>();
        this.timeout = 1000 * timeout;
        this.cap = capacity;
    }

    /**
     * Create a buffer with default capacity and timeout values.
     */
    public FSFTBuffer() {
        this(DSIZE, DTIMEOUT);
    }

    /**
     * Add a value to the buffer.
     * If the buffer is full then remove the least recently accessed
     * object to make room for the new object.
     */
    synchronized public boolean put(T t) {
        long time = System.currentTimeMillis();
        pruneMap(time);

        if (cap > 0) {
            if (masterMap.size() >= cap) {
                masterMap.remove(getOldest());
            }

            masterMap.put(t, time);
            return true;
        }

        return false;
    }

    /**
     * @param id the identifier of the object to be retrieved
     * @return the object that matches the identifier from the
     * buffer. If no such object matches the identifier, an
     * exception will be thrown
     */
    synchronized public T get(String id) throws NoSuchObjectException {

        long time = System.currentTimeMillis();
        pruneMap(time);

        for (T t : masterMap.keySet()) {
            if (t.id().equals(id)) {
                masterMap.put(t, time);
                return t;
            }
        }

        throw new NoSuchObjectException("No such object exists in the list");
    }

    /**
     * Update the last refresh time for the object with the provided id.
     * This method is used to mark an object as "not stale" so that its
     * timeout is delayed.
     *
     * @param id the identifier of the object to "touch"
     * @return true if successful and false otherwise
     */
    synchronized public boolean touch(String id) {
        long time = System.currentTimeMillis();
        pruneMap(time);

        // check if given object exists
        for (T t : masterMap.keySet()) {
            if (t.id().equals(id)) { // if yes
                masterMap.put(t, time); // reset time
                return true;
            }
        } // otherwise return false
        return false;
    }

    /**
     * Update an object in the buffer.
     * This method updates an object and acts like a "touch" to
     * renew the object in the cache.
     *
     * @param t the object to update
     * @return true if successful and false otherwise
     */
    synchronized public boolean update(T t) {
        long time = System.currentTimeMillis();
        pruneMap(time);

        for (T t1 : masterMap.keySet()) {
            if (t1.equals(t)) {
                masterMap.put(t, time);
                return true;
            }
        }
        return false;
    }

    /**
     * @return
     */
    synchronized private T getOldest() {
        T oldest = (T) masterMap.keySet().toArray()[0];

        // finding the object with the smallest time of creation
        for (int i = 0; i < masterMap.size(); i++) {
            if (masterMap.get(oldest) > masterMap.get(masterMap.keySet().toArray()[i])) {
                oldest = (T) masterMap.keySet().toArray()[i];
            }
        }

        return oldest;
    }

    /**
     * @param currentTime
     */
    synchronized private void pruneMap(long currentTime) {
        HashMap<T, Long> copy = new HashMap<>(masterMap);
        HashMap<T, Long> toRemove = new HashMap<>();

        for (Map.Entry<T, Long> entry : copy.entrySet()) {
            if (currentTime - entry.getValue() > timeout) {
                toRemove.put(entry.getKey(), entry.getValue());
            }
        }

        masterMap.entrySet().removeAll(toRemove.entrySet());
    }

    /**
     * @return
     */
    synchronized public Set<T> getCurrentSet() {
        pruneMap(System.currentTimeMillis());
        return new HashSet<>(masterMap.keySet());
    }

    /**
     * @return
     */
    synchronized public int getSize() {
        pruneMap(System.currentTimeMillis());
        return masterMap.size();
    }
}

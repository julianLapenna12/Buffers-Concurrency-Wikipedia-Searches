package cpen221.mp3.fsftbuffer;

import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FSFTBuffer<T extends Bufferable> {

    private final Map<String, DetailedObject> masterMap = new HashMap<>();
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
        if (t == null) return false;

        long time = System.currentTimeMillis();
        pruneMap(time);

        if (cap > 0) {
            if (masterMap.size() >= cap) {
                masterMap.remove(getLeastRecentlyAccessed());
            }

            masterMap.put(t.id(), new DetailedObject(t, time));
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
        if (id == null) throw new NoSuchObjectException("All IDs must be non null.");

        long time = System.currentTimeMillis();
        pruneMap(time);

        if (!masterMap.containsKey(id)) throw new NoSuchObjectException("All IDs must be non null.");

        masterMap.get(id).setAccessTime(time); // updating the access time
        return (T) masterMap.get(id).storedObject;
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
        if (id == null) return false;

        long time = System.currentTimeMillis();
        pruneMap(time);

        // check if given object exists
        if (masterMap.containsKey(id)) {
            masterMap.get(id).setStaleTime(time);
            return true;
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
        if (t == null) return false;

        long time = System.currentTimeMillis();
        pruneMap(time);

        if (!masterMap.containsKey(t.id())) return false;

        masterMap.get(t.id()).setAccessTime(time);
        return true;
    }

    /**
     * @return
     */
    synchronized private String getLeastRecentlyAccessed() {
        ArrayList<DetailedObject> sortedList = new ArrayList<>(masterMap.values());
        sortedList.sort(DetailedObject::compareTo);

        return sortedList.get(sortedList.size() - 1).id();
    }

    /**
     * @param currentTime
     */
    synchronized private void pruneMap(long currentTime) {
        HashMap<String, DetailedObject> copy = new HashMap<>(masterMap);
        HashMap<String , DetailedObject> toRemove = new HashMap<>();

        for (Map.Entry<String, DetailedObject> entry : copy.entrySet()) {
            if (currentTime - entry.getValue().getStaleTime() > timeout) {
                toRemove.put(entry.getKey(), entry.getValue());
            }
        }
        masterMap.entrySet().removeAll(toRemove.entrySet());
    }

    /*
    /**
     * @return
     *
    synchronized public Set<T> getCurrentSet() {
        pruneMap(System.currentTimeMillis());
        return new HashSet<>(masterMap.keySet());
    }
    */

    /**
     * @return
     */
    synchronized public int getSize() {
        pruneMap(System.currentTimeMillis());
        return masterMap.size();
    }
}

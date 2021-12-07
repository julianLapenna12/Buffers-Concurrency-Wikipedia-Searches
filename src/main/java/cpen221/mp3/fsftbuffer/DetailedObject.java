package cpen221.mp3.fsftbuffer;

import org.jetbrains.annotations.NotNull;

/**
 * This class holds parameterized bufferable objects, keeps track of each objects
 * timeout time and time of last use. Each object is identified by its distinct
 * identifier.
 */

public class DetailedObject<T extends Bufferable> implements Comparable {

    /*
    Rep Invariant

    storedObject is not null and has a distinct identifier and if two
        DetailedObjects have the same storedObject, then the DetailedObjects are the same

    staleTime <= currentTimeInMillis() at all times

    accessTime <= currentTimeInMillis() at all times
    */


    /*
    Abstraction function

    This class represents a wrapper for the bufferable object added by the user
    to a buffer. It serves to track the object and its use time and expiry time.

    storedObject is the bufferable object the user wants to add the buffer
        storedObject.id() is it's unique identifier
    staleTime is flag marker such that if the current time exceeds the staleTime
        by more than the timeout, the object is considered to be expired
    accessTime is a flag marker that tracks the last point in time that the
        object was used
    */

    private T storedObject;
    private long staleTime, accessTime;

    /**
     * Creates a new Detailed Object that stores an object,
     * its time of expiry and time of last use.
     *
     * A Detailed Object in a buffer will be removed when
     * the difference between the expiry time and the current
     * time surpasses the buffer's timeout time.
     *
     * All times are expressed in milliseconds since
     * January 1, 1970 UTC.
     *
     * @param t the object to be stored
     * @param timeCreated the time that the object is created
     */
    public DetailedObject(T t, long timeCreated) {
        this.storedObject = t;
        this.staleTime = timeCreated;
        this.accessTime = timeCreated;
    }

    /**
     * @return time of expiry
     */
    public long getStaleTime() {
        return staleTime;
    }

    /**
     * @param staleTime the new time of expiry
     */
    public void setStaleTime(long staleTime) {
        this.staleTime = staleTime;
    }

    /**
     * @return the time of last use
     */
    public long getAccessTime() {
        return accessTime;
    }

    /**
     * @param accessTime the new time of usage
     */
    public void setAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }

    /**
     * @return the identifier of the object stored
     */
    public String id() {
        return storedObject.id();
    }

    /**
     * @param storedObject the most up-to-date version
     *                     of the object
     */
    public void setStoredObject(T storedObject) {
        this.storedObject = storedObject;
    }

    /**
     * @return the stored object
     */
    public T getStoredObject() {
        return storedObject;
    }

    /**
     * The definition of how to compare two Detailed Objects,
     * where the one that has been accessed most recently has
     * a greater value.
     *
     * @param o the object to compare to
     * @return a negative value if the other object has been
     * accessed more recently, a positive value if this object
     * has been accessed more recently, and 0 if both objects
     * were accessed equally recently
     */
    @Override
    public int compareTo(@NotNull Object o) {
        if (!(o instanceof DetailedObject)) return 0;

        DetailedObject other = (DetailedObject) o;
        return (int) (this.accessTime - other.getAccessTime());
    }
}

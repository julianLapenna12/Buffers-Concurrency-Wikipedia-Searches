package cpen221.mp3.fsftbuffer;

import org.jetbrains.annotations.NotNull;

public class DetailedObject<T extends Bufferable> implements Comparable {

    final T storedObject;
    private long staleTime, accessTime;

    public DetailedObject(T t, long timeCreated) {
        this.storedObject = t;
        this.staleTime = timeCreated;
        this.accessTime = timeCreated;
    }

    public long getStaleTime() {
        return staleTime;
    }

    public void setStaleTime(long staleTime) {
        this.staleTime = staleTime;
    }

    public long getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }

    public String id() {
        return storedObject.id();
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (!(o instanceof DetailedObject)) return 0;

        // compareTo should return < 0 if this has been accessed
        // less recently than other, return > 0 if this has been
        // accessed more recently than other and return 0 if
        // they've been accessed equally recently (theoretically
        // it will never return 0)
        DetailedObject other = (DetailedObject) o;
        return (int) (this.accessTime - other.getAccessTime());
    }
}

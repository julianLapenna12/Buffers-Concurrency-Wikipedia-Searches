package cpen221.mp3.fsftbuffer;

public class RandomObject implements Bufferable {
    private final String id;

    public RandomObject(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }
}

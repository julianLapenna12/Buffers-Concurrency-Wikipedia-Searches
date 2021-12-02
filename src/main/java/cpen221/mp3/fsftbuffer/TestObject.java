package cpen221.mp3.fsftbuffer;

public class TestObject implements Bufferable {
    private final String id;

    public TestObject(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }
}

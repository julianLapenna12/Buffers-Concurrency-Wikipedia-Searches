package cpen221.mp3.fsftbuffer;

public class TestObject implements Bufferable {
    private final String id;

    /**
     * Creates a test object with an identifier
     * @param id the object's identifier
     */
    public TestObject(String id) {
        this.id = id;
    }

    /**
     * @return the object's identifier
     */
    @Override
    public String id() {
        return id;
    }
}

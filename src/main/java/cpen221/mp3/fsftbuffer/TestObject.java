package cpen221.mp3.fsftbuffer;

/**
 * This class is a class for testing which acts as a random object type
 */

public class TestObject implements Bufferable {

    /*
    Rep Invariant

    id != null

    If two TestObjects have the same id, they are the same TestObject
    */


    /*
    Abstraction function

    This class represents a random bufferable object that is used to test
    FSFTBuffers. Each distinct TestObject has a distinct identifier.
    */

    private final String id;

    /**
     * Creates a test object with an identifier
     * @param id the object's identifier, not null
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

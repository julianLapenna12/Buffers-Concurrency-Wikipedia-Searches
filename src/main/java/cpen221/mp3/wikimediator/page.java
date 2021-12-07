package cpen221.mp3.wikimediator;

import cpen221.mp3.fsftbuffer.Bufferable;

/**
 * page is used to store wikipedia pages in the FSFT Buffer
 */
public class page implements Bufferable {

    /*
    Rep Invariant

    !id.equals(null)

    !id.equals("")

    !text.equals(null)

    !text.equals("")
    */

    /*
    Abstraction Function

    For any wikipedia page p
    text = the content of that page represented as a string
    id = the title of that page, distinct to that page in particular
    */

    public String id;
    public String text;

    /**
     * Contructor for the class
     * @param pageTitle Title of the page to be constructed
     */
    public page (String pageTitle, String content) {
        text = content;
        id = pageTitle;
    }

    /**
     * Used to identify a page
     * @return the id of the page (the title)
     */
    public String id() {
        return (id);
    }

    /**
     *
     * @return
     */
    public String getText(){
        return text;
    }
}

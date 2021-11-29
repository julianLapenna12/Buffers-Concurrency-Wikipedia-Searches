package cpen221.mp3.wikimediator;

import cpen221.mp3.fsftbuffer.Bufferable;

/**
 * page is used to store wikipedia pages in the FSFT Buffer
 */
public class page implements Bufferable {
    /*Used to store the id of the page*/
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

    public String getText(){
        return text;
    }
}

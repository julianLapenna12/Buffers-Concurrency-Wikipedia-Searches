package cpen221.mp3.wikimediator;

/**
 * This class holds a node in the graph created when searching for paths between
 * two Wikipedia pages. It acts as a one directional node where it is linked
 * only to its parent node.
 */

public class WikiNode {

    /*
    Rep Invariant

    id is the distinct identifier and is never null

    id always has a corresponding Wikipedia page with a matching title
        (unless it was a title passed by the user that doesn't exist)

    If the id of two WikiNodes are the same, then they are the same WikiNode

    parent is null if and only if it is the source of the search path to another Wikipedia page
    */


    /*
    Abstraction function

    This class represents a Wikipedia page in a graph of Wikipedia pages
        that can be accessed by internal link from its parent page
    */

    private final WikiNode parent;
    private final String id;

    /**
     * Create a WikiNode that represents a Wikipedia page and some (limited)
     * information about it
     * @param id the title of the WikiNode. The title corresponds the title of
     *           its associated Wikipedia page
     * @param parent a WikiNode that is forward linked to this WikiNode. This
     *               represents a Wikipedia page that can link to the current page
     */
    public WikiNode(String id, WikiNode parent) {
        this.id = id;
        this.parent = parent;
    }

    /**
     *
     * @return the unique title of the WikiNode
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return the parent of the WikiNode
     */
    public WikiNode getParent() {
        return parent;
    }
}

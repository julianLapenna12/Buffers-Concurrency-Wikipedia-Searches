package cpen221.mp3.wikimediator;

import java.util.ArrayList;

public class WikiNode {

    private ArrayList<WikiNode> children;
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

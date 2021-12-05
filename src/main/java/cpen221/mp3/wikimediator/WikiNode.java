package cpen221.mp3.wikimediator;

import java.util.ArrayList;

public class WikiNode {

    private ArrayList<WikiNode> children;
    private WikiNode parent;
    private String id;

    /**
     *
     * @param id
     */
    public WikiNode(String id) {
        this.id = id;
    }

    /**
     *
     * @param id
     * @param parent
     */
    public WikiNode(String id, WikiNode parent) {
        this.id = id;
        this.parent = parent;
    }

    /**
     *
     * @return
     */
    public ArrayList<WikiNode> getChildren() {
        return new ArrayList<>(children);
    }

    /**
     *
     * @param children
     */
    public void setChildren(ArrayList<WikiNode> children) {
        this.children = children;
    }

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public WikiNode getParent() {
        return parent;
    }
}

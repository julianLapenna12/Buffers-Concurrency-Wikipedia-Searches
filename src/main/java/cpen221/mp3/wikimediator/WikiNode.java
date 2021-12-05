package cpen221.mp3.wikimediator;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class WikiNode {

    private ArrayList<WikiNode> children;
    private WikiNode parent;
    private final String id;

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
    /*
    /**
     *
     * @param o
     * @return
     *//*
    @Override
    public int compareTo(@NotNull Object o) {
        if(!(o instanceof WikiNode)) return 0;
        WikiNode other = (WikiNode) o;

        return other.getId().compareTo(this.id);
    }
    */
}

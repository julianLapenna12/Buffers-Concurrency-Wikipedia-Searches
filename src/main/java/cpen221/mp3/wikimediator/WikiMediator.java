package cpen221.mp3.wikimediator;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.fastily.jwiki.core.Wiki;

import java.util.*;
import java.util.concurrent.TimeoutException;

public class WikiMediator {

    /* TODO: Implement this datatype

        You must implement the methods with the exact signatures
        as provided in the statement for this mini-project.

        You must add method signatures even for the methods that you
        do not plan to implement. You should provide skeleton implementation
        for those methods, and the skeleton implementation could return
        values like null.

     */
    private FSFTBuffer pageData;

    private List<Long> searchRequests = new ArrayList<Long>();

    private List<Long> requests = new ArrayList<Long>();

    private Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();

    /**
     * Constructor that creates new pageData database
     *
     * @param capacity          Capacity of the database
     * @param stalenessInterval staleness interval (in seconds) for pages in the database
     */
    public WikiMediator(int capacity, int stalenessInterval) {
        pageData = new FSFTBuffer(capacity, stalenessInterval);
    }

    /**
     * Given a query, return up to limit the number of pages that match the query
     *
     * @param query query to search in wikipedia
     * @param limit number of elements that will be returned
     * @return A list of all the wikipedia pages matching the query
     */
    List<String> search(String query, int limit) {
        searchRequests.add(System.currentTimeMillis());
        return (wiki.search(query, limit));
    }

    /**
     *
     * @param pageTitle1
     * @param pageTitle2
     * @param timeout
     * @return
     * @throws TimeoutException
     */
    public List<String> shortestPath(String pageTitle1, String pageTitle2, int timeout) throws TimeoutException {
        long endTime = System.currentTimeMillis() + (timeout * 1000L);

        // create initial node with no children
        WikiNode startNode = new WikiNode(pageTitle1, null);

        ArrayList<WikiNode> queue = new ArrayList<>();
        ArrayList<WikiNode> searched = new ArrayList<>();
        ArrayList<String> path = new ArrayList<>();

        queue.add(startNode);
        WikiNode node;

        // add the first node to queue and search
        for (int i = 0; i < queue.size(); i++) {
            node = queue.get(i);
            node.setChildren(buildNode(node));

            // if the node in queue is the node we want
            if (node.getId().equals(pageTitle2)) {

                // add it to searched, generate its path and end the search
                searched.add(node);
                path = getPath(node);
                break;

                // otherwise if it also hasn't already been searched
            } else if (!searched.contains(node)) {

                // add it to searched
                searched.add(node);

                // and add its children (in lexicographical order to the queue
                queue.addAll(node.getChildren());
            }

            // we don't want to exceed that timeout!
            if (System.currentTimeMillis() > endTime) {
                throw new TimeoutException("shortest path search timed-out.");
            }
        }
        // return its path which if no path was found
        // is an empty array list, and otherwise is the shortest
        // lexicographical path
        return path;
    }

    /**
     *
     * @param page
     * @return
     */
    private ArrayList<WikiNode> buildNode(WikiNode page) {
        ArrayList<WikiNode> children = new ArrayList<>();
        for (String s : wiki.getLinksOnPage(page.getId())){
            children.add(new WikiNode(s, page));
        }

        return children;
    }

    /**
     *
     * @param w
     * @return
     */
    private ArrayList<String> getPath(WikiNode w) {
        ArrayList<String> path = new ArrayList<>();

        if (w.getParent() != null) {
            path.addAll(getPath(w.getParent()));
        }

        path.add(w.getId());
        return path;
    }
}

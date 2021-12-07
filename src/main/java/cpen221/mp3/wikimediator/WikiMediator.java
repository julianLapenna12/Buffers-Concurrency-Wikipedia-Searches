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
    public List<String> search(String query, int limit) {
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

        if (pageTitle1.equals(pageTitle2)) return new ArrayList<>(Collections.singleton(pageTitle1));

        // create initial node with no children
        WikiNode startNode = new WikiNode(pageTitle1, null);

        ArrayList<WikiNode> queue = new ArrayList<>();

        ArrayList<String> queueStrings = new ArrayList<>();
        ArrayList<String> nodeLinks;
        ArrayList<String> searchedStrings = new ArrayList<>();

        ArrayList<String> path = new ArrayList<>();

        queueStrings.add(pageTitle1);
        queue.add(startNode);

        WikiNode node;
        String nodeString;

        // add the first node to queue and search
        for (int i = 0; i < queueStrings.size(); i++) {
            nodeString = queueStrings.get(i);
            node = queue.get(i);
            nodeLinks = wiki.getLinksOnPage(nodeString);

            // if the node's links contain the node we want
            if (nodeLinks.contains(pageTitle2)) {

                // add it to searched, generate its path and end the search
                searchedStrings.add(nodeString);
                path = getPath(node);
                break;

                // otherwise if it also hasn't already been searched
            } else if (!searchedStrings.contains(nodeString)) {

                // add it to searched
                searchedStrings.add(nodeString);

                // and add its children (in lexicographical order) to the queue
                queueStrings.addAll(nodeLinks);

                for (String nodeLink : nodeLinks) {
                    queue.add(new WikiNode(nodeLink, node));
                }
            }

            // we don't want to exceed that timeout!
            if (System.currentTimeMillis() > endTime) {
                throw new TimeoutException("shortest path search timed-out.");
            }
        }
        // return its path which if no path was found
        // is an empty array list, and otherwise is the shortest
        // lexicographical path
        if (path.size() == 0) return path;
        path.add(pageTitle2);
        return path;
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

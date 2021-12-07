package cpen221.mp3.wikimediator;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.fastily.jwiki.core.Wiki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;


public class WikiMediator {

    /* TODO: Implement this datatype

        You must implement the methods with the exact signatures
        as provided in the statement for this mini-project

        You must add method signatures even for the methods that you
        do not plan to implement. You should provide skeleton implementation
        for those methods, and the skeleton implementation could return
        values like null.

     */
    private FSFTBuffer pageData;

    private List<Long> searchRequests = new ArrayList<Long>();
    private List<Long> requests = new ArrayList<Long>();
    private final Map<String, Integer> requestMap = new HashMap<String, Integer>();

    private final Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();

    /**
     * Constructor that creates new pageData database
     *
     * @param capacity          Capacity of the database
     * @param stalenessInterval staleness interval (in seconds) for pages in the database
     */
    public WikiMediator(int capacity, int stalenessInterval) {
        pageData = new FSFTBuffer<page>(capacity, stalenessInterval);
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
        requests.add(System.currentTimeMillis());
        int timesPrevRequested = 0;

        int prevRequestCount = requestMap.getOrDefault(query, 0);
        requestMap.put(query, ++prevRequestCount);

        List<String> results;
        results = wiki.search(query, limit);
        return (results);
    }

    /**
     * Given a page title, return the text of the page
     * @param pageTitle Page title of data to return
     * @return String of the text of the page
     */
    public String getPage(String pageTitle) {
        searchRequests.add(System.currentTimeMillis());
        requests.add(System.currentTimeMillis());

        int prevRequestCount = requestMap.getOrDefault(pageTitle, 0);
        requestMap.put(pageTitle, ++prevRequestCount);

        String result;

        page currentPage;

        try {
            currentPage = (page)pageData.get(pageTitle);
            result = currentPage.getText();
        }
        catch (Exception e){
            currentPage = new page(pageTitle, wiki.getPageText(pageTitle));
            result = currentPage.getText();
            pageData.put(currentPage);
        }
        return result;
    }

    /**
     *
     * @param pageTitle1 The title of the source Wikipedia page
     * @param pageTitle2 The title of the destination Wikipedia page
     * @param timeout the duration in seconds that the search may last
     * @return An ordered list of Wikipedia pages that can be traversed by
     *         internal Wikipedia links to arrive at the destination from
     *         the source (inclusive). The list provides the shortest possible path
     *         and in the case of a tie, the lowest lexicographical path
     * @throws TimeoutException If the operation is not successful in the
     *                          allotted time an exception will be thrown
     */
    public List<String> shortestPath(String pageTitle1, String pageTitle2, int timeout) throws TimeoutException {
        long endTime = System.currentTimeMillis() + (timeout * 1000L);

        // for the case of the start and end being the same pages
        // and when there is a page that nothing links to
        if (pageTitle1.equals(pageTitle2)) return new ArrayList<>(Collections.singleton(pageTitle1));
        if (wiki.whatLinksHere(pageTitle2).size() == 0) return new ArrayList<>();

        // create initial node with no parent
        WikiNode startNode = new WikiNode(pageTitle1, null);

        // Arraylist to store the nodes which create the path upon finding the destination
        ArrayList<WikiNode> queue = new ArrayList<>();

        // Arraylists to store the queue, searched and temp storage of the page links
        ArrayList<String> queueStrings = new ArrayList<>();
        ArrayList<String> nodeLinks;
        ArrayList<String> searchedStrings = new ArrayList<>();

        // Arraylist storing the path that will be returned
        ArrayList<String> path = new ArrayList<>();

        queue.add(startNode);
        queueStrings.add(pageTitle1);
        WikiNode node;
        String nodeString;

        // add the first node to queue and search
        for (int i = 0; i < queueStrings.size(); i++) {
            nodeString = queueStrings.get(i);
            node = queue.get(i);
            nodeLinks = wiki.getLinksOnPage(nodeString);

            // if the node's links contain the node we want
            if (nodeLinks.contains(pageTitle2)) {

                // generate its path and end the search
                path = getPath(node);
                break;

                // otherwise, if it also hasn't already been searched
            } else if (!searchedStrings.contains(nodeString)) {

                // add it to searched
                searchedStrings.add(nodeString);

                // add its children (in lexicographical order) to the queue
                queueStrings.addAll(nodeLinks);
                for (String s : nodeLinks) { // and add its children as nodes to their queue
                    queue.add(new WikiNode(s, node));
                }
            }

            // we don't want to exceed that timeout!
            if (System.currentTimeMillis() > endTime) {
                throw new TimeoutException("shortest path search timed-out.");
            }
        }
        // return its path which if no path was found is an empty array list,
        // and otherwise is the shortest lexicographical path
        if (path.size() == 0) return path;
        path.add(pageTitle2);
        return path;
    }

    /**
     * Given a child WikiNode, traverse up the tree of its parents to
     * find a path from the upper-most parent to the given child WikiNode
     * @param w the child WikiNode
     * @return a list of IDs including the parent and child nodes that
     *         represent a path that can be taken from the parent Wikipedia
     *         page to arrive at the child Wikipedia page
     */
    private ArrayList<String> getPath(WikiNode w) {
        ArrayList<String> path = new ArrayList<>();

        if (w.getParent() != null) {
            path.addAll(getPath(w.getParent()));
        }

        path.add(w.getId());
        return path;
    }


    /**
     * @param limit
     * @return
     */
    public List<String> zeitgeist(int limit) {
        List<String> returnList = requestMap.entrySet().stream()
                                            .sorted(Comparator.comparingInt(Map.Entry::getValue))
                                            .map(Map.Entry::getKey)
                                            .collect(Collectors.toList());
        Collections.reverse(returnList);

        return returnList;
    }
}

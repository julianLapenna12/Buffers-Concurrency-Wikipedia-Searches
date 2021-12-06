package cpen221.mp3.wikimediator;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.fastily.jwiki.core.Wiki;

import java.util.*;
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
    private Map<String, Integer> requestMap = new HashMap<String, Integer>();

    private Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();

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

        List<String> results = new ArrayList<String>();
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


    /*
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

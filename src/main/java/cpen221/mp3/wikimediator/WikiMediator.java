package cpen221.mp3.wikimediator;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.fastily.jwiki.core.Wiki;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
     * @param stalenessInterval staleness interval for pages in the database
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


    List<String> shortestPath(String pageTitle1, String pageTitle2, int timeout) throws TimeoutException {
        long endTime = System.currentTimeMillis() + (timeout * 1000L);

        ArrayList<String> queue = new ArrayList<>();
        ArrayList<String> searched = new ArrayList<>();

        String page;
        queue.add(pageTitle1);

        for (int i = 0; i < queue.size(); i++) {
            page = queue.get(i);
            if (page.equals(pageTitle2)) {
                searched.add(page);
                break;
            } else if (!searched.contains(page)) {
                searched.add(page);
                ArrayList<String> pageLinks;

                pageLinks = wiki.getLinksOnPage(page);
                Collections.sort(pageLinks);
                queue.addAll(pageLinks);
            }
            if (System.currentTimeMillis() > endTime) {
                throw new TimeoutException("search took too long");
            }
        }
        if (!searched.contains(pageTitle2)) {
            return new ArrayList<>();
        }
        return searched;

    }
}

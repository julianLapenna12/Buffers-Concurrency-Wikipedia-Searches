package cpen221.mp3.wikimediator;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.fastily.jwiki.core.Wiki;

import java.util.*;
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
     * @param capacity Capacity of the database
     * @param stalenessInterval staleness interval for pages in the database
     */
    public WikiMediator(int capacity, int stalenessInterval) {
        pageData = new FSFTBuffer<page>(capacity, stalenessInterval);
    }

    /**
     * Given a query, return up to limit the number of pages that match the query
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

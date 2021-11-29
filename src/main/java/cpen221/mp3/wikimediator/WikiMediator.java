package cpen221.mp3.wikimediator;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.fastily.jwiki.core.Wiki;
import java.util.ArrayList;
import java.util.List;

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

    private FSFTBuffer searchData;

    private List<Long> searchRequests = new ArrayList<Long>();

    private List<Long> requests = new ArrayList<Long>();

    private Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();

    /**
     * Constructor that creates new pageData database
     * @param capacity Capacity of the database
     * @param stalenessInterval staleness interval for pages in the database
     */
    public WikiMediator(int capacity, int stalenessInterval) {
        pageData = new FSFTBuffer<page>(capacity, stalenessInterval);
        searchData = new FSFTBuffer<search>(capacity, stalenessInterval);
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

        List<String> results = new ArrayList<String>();
        search currentSearch;
        search requestedSearch;

        try {
             currentSearch = (search)searchData.get(query);
             if (currentSearch.getLimit() < limit) {
                 results = wiki.search(query, limit);
             }
             else {
                 results = currentSearch.getData();
             }

            requestedSearch = new search(query, limit, results);
            searchData.put(requestedSearch);
        }
        catch (Exception e){
            results = wiki.search(query, limit);

            requestedSearch = new search(query, limit, results);
            searchData.put(requestedSearch);
        }
        return (results);
    }

    public String getPage(String pageTitle) {
        searchRequests.add(System.currentTimeMillis());
        requests.add(System.currentTimeMillis());

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
}

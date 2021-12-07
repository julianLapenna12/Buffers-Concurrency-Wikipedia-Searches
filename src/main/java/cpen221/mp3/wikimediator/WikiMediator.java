package cpen221.mp3.wikimediator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.fastily.jwiki.core.Wiki;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class mediates interactions between the user and the api.  It also keeps track of certain metrics, like the most searched pages and searches.
 */

public class WikiMediator {

    /*
    Rep Invariant
    requests.size() >= Total number of entries in all requestMap value lists
    Each entry in requestMap values must have corresponding requests entry
    requestMap can never have an empty list as it's corresponding value
    pageData size must never exceed total entries in requestMap value lists
    */


    /*
    Abstraction function
    This class represents a mediator between a user and wikipedia
    pageData represents a finite size finite time buffer used for caching previously accessed pages
    requests represents all the previous requests made to the mediator, by the time they were requested, in milliseconds, since 12:00am UTC January 1st, 1970
    requestMap maps a certain request to the number of times it was requested, as well as when those requests were initiated.  Similair to requests, these times are stored as milliseconds since 12:00am UTC January 1st, 1970
    */

    /*
    Thread Safety Argument
    The datatype is thread safe as all the mutable collections it uses are thread safe.  Any possible data races that may occur between these elements, moreover, are synchronized, so that no data races occur
    No method supports removal from data structures
    The other variables it uses are strings, which are immutable, and a wiki, which is never mutated or accessed directly
    The FSFT pageData is assumed to be a threadsafe datatype, as per task 2
    */

    private FSFTBuffer pageData;

    private List<Long> searchRequests = new ArrayList<Long>();

    private List<Long> requests = new ArrayList<Long>();

    private Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();

    private Map<String, ArrayList<Long>> requestMap = Collections.synchronizedMap(new HashMap<String, ArrayList<Long>>());
    private String requestMapFileLocation = "local/dataMap.json";
    private String requestListFileLocation = "local/dataList.json";

    /**
     * Constructor that creates new pageData database, and loads in all previous data (if any exists) from local data.json file used to store all data
     * @param capacity Capacity of the database.  Must be greater than or equal to 1
     * @param stalenessInterval staleness interval for pages in the database in seconds.  Must be greater than or equal to 1
     */
    public WikiMediator(int capacity, int stalenessInterval) {
        pageData = new FSFTBuffer<page>(capacity, stalenessInterval);

        synchronized (this) {
            try {
                Gson gson = new Gson();

                Reader readerMap = Files.newBufferedReader(Paths.get(requestMapFileLocation));
                try {
                    requestMap = gson.fromJson(readerMap, new TypeToken<Map<String, List<Long>>>() {
                    }.getType());
                } catch (NullPointerException e) {
                    requestMap = Collections.synchronizedMap(new HashMap<String, ArrayList<Long>>());
                }
                readerMap.close();

                Reader readerList = Files.newBufferedReader(Paths.get(requestListFileLocation));
                requests = gson.fromJson(readerList, new TypeToken<List<Long>>() {
                }.getType());
                readerList.close();

            } catch (Exception e) {
                System.out.println("Unable to read data from JSON file");
            }
        }
    }

    /**
     * @param query query to search in wikipedia.  Cannot be an empty string
     * @param limit number of elements that will be returned.  Must be greater than or equal to 1
     * @return A list of all the wikipedia pages matching the query
     */
    public List<String> search(String query, int limit) {
        requests.add(System.currentTimeMillis());

        List<Long> defaultList = new ArrayList<Long>();
        defaultList.add(System.currentTimeMillis());

        synchronized (this) {
            if (requestMap.containsKey(query)) {
                requestMap.get(query).add(System.currentTimeMillis());
            } else {
                requestMap.put(query, (ArrayList<Long>) defaultList);
            }
        }

        List<String> results = new ArrayList<String>();
        results = wiki.search(query, limit);
        return (results);
    }

    /**
     * Given a page title, return the text of the page
     * @param pageTitle Page title of data to return
     * @return String of the text of the page.  Returns an empty string if no corresponding wikipedia page is found
     */
    public String getPage(String pageTitle) {
        requests.add(System.currentTimeMillis());

        List<Long> defaultList = new ArrayList<Long>();
        defaultList.add(System.currentTimeMillis());

        synchronized (this) {
            if (requestMap.containsKey(pageTitle)) {
                requestMap.get(pageTitle).add(System.currentTimeMillis());
            } else {
                requestMap.put(pageTitle, (ArrayList<Long>) defaultList);
            }
        }

        String result;
        page currentPage;

        //Try to find pageTitle in cache.  If not cached, find page in wikipedia and cache page
        try {
            currentPage = (page) pageData.get(pageTitle);
            result = currentPage.getText();
        } catch (Exception e) {
            currentPage = new page(pageTitle, wiki.getPageText(pageTitle));
            result = currentPage.getText();
            pageData.put(currentPage);
        }

        return result;
    }

    /**
     * Returns the most common search queries and getPage requests as a sorted list since the start of Wikimediator
     * @param limit Maximum number of elements to return.  Must be greater than or equal to 1
     * @return A list of search queries and getPage requests of size limit sorted in decreasing order by the number of times they were requested
     */
    public List<String> zeitgeist(int limit) {
        requests.add(System.currentTimeMillis());

        Map<String, Integer> listToSort = new HashMap<String, Integer>();

        //Creates a list to sort that simply concatenates the list of times into a single number representing number of searches
        synchronized (this) {
            for (int i = 0; i < requestMap.size(); i++) {
                listToSort.put((String) requestMap.keySet().toArray()[i], requestMap.get(requestMap.keySet().toArray()[i]).size());
            }
        }

        //Creates a return list of strings based on the number of searches for each entry
        List<String> listToReturn = listToSort.entrySet().stream()
                                            .sorted(Comparator.comparingInt(Map.Entry::getValue))
                                            .map(Map.Entry::getKey)
                                            .collect(Collectors.toList());
        Collections.reverse(listToReturn);

        //Ensures that limit is not too large for the return list
        if (limit >= listToReturn.size()) {
            return listToReturn;
        }

        return listToReturn.subList(0, limit);
    }

    /**
     * Finds the most searched items in the past timeLimitInSeconds seconds
     * @param timeLimitInSeconds Amount of seconds since current time to begin accepting searches as valid.  Must be greater than or equal to 1
     * @param maxItems Maximum number of items to return.  Must be greater than or equal to 1
     * @return A list in decreasing order of searches based on the number of times they have been searched in the past timeLimitInSeconds seconds
     */
    public List<String> trending(int timeLimitInSeconds, int maxItems) {
        requests.add(System.currentTimeMillis());

        Map<String, Integer> listToSort = new HashMap<String, Integer>();

        //Creates a list to sort by taking out all entries not within the time window, and returning a map with queries and the respective number of entries
        synchronized (this) {
            for (int i = 0; i < requestMap.size(); i++) {
                for (int j = 0; j < requestMap.get(requestMap.keySet().toArray()[i]).size(); j++) {
                    if ((long) (requestMap.get(requestMap.keySet().toArray()[i]).get(j)) > System.currentTimeMillis() - timeLimitInSeconds*1000) {
                        if (!listToSort.containsKey(requestMap.keySet().toArray()[i])) {
                            listToSort.put((String) requestMap.keySet().toArray()[i], 1);
                        } else {
                            listToSort.put((String) requestMap.keySet().toArray()[i], listToSort.get(requestMap.keySet().toArray()[i]) + 1);
                        }
                    }
                }
            }
        }

        //Creates a return list of strings based on the number of searches for each entry
        List<String> returnList = listToSort.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        Collections.reverse(returnList);

        //Ensures that limit is not too big for the return list
        if (maxItems >= returnList.size()) {
            return returnList;
        }

        return returnList.subList(0, maxItems);
    }

    /**
     * Returns the largest number of requests in a given time window
     * @param timeWindowInSeconds Window for which to search in.  Must be greater than or equal to 1
     * @return The highest number of requests in a given window
     */
    public int windowedPeakLoad(int timeWindowInSeconds) {
        requests.add(System.currentTimeMillis());

        requests.add(System.currentTimeMillis());
        int currentTotal = 0;
        int largestTotal = 0;

        synchronized (this) {
            for (int i = 0; i < requests.size(); i++) {
                currentTotal = countInWindow(requests, requests.get(i) - timeWindowInSeconds * 1000, requests.get(i) + 1);
                if (currentTotal > largestTotal) {
                    largestTotal = currentTotal;
                }
            }
        }

        return largestTotal;
    }

    /**
     * Returns the largest number of requests in a 30 second time window
     * @return Maximum number of requests, as an integer, within 30 seconds of one another
     */
    public int windowedPeakLoad() {
        return windowedPeakLoad(30);
    }

    /**
     * Used to calculate the number of elements in an array of longs within a time window
     * @param list List for which to search in
     * @param start Lower bound on Long values
     * @param end Upper bound on Long values
     * @return The number of Long values within the lower and upper bounds
     */
    private int countInWindow (List<Long> list, Long start, Long end) {
        int count = 0;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) >= start && list.get(i) < end) {
                count++;
            }
        }

        return count;
    }

    /**
     * Saves working data to a local .json filesystem consisting of two json files
     */
    public void writeToFile() {

        try {
            Gson gson = new Gson();

            Writer writerMap = new FileWriter("local/dataMap.json");
            new Gson().toJson(requestMap, writerMap);
            writerMap.close();
        }
        catch (Exception e) {
            System.out.println("Test has Failed - unable to write to map json");
        }

        try  {
            Gson gson = new Gson();
            Writer writerList = new FileWriter("local/dataList.json");
            new Gson().toJson(requests, writerList);
            writerList.close();
        }
        catch (Exception e) {
            System.out.println("Test has Failed - unable to write to list json");
        }
    }

}

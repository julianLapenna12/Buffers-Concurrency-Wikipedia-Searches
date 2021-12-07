package cpen221.mp3.wikimediator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import kotlin.jvm.internal.TypeReference;
import org.fastily.jwiki.core.Wiki;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Creates a new instance of wikimediator
 */

public class WikiMediator {

    private FSFTBuffer pageData;

    private List<Long> requests = new ArrayList<Long>();

    private Map<String, ArrayList<Long>> requestMap = new HashMap<String, ArrayList<Long>>();

    private String requestMapFileLocation = "local/dataMap.json";
    private String requestListFileLocation = "local/dataList.json";

    private Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();

    /**
     * Constructor that creates new pageData database, and loads in all previous data (if any exists) from local data.json file used to store all data
     * @param capacity Capacity of the database
     * @param stalenessInterval staleness interval for pages in the database
     */
    public WikiMediator(int capacity, int stalenessInterval) {




        pageData = new FSFTBuffer<page>(capacity, stalenessInterval);

        try {
            Gson gson = new Gson();

            Reader readerMap = Files.newBufferedReader(Paths.get(requestMapFileLocation));
            requestMap = gson.fromJson(readerMap, new TypeToken<Map<String, List<Long>>>(){}.getType());
            readerMap.close();

            //By default, program will read data from json file as a double, and thus data needs to be converted into a long


            Reader readerList = Files.newBufferedReader(Paths.get(requestListFileLocation));
            requests = gson.fromJson(readerList, new TypeToken<List<Long>>(){}.getType());
            readerList.close();



        }
        catch (Exception e) {

        }

    }

    /**
     * Given a query, return up to limit the number of pages that match the query
     *
     * @param query query to search in wikipedia
     * @param limit number of elements that will be returned
     * @return A list of all the wikipedia pages matching the query
     */
    public List<String> search(String query, int limit) {
        requests.add(System.currentTimeMillis());

        List<Long> defaultList = new ArrayList<Long>();
        defaultList.add(System.currentTimeMillis());

        if (requestMap.containsKey(query)) {
            requestMap.get(query).add(System.currentTimeMillis());
        }
        else {
            requestMap.put(query, (ArrayList<Long>) defaultList);
        }

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
        requests.add(System.currentTimeMillis());

        List<Long> defaultList = new ArrayList<Long>();
        defaultList.add(System.currentTimeMillis());

        if (requestMap.containsKey(pageTitle)) {
            requestMap.get(pageTitle).add(System.currentTimeMillis());
        }
        else {
            requestMap.put(pageTitle, (ArrayList<Long>) defaultList);
        }

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
        requests.add(System.currentTimeMillis());

        Map<String, Integer> listToSort = new HashMap<String, Integer>();

        //Creates a list to sort that simply concatenates the list of times into a single number representing number of searches
        for (int i = 0; i < requestMap.size(); i++) {
            listToSort.put((String)requestMap.keySet().toArray()[i], requestMap.get(requestMap.keySet().toArray()[i]).size());
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
     * @param timeLimitInSeconds Amount of seconds since current time to begin accepting searches as valid
     * @param maxItems Maximum number of items to return
     * @return A list in decreasing order of searches based on the number of times they have been searched in the past timeLimitInSeconds seconds
     */
    public List<String> trending(int timeLimitInSeconds, int maxItems) {
        requests.add(System.currentTimeMillis());

        Map<String, Integer> listToSort = new HashMap<String, Integer>();

        //Creates a list to sort by taking out all entries not within the time window, and returning a map with queries and the respective number of entries
        for (int i = 0; i < requestMap.size(); i++) {
            for (int j = 0; j < requestMap.get(requestMap.keySet().toArray()[i]).size(); j++) {
                if ((long)(requestMap.get(requestMap.keySet().toArray()[i]).get(j))/1000 > System.currentTimeMillis()/1000 - timeLimitInSeconds) {
                    if (!listToSort.containsKey(requestMap.keySet().toArray()[i])) {
                        listToSort.put((String) requestMap.keySet().toArray()[i], 1);
                    }
                    else {
                        listToSort.put((String) requestMap.keySet().toArray()[i], listToSort.get(requestMap.keySet().toArray()[i])+1);
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
     * Returns the largest number of searches in a given time window
     * @param timeWindowInSeconds Window for which to search in
     * @return The highest number of searches in a given window
     */
    public int windowedPeakLoad(int timeWindowInSeconds) {
        requests.add(System.currentTimeMillis());
        int currentTotal = 0;
        int largestTotal = 0;

        for (int i = 0; i < requests.size(); i++) {
            currentTotal = countInWindow(requests, requests.get(i)-timeWindowInSeconds*1000, requests.get(i)+1);
            if (currentTotal > largestTotal) {
                largestTotal = currentTotal;
            }
        }

        return largestTotal;
    }

    public int windowedPeakLoad() {

        return windowedPeakLoad(30);
    }

    private int countInWindow (List<Long> list, Long start, Long end) {
        int count = 0;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) >= start && list.get(i) < end) {
                count++;
            }
        }

        return count;
    }
}

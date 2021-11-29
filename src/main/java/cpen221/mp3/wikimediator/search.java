package cpen221.mp3.wikimediator;

import cpen221.mp3.fsftbuffer.Bufferable;

import java.util.List;

public class search implements Bufferable{
    private String id;
    private int size;
    private List<String> resultList;

    /**
     *
     * @param search
     * @param limit
     * @param results
     */
    public search(String search, int limit, List<String> results){
        id = search;
        size = limit;
        resultList = results;
    }

    public List<String> getData() {
        return resultList;
    }

    public int getLimit() {
        return size;
    }

    public String id() {
        return id;
    }
}

package cpen221.mp3.server;

import org.fastily.jwiki.core.Wiki;

public class WikiResponse {
    public String id;
    public String status;
    public Object response;

    /**
     *
     */
    public WikiResponse(){

    }

    /**
     *
     * @param id
     * @param response
     * @param status
     */
    public WikiResponse(String id, Object response, String status){
        this.id = id;
        this.status = status;
        this.response = response;
    }

    /**
     *
     * @param id
     * @param response
     */
    public WikiResponse(String id, Object response){
        this.id = id;
        this.response = response;
    }
}

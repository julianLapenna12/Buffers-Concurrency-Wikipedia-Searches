package cpen221.mp3.server;

import org.fastily.jwiki.core.Wiki;

public class WikiResponse {
    public String id;
    public String status;
    public Object response;
    public WikiResponse(){

    }

    public WikiResponse(String id, Object response){
        this.id = id;
        this.response = response;
    }
}

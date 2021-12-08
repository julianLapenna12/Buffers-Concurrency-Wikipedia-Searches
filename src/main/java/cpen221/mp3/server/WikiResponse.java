package cpen221.mp3.server;

import org.fastily.jwiki.core.Wiki;

/**
 * WikiResponse represents the JSON object that the server uses to respond
 * RI; ID is always a value, status is either success or failed, response is either a string or array of strings
 */
class WikiResponse {
    public String id;
    public String status;
    public Object response;

    /**
     * @param id the set ID of the response
     * @param response the response from Wikimediator, either a string a array of string
     */

    public WikiResponse(String id, Object response){
        this.id = id;
        this.response = response;
    }

    /**
     * Empty Constructor Overload
     */
    public WikiResponse() {

    }
}

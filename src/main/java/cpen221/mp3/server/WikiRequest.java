package cpen221.mp3.server;

/**
 * Wikirequest represents the JSON object used by the client to send requests to the server
 * RI: Id is always a value, type is a valid method from WikiMediator given in the list in WikiMediator Server,
 * and for each valid method call all of its respective parameter are not null.
 */
class WikiRequest {
    public String id;
    public String type;
    public String query;
    public String pageTitle;
    public int timeLimitInSeconds;
    public int maxItems;
    public Integer timeWindowInSeconds;
    public String pageTitle1;
    public int limit;
    public String pageTitle2;
    public Integer timeout;
}

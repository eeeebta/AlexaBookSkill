package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class GoodreadsResponse {
    // @JsonProperty("search")
    private JsonNode search;

    // Ignoring field causes error
    // @JsonIgnore
    // @JsonProperty("Request")
    private JsonNode Request;

    private results returnedResults = new results();
    private String resultVar = returnedResults.returnResults();

    public void getBestBook() {
        System.out.println(resultVar);
    }
}

class results {
    @JsonProperty("results")
    private JsonNode results;
    public String returnResults() {
        return results.toString();
    }
}

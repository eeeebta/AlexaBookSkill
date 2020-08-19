package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class BookList {
    // List name and list of books
    private String listName;
    private Map<String, BookDetails> bookDetails;


    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }


    public Map<String, BookDetails> getBookDetails() {
        return bookDetails;
    }

    public void setBookDetails(Map<String, BookDetails> bookDetails) {
        this.bookDetails = bookDetails;
    }
}

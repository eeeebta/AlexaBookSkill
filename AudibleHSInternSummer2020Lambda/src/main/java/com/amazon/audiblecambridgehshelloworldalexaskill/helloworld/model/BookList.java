package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model;

import org.json.JSONObject;

import java.util.List;

public class BookList {
    // List name and list of books
    private String listName;
    private List<BookDetails> bookDetails;


    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public List<BookDetails> getBookDetails() {
        return bookDetails;
    }

    public void setBookDetails(List<BookDetails> bookDetails) {
        this.bookDetails = bookDetails;
    }

}

package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookList {
    // List name and list of books
    private String listName;
    private Map<String, List<BookDetails>> bookDetails;

    public BookList() {
        this.setBookDetails(new HashMap<>());
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }


    public Map<String, List<BookDetails>> getBookDetails() {
        return bookDetails;
    }

    public void setBookDetails(Map<String, List<BookDetails>> bookDetails) {
        this.bookDetails = bookDetails;
    }
}

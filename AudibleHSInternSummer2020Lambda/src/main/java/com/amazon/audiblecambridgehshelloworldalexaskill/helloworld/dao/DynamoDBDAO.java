package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.dao;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers.Utility;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookDetails;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookList;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookSaveStatus;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.ReadingListResponse;
import com.amazonaws.services.dynamodbv2.xspec.S;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamoDBDAO {

    public BookSaveStatus saveList(String listName, BookDetails bookDetails, HandlerInput input) {
        try {
            // To write book to a list per user

            // Gets existing bookLists/already existing objects
            Map<String, Object> persistentAttributes = new HashMap<>(input.getAttributesManager().getPersistentAttributes());



            ObjectMapper mapper = new ObjectMapper();

            try {
                Object userListObject = persistentAttributes.get("user_list");
                if (userListObject != null) {
                    // Handle duplicates and append book to list
                    BookList bookList = mapper.readValue(userListObject.toString(), BookList.class);
                    System.out.println("USER_LIST_OBJECT: " + bookList);

                    Map<String, BookDetails> bookDetailsMap = bookList.getBookDetails();
                    if (!bookDetailsMap.containsKey(bookDetails.getBookId())) {
                        bookDetailsMap.put(bookDetails.getBookId(), bookDetails);
                        persistentAttributes.put("user_list",  mapper.writeValueAsString(bookList));
                    } else {
                        return BookSaveStatus.BOOK_EXISTS;
                    }

                } else {
                    // Create a book List
                    BookList bookList = new BookList();

                    // Add the List Name
                    bookList.setListName(listName);
                    Map<String, BookDetails> bookDetailsMap = new HashMap<>();
                    bookDetailsMap.put(bookDetails.getBookId(), bookDetails);
                    // Set Book Details to the List
                    // Add multiple without overwriting
                    // Check if value exists and add to array instead of replacing
                    bookList.setBookDetails(bookDetailsMap);

                    persistentAttributes.put("user_list",  mapper.writeValueAsString(bookList));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return BookSaveStatus.SAVE_ERROR;
            }


            // set persistence attribute
            input.getAttributesManager().setPersistentAttributes(persistentAttributes);

            // Save to DynamoDb
            input.getAttributesManager().savePersistentAttributes();

            // Return that book was saved
            return BookSaveStatus.SAVED_BOOK;
        }
        catch (Exception e) {
            e.printStackTrace();
            Utility.log(input, e.getMessage());
            return BookSaveStatus.SAVE_ERROR;
        }
    }

    public ReadingListResponse getList(String listName, HandlerInput input) throws IOException {
        ReadingListResponse response = new ReadingListResponse();
        Map<String, Object> persistentAttributes = new HashMap<>(input.getAttributesManager().getPersistentAttributes());

        // Is null sometimes so the first command cannot be what's on my list
        Object userListObject = persistentAttributes.get("user_list");
        if (userListObject == null) {
            response.setResponseMessage("Could not find the list");
            response.setSuccessful(false);
            return response;
        }

        System.out.println("USERLISTOBJECT TEST: " + userListObject);
        System.out.println("PERSISTENT ATTRI: " + persistentAttributes);


        ObjectMapper mapper = new ObjectMapper();
        BookList bookList = mapper.readValue(userListObject.toString(), BookList.class);


        Map<String, BookDetails> bookDetails = bookList.getBookDetails();

        String bookNames = "";
        int currBook = 0;
        for (Map.Entry<String, BookDetails> bookEntry: bookDetails.entrySet()) {
            String key = bookEntry.getKey();
            BookDetails book = bookEntry.getValue();
            if (bookDetails.entrySet().size() == 1) {
                bookNames = bookNames + book.getBookName() + " by " + book.getAuthorName();
            }
            else if (currBook == bookDetails.entrySet().size() - 1) {
                bookNames = bookNames + "and " + book.getBookName() + " by " + book.getAuthorName();
            } else {
                bookNames = bookNames + book.getBookName() + " by " + book.getAuthorName() + ", ";
            }
            System.out.println("CURR_BOOK: " + currBook);
            System.out.println("SIZE OF ARRAY: " + bookDetails.entrySet().size());
            currBook++;
        }
        response.setResponseMessage(bookNames);
        response.setSuccessful(true);
        System.out.println("BOOKNAMES: " + bookNames);
        return response;
    }
}

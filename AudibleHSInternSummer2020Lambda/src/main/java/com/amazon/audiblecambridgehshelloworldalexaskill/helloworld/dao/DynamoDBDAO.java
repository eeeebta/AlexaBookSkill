package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.dao;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers.Utility;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookDetails;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookList;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookSaveStatus;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.ReadingListResponse;
import com.amazonaws.services.dynamodbv2.xspec.S;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import javax.management.remote.SubjectDelegationPermission;
import java.io.IOException;
import java.util.*;

public class DynamoDBDAO {

    public BookSaveStatus saveList(String listName, BookDetails bookDetails, HandlerInput input) {
        try {
            // To write book to a list per user

            // Gets existing bookLists/already existing objects
            Map<String, Object> persistentAttributes = new HashMap<>(input.getAttributesManager().getPersistentAttributes());

            ObjectMapper mapper = new ObjectMapper();

            MapType hashMapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, BookList.class);
            try {
                Object userListObject = persistentAttributes.get("user_list");

                // Assumes userListObject is not null even when list does not exist...
                // Did not want to end up using try/catch, but did because (userListObject != null) always ends up being true
                if (userListObject != null) {
                    // Handle duplicates and append book to list
                    // Multiple maps inside of the bigger map
                    // Map of String, BookList
                    // Turn BookList.class to HashMap<String, BookList>.class

                    // Will except here if null
                    HashMap<String, BookList> bookListHashMap = mapper.readValue(userListObject.toString(), hashMapType);

                    // This only works if a list exist, otherwise I need to create one without overwriting the existing HashMap
                    boolean createdNewList = false;
                    Map<String, List<BookDetails>> bookDetailsMap;
                    HashMap<String, BookList> bookListHashMapBefore = bookListHashMap;
                    try {
                        bookDetailsMap = bookListHashMap.get(listName).getBookDetails();
                    } catch (Exception e) {
                        // The error here is that a new list is created with the
                        bookListHashMap = createNewList(listName, bookDetails, bookListHashMap);
                        bookDetailsMap = bookListHashMap.get(listName).getBookDetails();
                        createdNewList = true;
                        e.printStackTrace();
                    }

                    // Could move this above the try/catch to make it cleaner
                    boolean existsAlready = false;
                    for (Map.Entry<String, List<BookDetails>> book: bookListHashMapBefore.get(listName).getBookDetails().entrySet()) {
                        if (book.getKey().equals(bookDetails.getBookId()) && !createdNewList) {
                            // Could return here that the book exists
                            existsAlready = true;
                            break;
                        }
                        System.out.println("BOOK: " + book.getKey() + ", " + book.getValue().get(0) + "OTHERWISE: " + book.toString());
                    }

                    // Check if the book is inside of the lists
                    if (!existsAlready) {
                        bookDetailsMap.put(bookDetails.getBookId(), new ArrayList<>());
                        bookDetailsMap.get(bookDetails.getBookId()).add(bookDetails);
                        persistentAttributes.put("user_list",  mapper.writeValueAsString(bookListHashMap));
                    } else {
                        return BookSaveStatus.BOOK_EXISTS;
                    }

                } else {
                    HashMap<String, BookList> bookListHashMap = new HashMap<>();


                    // Change this to save hashmap of String, book list
                    persistentAttributes.put("user_list",  mapper.writeValueAsString(createNewList(listName, bookDetails, bookListHashMap)));
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

        ObjectMapper mapper = new ObjectMapper();
        MapType hashMapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, BookList.class);
        HashMap<String, BookList> bookListHashMap = mapper.readValue(userListObject.toString(), hashMapType);

        Map<String, List<BookDetails>> bookDetails = bookListHashMap.get(listName).getBookDetails();

        StringBuilder bookNames = new StringBuilder();
        int currBook = 0;
        for (Map.Entry<String, List<BookDetails>> bookEntry: bookDetails.entrySet()) {
            String key = bookEntry.getKey();
            List<BookDetails> books = bookEntry.getValue();
            for (BookDetails book : books) {
                if (bookDetails.entrySet().size() == 1) {
                    bookNames.append(book.getBookName()).append(" by ").append(book.getAuthorName());
                }
                else if (currBook == bookDetails.entrySet().size() - 1) {
                    bookNames.append("and ").append(book.getBookName()).append(" by ").append(book.getAuthorName());
                } else {
                    bookNames.append(book.getBookName()).append(" by ").append(book.getAuthorName()).append(", ");
                }
            }

            currBook++;
        }
        response.setResponseMessage(bookNames.toString());
        response.setSuccessful(true);
        return response;
    }

    public boolean deleteList(String listName, HandlerInput input) {
        // Load in the list/JSON object
        // Look through and delete the key value pair corresponding to

        try {
            Map<String, Object> persistentAttributes = new HashMap<>(input.getAttributesManager().getPersistentAttributes());
            ObjectMapper mapper = new ObjectMapper();
            MapType hashMapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, BookList.class);

            try {
                Object userListObject = persistentAttributes.get("user_list");

                if (userListObject != null) {
                    HashMap<String, BookList> bookListHashMap = mapper.readValue(userListObject.toString(), hashMapType);

                    bookListHashMap.remove(listName);

                    persistentAttributes.put("user_list",  mapper.writeValueAsString(bookListHashMap));

                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            // set persistence attribute
            input.getAttributesManager().setPersistentAttributes(persistentAttributes);

            // Save to DynamoDb
            input.getAttributesManager().savePersistentAttributes();

            // Return that book was saved
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            Utility.log(input, e.getMessage());
            return false;
        }
    }

    private HashMap<String, BookList> createNewList(String listName, BookDetails bookDetails, HashMap<String, BookList> bookListHashMapReturn) {
        // Have to edit this to save to a map as opposed to just this list
        // Create a book List
        BookList bookList = new BookList();


        // Add the List Name
        bookList.setListName(listName);
        Map<String, List<BookDetails>> bookDetailsMap = new HashMap<>();

        // Creates a list and then adds it to that new list, in this case book details is added
        bookDetailsMap.put(bookDetails.getBookId(), Arrays.asList(bookDetails));

        // Set Book Details to the List
        // Add multiple without overwriting
        // Check if value exists and add to array instead of replacing
        bookList.setBookDetails(bookDetailsMap);
        bookListHashMapReturn.put(listName, bookList);

        return bookListHashMapReturn;
    }
}

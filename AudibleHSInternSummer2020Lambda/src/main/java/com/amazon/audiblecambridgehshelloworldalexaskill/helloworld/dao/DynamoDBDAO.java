package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.dao;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers.Utility;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookDetails;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookList;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookSaveStatus;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.ReadingListResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.util.*;

public class DynamoDBDAO {

    public BookSaveStatus saveList(String listName, BookDetails bookDetails, HandlerInput input) {
        try {
            // Gets existing bookLists/already existing objects
            Map<String, Object> persistentAttributes = new HashMap<>(input.getAttributesManager().getPersistentAttributes());

            // Initialize ObjectMapper
            ObjectMapper mapper = new ObjectMapper();

            // Create a MapType to feed in to
            MapType hashMapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, BookList.class);
            try {
                // Return user data
                Object userListObject = persistentAttributes.get("user_list");

                // Check if the userListObject is not null
                if (userListObject != null) {

                    // Convert JSON to HashMap
                    HashMap<String, BookList> bookListHashMap = mapper.readValue(userListObject.toString(), hashMapType);

                    // Initialize bookDetailMap and createdNewList
                    boolean createdNewList = false;
                    Map<String, List<BookDetails>> bookDetailsMap;

                    // Save a previous version of the HashMap
                    HashMap<String, BookList> bookListHashMapBefore = bookListHashMap;

                    // Check if the HashMap contains the listName key
                    if (bookListHashMap.containsKey(listName)) {
                        bookDetailsMap = bookListHashMap.get(listName).getBookDetails();
                    }
                    // Otherwise create a new list with the book in the HashMap and set created new list to true
                    else {
                        bookListHashMap = createNewList(listName, bookDetails, bookListHashMap);
                        bookDetailsMap = bookListHashMap.get(listName).getBookDetails();
                        createdNewList = true;
                    }

                    // Check if the book already exists in the list
                    boolean existsAlready = false;
                    for (Map.Entry<String, List<BookDetails>> book: bookListHashMapBefore.get(listName).getBookDetails().entrySet()) {
                        if (book.getKey().equals(bookDetails.getBookId()) && !createdNewList) {
                            // TODO? Could return here that the book exists
                            existsAlready = true;
                            break;
                        }
                    }

                    // Check if book already exists and if it does just return a BOOK_EXISTS response otherwise
                    if (!existsAlready) {
                        // write the data to the user_list
                        bookDetailsMap.put(bookDetails.getBookId(), new ArrayList<>());
                        bookDetailsMap.get(bookDetails.getBookId()).add(bookDetails);
                        persistentAttributes.put("user_list",  mapper.writeValueAsString(bookListHashMap));
                    } else {
                        return BookSaveStatus.BOOK_EXISTS;
                    }

                } else {
                    // Create a new HashMap since this is the first run
                    HashMap<String, BookList> bookListHashMap = new HashMap<>();

                    // Write new list and book to user_list
                    persistentAttributes.put("user_list",  mapper.writeValueAsString(createNewList(listName, bookDetails, bookListHashMap)));
                }
            } catch (Exception e) {

                // Return a SAVE_ERROR if this excepts
                e.printStackTrace();
                return BookSaveStatus.SAVE_ERROR;
            }

            // Set persistence attribute
            input.getAttributesManager().setPersistentAttributes(persistentAttributes);

            // Save to DynamoDb
            input.getAttributesManager().savePersistentAttributes();

            // Return that book was saved
            return BookSaveStatus.SAVED_BOOK;
        }
        catch (Exception e) {

            // Return SAVE_ERROR if an exception occurs
            e.printStackTrace();
            Utility.log(input, e.getMessage());
            return BookSaveStatus.SAVE_ERROR;
        }
    }


    public ReadingListResponse getList(String listName, HandlerInput input) throws IOException {

        // Initialize response
        ReadingListResponse response = new ReadingListResponse();

        // Retrieve persistentAttributes
        Map<String, Object> persistentAttributes = new HashMap<>(input.getAttributesManager().getPersistentAttributes());

        // Grab the user_list
        Object userListObject = persistentAttributes.get("user_list");

        // If the userListObject is null then return the failed response
        if (userListObject == null) {
            response.setResponseMessage("You have no lists! Create one by asking me to \"Add (book name) to (list name)\"");
            response.setSuccessful(false);
            return response;
        }

        // If userListObject isn't null then move onto converting the object to a HashMap
        ObjectMapper mapper = new ObjectMapper();
        MapType hashMapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, BookList.class);
        HashMap<String, BookList> bookListHashMap = mapper.readValue(userListObject.toString(), hashMapType);

        // Check if the bookListHashMap doesn't contain the list and return the appropriate response
        if (!bookListHashMap.containsKey(listName)) {
            response.setSuccessful(false);
            response.setResponseMessage("That list does not exist. You can ask me \"Add (book name) to (list name)\" " +
                    "and it will automatically create a list for you with that book in it");
            return response;
        }

        // Get bookDetails from the list
        Map<String, List<BookDetails>> bookDetails = bookListHashMap.get(listName).getBookDetails();

        // Make StringBuilder for response and keep count of number of books in the list
        StringBuilder bookNames = new StringBuilder();
        int currBook = 0;

        // Nested loop to get the details of each book that are required
        for (Map.Entry<String, List<BookDetails>> bookEntry: bookDetails.entrySet()) {
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

            // Might be better to use an iteration loop over a foreach loop, but this increments
            // the current position inside of the list
            currBook++;
        }

        // Could also return a "how many books are in the list" response
        response.setResponseMessage(bookNames.toString());
        response.setSuccessful(true);
        return response;
    }

    public String findUserLists(HandlerInput input) {
        // Initialize variables and failure response
        String failedTextResponse = "You don't have any lists. Create them by saying \"Add (your book) to (your list name)\"";
        StringBuilder listTitles = new StringBuilder();

        // Try to proceed with retrieving lists and list names
        try {
            int numOfLists = 0;

            // Grab user_list and user data
            Map<String, Object> persistentAttributes = new HashMap<>(input.getAttributesManager().getPersistentAttributes());
            ObjectMapper mapper = new ObjectMapper();
            MapType hashMapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, BookList.class);
            Object userListObject = persistentAttributes.get("user_list");

            // If it isn't null then convert it and create a titles string with the list names
            if (userListObject != null) {
                HashMap<String, BookList> bookListHashMap = mapper.readValue(userListObject.toString(), hashMapType);

                for (Map.Entry<String, BookList> bookListKeyValPair: bookListHashMap.entrySet()) {
                    if (bookListHashMap.size() == 1) {
                        listTitles.append(bookListKeyValPair.getKey());
                    } else if (numOfLists == bookListHashMap.entrySet().size() - 1) {
                        listTitles.append("and ").append(bookListKeyValPair.getKey());
                    } else {
                        listTitles.append(bookListKeyValPair.getKey()).append(", ");
                    }
                    numOfLists++;
                }
            } else {
                return failedTextResponse;
            }

            // Return formatting of speech
            if (numOfLists < 10 && numOfLists > 1) {
                return String.format("You have %s lists called %s", numToString(numOfLists), listTitles);
            } else if (numOfLists == 1){
                return String.format("You have one list called %s", listTitles);
            } else {
                return String.format("You have %s lists called %s", numOfLists, listTitles);
            }

        } catch (Exception e) {
            // Otherwise return
            e.printStackTrace();
            return failedTextResponse;
        }
    }

    public boolean deleteList(String listName, HandlerInput input) {
        // Try to load everything in
        try {
            // Grab persistentAttributes and initialize mapper and hashMapType
            Map<String, Object> persistentAttributes = new HashMap<>(input.getAttributesManager().getPersistentAttributes());
            ObjectMapper mapper = new ObjectMapper();
            MapType hashMapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, BookList.class);

            try {
                // Grab user list
                Object userListObject = persistentAttributes.get("user_list");

                // Make sure userListObject is not null
                if (userListObject != null) {

                    // Turn user_list into HashMap
                    HashMap<String, BookList> bookListHashMap = mapper.readValue(userListObject.toString(), hashMapType);

                    // Remove the corresponding list
                    bookListHashMap.remove(listName);

                    // Rewrite the object to the DB
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

    // Turn a number from 1-9 into a word
    private String numToString(int num) {
        String[] numLetArray = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};

        return numLetArray[num - 1];
    }
}

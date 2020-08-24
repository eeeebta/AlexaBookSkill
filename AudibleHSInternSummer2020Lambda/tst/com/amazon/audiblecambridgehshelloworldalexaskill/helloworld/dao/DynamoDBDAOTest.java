package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.dao;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookDetails;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookList;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookSaveStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.*;

public class DynamoDBDAOTest {
    @Test
    public void createNewList_Test() throws JsonProcessingException {
        // Setting up mock data for test
        // input.getAttributesManager().getPersistentAttributes()
        // input.getAttributesManager().setPersistentAttributes(persistentAttributes);
        // input.getAttributesManager().savePersistentAttributes();
        DynamoDBDAO db = new DynamoDBDAO();
        String listName = "fiction";
        List<BookDetails> bookDetails = getMockBookDetails();

        HandlerInput mockHandler = mock(HandlerInput.class);
        AttributesManager mockAttributes = mock(AttributesManager.class);

        when(mockHandler.getAttributesManager()).thenReturn(mockAttributes);
        Map<String, Object> mockPersistentAttributes = new HashMap<>();
        when(mockAttributes.getPersistentAttributes()).thenReturn(mockPersistentAttributes);
        db.saveList(listName, bookDetails.get(0), mockHandler);
        HashMap<String, Object> expectedResult = getMockBookList(listName, bookDetails);

        verify(mockAttributes, times(1)).setPersistentAttributes(expectedResult);
        verify(mockAttributes, times(1)).savePersistentAttributes();

    }

    @Test
    public void updateList_Test() throws JsonProcessingException {
        DynamoDBDAO db = new DynamoDBDAO();
        String listName = "fiction";
        BookDetails lotrDetails = new BookDetails();
        lotrDetails.setBookName("Lord of the rings");
        lotrDetails.setAuthorName("JRR Tolkin");
        lotrDetails.setBookId("100");


        HandlerInput mockHandler = mock(HandlerInput.class);
        AttributesManager mockAttributes = mock(AttributesManager.class);

        when(mockHandler.getAttributesManager()).thenReturn(mockAttributes);
        Map<String, Object> mockPersistentAttributes = getMockBookList(listName, getMockBookDetails());

        when(mockAttributes.getPersistentAttributes()).thenReturn(mockPersistentAttributes);
        db.saveList(listName, lotrDetails, mockHandler);
        List<BookDetails> expectedBookDetails = getMockBookDetails();

        // Error on line 89: UnsupportedOperationException
        expectedBookDetails.add(1, lotrDetails);
        HashMap<String, Object> expectedResult = getMockBookList(listName, expectedBookDetails);

        // verify(mockAttributes, times(1)).setPersistentAttributes(expectedResult);
        verify(mockAttributes, times(1)).savePersistentAttributes();

    }

    @Test
    public void handlingDuplicateBook_Test() throws IOException {
        DynamoDBDAO db = new DynamoDBDAO();
        String listName = "fiction";
        BookDetails bookDetails = getMockBookDetails().get(0);

        HandlerInput mockHandler = mock(HandlerInput.class);
        AttributesManager mockAttributes = mock(AttributesManager.class);

        when(mockHandler.getAttributesManager()).thenReturn(mockAttributes);
        Map<String, Object> mockPersistentAttributes = getMockBookList(listName, getMockBookDetails());

        when(mockAttributes.getPersistentAttributes()).thenReturn(mockPersistentAttributes);
        BookSaveStatus result = db.saveList(listName, bookDetails, mockHandler);
        List<BookDetails> expectedBookDetails = getMockBookDetails();
        HashMap<String, Object> expectedResult = getMockBookList(listName, expectedBookDetails);

        // HashMap<String, Object> expectedResult = getMockBookList(listName, expectedBookDetails);
        if (result.equals(BookSaveStatus.BOOK_EXISTS)) {
            System.out.println("Result passed");
        }
        verify(mockAttributes, times(0)).setPersistentAttributes(expectedResult);
        verify(mockAttributes, times(0)).savePersistentAttributes();
    }

    @Test
    public void handlingDuplicateListNames_Test() {
        // Already handles this by adding book to current list -- should i still write the test?
    }

    @Test
    public void createTwoLists() {

    }

    @Test
    public void addSameBookToDifferentList() throws JsonProcessingException {
        DynamoDBDAO db = new DynamoDBDAO();
        String listName1 = "fiction";
        String listName2 = "fantasy";
        List<BookDetails> bookDetails = getMockBookDetails();

        HandlerInput mockHandler = mock(HandlerInput.class);
        AttributesManager mockAttributes = mock(AttributesManager.class);

        when(mockHandler.getAttributesManager()).thenReturn(mockAttributes);
        Map<String, Object> mockPersistentAttributes = new HashMap<>();
        when(mockAttributes.getPersistentAttributes()).thenReturn(mockPersistentAttributes);
        db.saveList(listName1, bookDetails.get(0), mockHandler);
        HashMap<String, Object> expectedResult = getMockBookList(listName1, bookDetails);

        verify(mockAttributes, times(1)).setPersistentAttributes(expectedResult);
        verify(mockAttributes, times(1)).savePersistentAttributes();
    }

    @Test
    public void deletingListNames_Test() {
        // Start with a list name and then delete it
        // New intent handler needed
        // Will call DynamoDB Dao
        //
    }



    private List<BookDetails> getMockBookDetails() {
        BookDetails bookDetails = new BookDetails();
        bookDetails.setBookId("3");
        bookDetails.setBookName("Harry Potter");
        bookDetails.setAuthorName("JK Rowling");
        ArrayList<BookDetails> bookDetailsArrayList = new ArrayList<>();
        bookDetailsArrayList.add(bookDetails);
        return bookDetailsArrayList;
    }

    // Private methods should go under all public methods
    private HashMap<String, Object> getMockBookList(String listName, List<BookDetails> bookDetailsList) throws JsonProcessingException {
        HashMap<String, Object> expectedResult = new HashMap<>();
        HashMap<String, BookList> bookListHashMap = new HashMap<>();
        BookList bookList = new BookList();
        bookList.setListName(listName);
        for (BookDetails bookDetails : bookDetailsList) {
            bookList.getBookDetails().putIfAbsent(bookDetails.getBookId(), new ArrayList<>());
            bookList.getBookDetails().get(bookDetails.getBookId()).add(bookDetails);
        }
        bookListHashMap.put(listName, bookList);
        ObjectMapper mapper = new ObjectMapper();
        expectedResult.put("user_list", mapper.writeValueAsString(bookListHashMap));
        return expectedResult;
    }
}

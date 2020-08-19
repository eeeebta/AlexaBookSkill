package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.dao.DynamoDBDAO;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookDetails;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookSaveStatus;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.repository.GoodReadsAPIRepository;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class AddToListIntentHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AddToListIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        Utility.log(input, "Starting request");
        Utility.logSlots(input);

        Map<String, Slot> slots = Utility.getSlots(input);

        // Get slots
        String bookName = slots.get("BOOK_NAME").getValue();
        String listName = slots.get("LIST_NAME").getValue();

        // Call GoodReadsAPI
        GoodReadsAPIRepository api = new GoodReadsAPIRepository();

        String speechText;
        try {
            BookDetails bookDetails = api.getBookDetails(bookName);

            DynamoDBDAO db = new DynamoDBDAO();
            BookSaveStatus result = db.saveList(listName, bookDetails, input);

            if (result == BookSaveStatus.SAVED_BOOK) {
                speechText = String.format("Saved %s to %s", bookName, listName);
                // String.format("I found a book called %s by %s", bd.getBookName(), bd.getAuthorName());
            } else if (result == BookSaveStatus.BOOK_EXISTS) {
                speechText = String.format("%s is already in %s", bookName, listName);
            }
            else {
                speechText = String.format("Could not save that book to %s", listName);
            }
            Utility.log(input, "Speech text response is " + speechText);
            // response object with a card (shown on devices with a screen) and speech (what alexa says)
            return input.getResponseBuilder()
                    .withSpeech(speechText) // alexa says this
                    .withSimpleCard("HelloWorld", speechText) // alexa will show this on a screen
                    .build();
        } catch (IOException e) {
            Utility.log(input, e.toString());
            throw new RuntimeException(e);
        }



    }


}

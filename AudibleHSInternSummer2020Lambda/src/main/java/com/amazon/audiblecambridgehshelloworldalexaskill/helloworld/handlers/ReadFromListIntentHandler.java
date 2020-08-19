package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.dao.DynamoDBDAO;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.ReadingListResponse;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class ReadFromListIntentHandler implements RequestHandler {
    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("ReadFromListIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        Utility.log(input, "Starting request");
        Utility.logSlots(input);

        Map<String, Slot> slots = Utility.getSlots(input);

        // Get slots
        String listName = slots.get("LIST_NAME").getValue();

        String speechText;

        DynamoDBDAO db = new DynamoDBDAO();

        try {
            ReadingListResponse response = db.getList("place_holder", input);
            if (response.isSuccessful()) {
                speechText = "Your reading list has " + response.getResponseMessage();
            } else {
                speechText = response.getResponseMessage();
            }
        } catch (IOException e) {
            speechText = "There was an error finding the books";
            e.printStackTrace();
        }


        return input.getResponseBuilder()
                .withSpeech(speechText) // alexa says this
                .withSimpleCard("HelloWorld", speechText) // alexa will show this on a screen
                .build();
    }
    // User will give list name
    // Read all the book
    // Call dynamoDBDAO with new
    // Read from user_list

}

package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.dao.DynamoDBDAO;

import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class DeleteListIntentHandler implements RequestHandler {
    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("DeleteListIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        Utility.log(input, "Starting request");
        Utility.logSlots(input);

        Map<String, Slot> slots = Utility.getSlots(input);


        String listName = slots.get("LIST_NAME").getValue();

        String speechText;
        DynamoDBDAO db = new DynamoDBDAO();
        boolean dbResult = db.deleteList(listName, input);
        if (dbResult) {
            speechText = String.format("Successfully deleted %s", listName);
        } else {
            speechText = String.format("Unable to delete %s", listName);
        }

        return input.getResponseBuilder()
                .withSpeech(speechText) // alexa says this
                .withSimpleCard("HelloWorld", speechText) // alexa will show this on a screen
                .build();

    }
}

package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.api.GoodReadsAPI;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookDetails;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.repository.GoodReadsAPIRepository;

import java.io.IOException;
import java.util.*;

import static com.amazon.ask.request.Predicates.intentName;

/**
 * Handles HelloWorldIntent
 */
public class FindBookIntentHandler implements RequestHandler {

    /**
     * Determine if this handler can handle the intent (but doesn't actually handle it)
     *
     * This is called by the ASK framework.
     *
     * @param input
     * @return
     */
    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("FindBookIntent"));
    }

    /**
     * Actually handle the event here.
     *
     * This is called by the ASK framework.
     *
     * @param input
     * @return
     */
    @Override
    public Optional<Response> handle(HandlerInput input) {
        Utility.log(input, "Starting request");
        Utility.logSlots(input);

        Map<String, Slot> slots = Utility.getSlots(input);


        String bookName = slots.get("BOOK_NAME").getValue();

        GoodReadsAPIRepository api = new GoodReadsAPIRepository();

        BookDetails bd = null;
        try {
            bd = api.getBookDetails(bookName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String speechText;

        if (bd != null) {
            speechText = String.format("I found a book called %s by %s", bd.getBookName(), bd.getAuthorName());
        }
        else {
            speechText = "Could not find that book";
        }

        // I found a book by <author> “I found a book called ”$BOOK_NAME“ by $AUTHOR"

        Utility.log(input, "Speech text response is " + speechText);

        // response object with a card (shown on devices with a screen) and speech (what alexa says)
        return input.getResponseBuilder()
                .withSpeech(speechText) // alexa says this
                .withSimpleCard("HelloWorld", speechText) // alexa will show this on a screen
                .build();
    }

}
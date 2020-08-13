package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class FindBookIntentHandlerTest {

    private FindBookIntentHandler handler;

    /**
     * This is called before each test.  Do shared setup work here.
     * @throws Exception
     */
    @Before // this annotation makes this method run before each test
    public void setUp() throws Exception {
        handler = new FindBookIntentHandler();
    }

    /**
     * This is a test.  We run some of our code and then validate that the results are what we expect.
     * This is a great way to test small parts of your code to make sure it behaves how you want
     * as well as a great way to test new changes before you deploy them.
     *
     * This is a basic test that makes sure when we invoke HelloWorldIntent with the city Boston that
     * we get a response that Boston is rainy.
     */
    @Test
    public void handle_CityNameProvided() {
        Response response = invoke("HelloWorldIntent", "CityNameSlot", "Boston");
        String textRespose = response.getOutputSpeech().toString();
        Assert.assertNotNull(textRespose);
        Assert.assertNotEquals(0, textRespose.length());
        Assert.assertTrue(textRespose.contains("The weather in Boston is rainy!"));
    }

    /**
     * This test validates that the hander responds with a different response
     * when no city name is provided.
     */
    @Test
    public void handle_NoCityNameProvided() {
        Response response = invoke("HelloWorldIntent", "", "");
        String textRespose = response.getOutputSpeech().toString();

        Assert.assertNotNull(textRespose);
        Assert.assertNotEquals(0, textRespose.length());
        // TODO fix me
        Assert.assertTrue(textRespose.contains("The weather in Boston is rainy!"));
    }

    /**
     * This method performs a call against your code just like Alexa would.
     * The logic is a bit complicated so we're going to provide it for you.
     *
     * Right now this only supports a single slot.  You may need to expand it to support more!
     *
     * @param intent The intent name (i.e HelloWorldIntent)
     * @param slotKey The slot key (i.e. CityNameSlot)
     * @param slotValue The value of the slot (i.e. "Boston")
     * @return The response value
     */
    public Response invoke(final String intent, final String slotKey, final String slotValue) {
        return handler.handle(HandlerInput.builder()
                .withRequestEnvelope(RequestEnvelope.builder()
                        .withRequest(IntentRequest.builder()
                                .withRequestId(UUID.randomUUID().toString())
                                .withIntent(Intent.builder()
                                        .withName(intent)
                                        .putSlotsItem(slotKey, Slot.builder()
                                                .withName(slotKey)
                                                .withValue(slotValue)
                                                .build())
                                        .build()).build())
                        .build()).build()).get();
    }
}
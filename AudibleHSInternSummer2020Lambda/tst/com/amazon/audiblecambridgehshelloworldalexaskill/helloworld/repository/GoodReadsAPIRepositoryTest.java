package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.repository;

import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.api.GoodReadsAPI;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookDetails;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Before;
import org.junit.Test;

import java.awt.print.Book;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class GoodReadsAPIRepositoryTest {
    AWSSimpleSystemsManagement mockSystemManager;

    @Before
    public void setup() {

        // Creating mockSystemManager with properties that will allow it to run
        GetParameterRequest keyRequest = new GetParameterRequest();
        keyRequest.withName("GOOD_READS_KEY");
        mockSystemManager = mock(AWSSimpleSystemsManagement.class);

        Parameter mockParam = mock(Parameter.class);
        GetParameterResult mockResult = mock(GetParameterResult.class);
        mockResult.setParameter(mockParam);

        when(mockResult.getParameter()).thenReturn(mockParam);
        when(mockSystemManager.getParameter(keyRequest)).thenReturn(mockResult);

        // Key should go here
        when(mockParam.getValue()).thenReturn("--");
    }

    @Test
    public void test_api() {

        GoodReadsAPIRepository repo = new GoodReadsAPIRepository(mockSystemManager);
        try {
            // Get book details for Harry Potter and the Sorcerer's Stone from the getBookDetails method
            BookDetails xml = repo.getBookDetails("Harry Potter and the Sorcerer's Stone");

            // Print out the returned XML
            System.out.println(xml.toString());
        }
        catch (Exception e) {
            // If there is an exception print it out
            System.out.println(e.toString());
        }

    }

    private String getXml() {
        return "<PhoneDetails>\n" +
                "  <internal_memory>6/64 GB</internal_memory>\n" +
                "  <display_size>6.4</display_size>\n" +
                "  <phone_name>OnePlus</phone_name>\n" +
                "</PhoneDetails>";
    }

}

class PhoneDetails {

    @JsonProperty("phone_name")
    private String name;

    @JsonProperty("display_size")
    private String displaySize;

    @JsonProperty("internal_memory")
    private String memory;

    // rest of the code remains as is
}

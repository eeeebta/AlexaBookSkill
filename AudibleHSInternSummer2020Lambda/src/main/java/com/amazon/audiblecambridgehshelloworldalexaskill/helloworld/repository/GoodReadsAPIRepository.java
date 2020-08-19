package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.repository;

import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookDetails;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
// import org.json.*;

import java.awt.print.Book;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GoodReadsAPIRepository {
    private AWSSimpleSystemsManagement systemManagerClient;

    public GoodReadsAPIRepository() {
        this.systemManagerClient = AWSSimpleSystemsManagementClientBuilder.defaultClient();
    }

    public GoodReadsAPIRepository(AWSSimpleSystemsManagement systemManagerClient) {
        this.systemManagerClient = systemManagerClient;
    }

    private String getGoodReadsURL(String bookNameInput) {
        // Setup to get Good Reads key
        GetParameterRequest keyRequest = new GetParameterRequest();
        keyRequest.withName("GOOD_READS_KEY");

        // Finally call Systems Manager to get the tokens.

        GetParameterResult resultParameter = systemManagerClient.getParameter(keyRequest);
        Parameter parameter = resultParameter.getParameter();
        String key = parameter.getValue();

        try {
             return "https://www.goodreads.com/search.xml?key=" + key + "&q=" + URLEncoder.encode(bookNameInput, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.toString());
            throw new RuntimeException(e);
        }
    }

    public BookDetails getBookDetails(String bookNameInput) throws IOException {
        String goodReadsURL = getGoodReadsURL(bookNameInput);

        // Grab the url and open a connection with the request method of get
        URL url = new URL(goodReadsURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Parse the response/save the response
        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        // Close and disconnect
        in.close();
        conn.disconnect();

        //
        List<String> returnedJsonObjectList = convertToJson(content.toString());

        BookDetails returnBook = new BookDetails();
        returnBook.setBookName(returnedJsonObjectList.get(0));
        returnBook.setAuthorName(returnedJsonObjectList.get(1));
        returnBook.setBookId(returnedJsonObjectList.get(2));

        return returnBook;
    }

    // Convert XML to JSON and then return a list of useful book data
    public List<String> convertToJson(String xml) {
        String jsonPPS = "";
        List<String> bookDetails = new ArrayList<>();
        try {
            // Convert XML to JSON
            JSONObject json = XML.toJSONObject(xml);

            // Convert JSON to String
            jsonPPS = json.toString();

            // Print
            System.out.println(jsonPPS);

            // Filter and grab the parts of the JSON object that are required
            JSONObject bestBook = json.getJSONObject("GoodreadsResponse").getJSONObject("search").getJSONObject("results").getJSONArray("work").optJSONObject(0).getJSONObject("best_book");
            String title = bestBook.getString("title");
            String author = bestBook.getJSONObject("author").getString("name");
            String bestBookId = bestBook.getJSONObject("id").toString();
            bestBookId = bestBookId.substring(28, bestBookId.length() - 1);

            // Print
            System.out.println("BEST BOOK: " + bestBook);
            System.out.println("TITLE " + title);
            System.out.println("AUTHOR: " + author);
            System.out.println("ID: " + bestBookId);

            // Add them to the list
            bookDetails.add(title);
            bookDetails.add(author);
            bookDetails.add(bestBookId);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
        return bookDetails;
    }

}

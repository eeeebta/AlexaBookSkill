package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.repository;

import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.GoodreadsResponse;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;

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

    public String getBookDetails(String bookNameInput) throws IOException {
        String goodReadsURL = getGoodReadsURL(bookNameInput);

        URL url = new URL(goodReadsURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        conn.disconnect();
        ObjectMapper xmlMapper = new XmlMapper();
        GoodreadsResponse deserializedData = xmlMapper.readValue(content.toString(), GoodreadsResponse.class);
        // System.out.println();
        deserializedData.getBestBook();

        return content.toString();
    }

}

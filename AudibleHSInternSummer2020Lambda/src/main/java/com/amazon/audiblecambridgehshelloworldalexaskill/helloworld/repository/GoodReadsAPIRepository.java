package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.repository;

import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.GoodreadsResponse;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
// import org.json.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
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

    public String getBookDetails(String bookNameInput) throws IOException, ParserConfigurationException, SAXException {
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
//         ObjectMapper xmlMapper = new XmlMapper();
//         GoodreadsResponse deserializedData = xmlMapper.readValue(content.toString(), GoodreadsResponse.class);

//         String contentString = content.toString();

        String returnedString = convertToJson(content.toString());

        System.out.println(returnedString);

        return "contentString";
    }

//    public String extractBestBookTag(String url) throws IOException, SAXException, ParserConfigurationException {
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        DocumentBuilder db = dbf.newDocumentBuilder();
//        Document doc = db.parse(new URL(url).openStream());
//
//        System.out.println("root of xml file" + doc.getDocumentElement().getNodeName());
//        NodeList goodReadsResponse = doc.getElementsByTagName("GoodreadsResponse");
//
//        return "";
//    }

    public String convertToJson(String xml) {
        String jsonPPS = "";
        try {
            JSONObject json = XML.toJSONObject(xml);
            jsonPPS = json.toString();
            System.out.println(jsonPPS);
            JSONObject bestBook = json.getJSONObject("GoodreadsResponse").getJSONObject("search").getJSONObject("results").getJSONArray("work").optJSONObject(0).getJSONObject("best_book");
            bestBook.getString("");
            System.out.println(bestBook);
        } catch (JSONException je) {
            System.out.println(je.toString());
        }
        return jsonPPS;
    }

}

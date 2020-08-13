package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.api;

import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model.BookDetails;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class GoodReadsAPI {
    private Map<String, BookDetails> bookDetailMap;

    public GoodReadsAPI() {
        this.bookDetailMap = new HashMap<>();
        setMockBookDetails();
    }

    // Take input of book name
    // Output: book details -- could be string


    public Optional<BookDetails> getBookDetailsByBookName(String bookName) {
        if (this.bookDetailMap.containsKey(bookName)) {
            return Optional.of(this.bookDetailMap.get(bookName));
        }

        return Optional.empty();
    }

    private void setMockBookDetails() {
        List<String> bookList = Arrays.asList(new String []{"harry potter", "lord of the rings"});
        List<String> authorList = Arrays.asList(new String []{"jk rowling", "rr tolkin"});
        for (int i = 0; i < bookList.size(); i++) {
            BookDetails bd = new BookDetails();
            bd.setBookName(bookList.get(i));
            bd.setAuthorName(authorList.get(i));
            bookDetailMap.put(bd.getBookName(), bd);
        }

    }

    public void callGoodReadsAPI() throws IOException {
        URL url = new URL("https://www.goodreads.com/search/index.xml?key=&q=Ender%27s+Game");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        System.out.println(content);
    }

    public static void main(String[] args) throws IOException {
        GoodReadsAPI api = new GoodReadsAPI();
        api.callGoodReadsAPI();
    }
}

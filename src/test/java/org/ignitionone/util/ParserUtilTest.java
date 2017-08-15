package org.ignitionone.util;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;


public class ParserUtilTest {

    @Test
    public void testSanitizeUrl() {
        // sanitizeUrl method is supposed to return a valid url given a
        // url- Should append www, check if url is in proper format etc.

        String url1 = "www.example.co.uk";
        String url2 = "www.example.com";
        String url3 = "http://example.co.uk";
        String url4 = "e232xample.co.in";
        String url5 = "e232xample.co.uk.in";
        String url6 = "e232xample.com";
        String url7 = "example";

        assertEquals("www.example.co.uk", ParserUtil.sanitizeUrl(url1));
        assertEquals("www.example.com", ParserUtil.sanitizeUrl(url2));
        assertEquals("http://example.co.uk", ParserUtil.sanitizeUrl(url3));
        assertEquals("www.e232xample.co.in", ParserUtil.sanitizeUrl(url4));
        assertEquals("www.e232xample.co.uk.in", ParserUtil.sanitizeUrl(url5));
        assertEquals("www.e232xample.com", ParserUtil.sanitizeUrl(url6));
        assertEquals(null, ParserUtil.sanitizeUrl(url7));
    }

    @Test
    public void testIsHtml() {
        String htmlString = "<!DOCTYPE html> <html><head>This is test html tag<head></html>";
        String plainText = "This is normal text. Non-html!!";

        assertEquals(true, ParserUtil.isHtml(htmlString));
        assertEquals(false, ParserUtil.isHtml(plainText));
    }

    @Test
    public void testIsAdtxt() {
        String adsTxtString = "\"#Ads.txt economist.com\\n\" +\n" +
                "\"\\n\" +\n" +
                "\"google.com, pub-9789600135996590, DIRECT\\n\" +\n" +
                "\"indexexchange.com, 184475, DIRECT\\n\" +\n" +
                "\"rubiconproject.com, 11914, DIRECT\"";
        String plainText = "This is normal text. Non-Adtxt!!";

        String comment = "#This is a comment";


        System.out.println(ParserUtil.isAdsTXT(comment));

        assertEquals(true, ParserUtil.isAdsTXT(adsTxtString));
        assertEquals(false, ParserUtil.isAdsTXT(plainText));
    }

    @Test
    public void testFilterValidUrls() {
        Set<String> testUrls = new HashSet<>();

        testUrls.add("www.example.com");
        testUrls.add("www.example.com");
        testUrls.add("www.example.com");
        testUrls.add("www.example.com");
        testUrls.add("example");
        testUrls.add("testurl.com");
        testUrls.add("http://example.com");
        testUrls.add("https://examplehttps.com");

        Set<String> sanitizedUrls = ParserUtil.filterValidUrls(testUrls);
        assertEquals(4, sanitizedUrls.size());
    }
}

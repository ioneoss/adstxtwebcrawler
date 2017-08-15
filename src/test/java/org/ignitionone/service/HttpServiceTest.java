package org.ignitionone.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.ignitionone.model.HttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HttpServiceTest {
    protected static final int TEST_PORT = 8089;
    private HttpService httpService;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(TEST_PORT);

    @Before
    public void setup() {
        Properties properties = new Properties();
        properties.setProperty("parallec.http.parallelism", "500");
        properties.setProperty("parallec.http.port", String.valueOf(TEST_PORT));
        httpService = new HttpService(properties);
    }

    @Test
    public void testExecuteHttpRequestsInvalidWithHtml() {
        String content = "<html><body>Some content</body></html>";

        stubFor(get(urlEqualTo("/ads.txt"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(content)));

        List<String> targets = Arrays.asList("localhost");
        List<HttpResponse> responses = httpService.executeHttpRequests(targets);
        assertEquals(0, responses.size());
    }

    @Test
    public void testExecuteHttpRequestsInvalidMalformedAdsTxt() {
        String content =  "\n" +
                "google.com, pub-9789600135996590, DIRECT\n" +
                "indexexchange.com, 184475, DIRECT\n" +
                "rubiconproject.com, 11914, DIRECT";

        stubFor(get(urlEqualTo("/ads.txt"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withStatus(200)
                        .withBody(content)));

        List<String> targets = Arrays.asList("localhost");
        List<HttpResponse> responses = httpService.executeHttpRequests(targets);
        assertEquals(1, responses.size());
        assertFalse(responses.get(0).hasAdsTxtData());
        assertEquals(200, responses.get(0).getResponseCode());
        assertEquals("localhost", responses.get(0).getDomainName());
        assertEquals(content, responses.get(0).getResponseContent());
    }

    @Test
    public void testExecuteHttpRequestsValid() {
        String content = "#Ads.txt economist.com\n" +
                "\n" +
                "google.com, pub-9789600135996590, DIRECT\n" +
                "indexexchange.com, 184475, DIRECT\n" +
                "rubiconproject.com, 11914, DIRECT";

        stubFor(get(urlEqualTo("/ads.txt"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withStatus(200)
                        .withBody(content)));

        List<String> targets = Arrays.asList("localhost");
        List<HttpResponse> responses = httpService.executeHttpRequests(targets);
        assertEquals(1, responses.size());
        assertTrue(responses.get(0).hasAdsTxtData());
        assertEquals(200, responses.get(0).getResponseCode());
        assertEquals("localhost", responses.get(0).getDomainName());
        assertEquals(content, responses.get(0).getResponseContent());
    }

    @Test
    public void testExecuteHttpRequestsRedirect() {

        stubFor(get(urlEqualTo("/adsredirect.txt"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("#Ads.txt economist.com\n" +
                                "\n" +
                                "google.com, pub-9789600135996590, DIRECT\n" +
                                "indexexchange.com, 184475, DIRECT\n" +
                                "rubiconproject.com, 11914, DIRECT")));

        stubFor(get(urlEqualTo("/ads.txt"))
                .willReturn(aResponse()
                        .withHeader("Localhost", "http://localhost:8089/adsredirect.txt")
                        .withStatus(301)
                        .withBody("<html>\n" +
                                "<head><title>301 Moved Permanently</title></head>\n" +
                                "<body bgcolor=\"white\">\n" +
                                "<center><h1>301 Moved Permanently</h1></center>\n" +
                                "<hr><center>nginx</center>\n" +
                                "</body>\n" +
                                "</html>")
                ));

        List<String> targets = Arrays.asList("localhost");
        List<HttpResponse> responses = httpService.executeHttpRequests(targets);

        assertEquals(0, responses.size());
    }
}
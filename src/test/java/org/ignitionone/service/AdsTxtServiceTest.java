package org.ignitionone.service;

import org.ignitionone.model.AdsTxtRecord;
import org.ignitionone.model.HttpResponse;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class AdsTxtServiceTest {
    private AdsTxtService adsTxtService;

    @Before
    public void setup() {
        Properties properties = new Properties();
        properties.setProperty("datastores.write", "console");
        properties.setProperty("datastores.read", "console");

        adsTxtService = new AdsTxtService(properties);
    }

    @Test
    public void testProcessHttpResponses() {
        List<AdsTxtRecord> adsTxtRecords;
        List<HttpResponse> httpResponses = new ArrayList<>();

        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setDomainName("www.businessinsider.com");
        httpResponse.setHasAdsTxt(true);
        httpResponse.setResponseCode(200);
        httpResponse.setResponseContent("#Ads.txt economist.com\n" +
                "\n" +
                "google.com, pub-9789600135996590, DIRECT, d75815a79\n" +
                "indexexchange.com, 184475, DIRECT\n" +
                "rubiconproject.com, 11914, RESELLER");

        httpResponses.add(httpResponse);
        adsTxtRecords = adsTxtService.processHttpResponses(httpResponses);

        assertEquals(3, adsTxtRecords.size());
        assertEquals("www.businessinsider.com", adsTxtRecords.get(0).getSourceDomain());
        assertEquals("google.com", adsTxtRecords.get(0).getAdServingDomain());
        assertEquals("pub-9789600135996590", adsTxtRecords.get(0).getPublisherAccId());
        assertEquals("DIRECT", adsTxtRecords.get(0).getAccType());
        assertEquals("d75815a79", adsTxtRecords.get(0).getCertAuthId());
        assertEquals(new LocalDate(), adsTxtRecords.get(0).getInsertDate().toLocalDate());

        assertEquals("www.businessinsider.com", adsTxtRecords.get(1).getSourceDomain());
        assertEquals("indexexchange.com", adsTxtRecords.get(1).getAdServingDomain());
        assertEquals("184475", adsTxtRecords.get(1).getPublisherAccId());
        assertEquals("DIRECT", adsTxtRecords.get(1).getAccType());
        assertEquals("", adsTxtRecords.get(1).getCertAuthId());
        assertEquals(new LocalDate(), adsTxtRecords.get(1).getInsertDate().toLocalDate());

        assertEquals("www.businessinsider.com", adsTxtRecords.get(2).getSourceDomain());
        assertEquals("rubiconproject.com", adsTxtRecords.get(2).getAdServingDomain());
        assertEquals("11914", adsTxtRecords.get(2).getPublisherAccId());
        assertEquals("RESELLER", adsTxtRecords.get(2).getAccType());
        assertEquals("", adsTxtRecords.get(2).getCertAuthId());
        assertEquals(new LocalDate(), adsTxtRecords.get(2).getInsertDate().toLocalDate());
    }

    @Test
    public void testCommentRemoval() {
        String lineWithComment = "This, is a ,line with #comment";
        String noPrior = "#";
        assertEquals("This, is a ,line with", adsTxtService.removeComments(lineWithComment));
        assertEquals("", adsTxtService.removeComments(""));
        assertEquals(null, adsTxtService.removeComments(null));

        adsTxtService.removeComments(noPrior);
    }
}



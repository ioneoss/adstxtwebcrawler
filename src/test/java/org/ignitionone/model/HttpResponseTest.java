package org.ignitionone.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HttpResponseTest {

    @Test
    public void testToString() {
        HttpResponse httpResponse = new HttpResponse();
        String toStringValue = httpResponse.toString();
        String expectedToStringValue =
            "StatusCode: 0\n" +
            "HasAdsTxt : false\n" +
            "DomainName: null\n" +
            "ResponseContent: null";
        assertEquals(expectedToStringValue, toStringValue);
    }
}

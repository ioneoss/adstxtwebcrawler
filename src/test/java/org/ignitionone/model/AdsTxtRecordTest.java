package org.ignitionone.model;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AdsTxtRecordTest {

    @Test
    public void testToString() {
        AdsTxtRecord adsTxtRecord = new AdsTxtRecord();
        String toStringValue = adsTxtRecord.toString();
        String expectedToStringValue = ",null,null,null,null,null\n";

        assertTrue(toStringValue.contains(expectedToStringValue));
    }
}

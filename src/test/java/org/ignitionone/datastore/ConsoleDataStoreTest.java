package org.ignitionone.datastore;

import org.ignitionone.model.AdsTxtRecord;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConsoleDataStoreTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private ConsoleDataStore consoleDataStore;

    @Before
    public void setup() {
        consoleDataStore = new ConsoleDataStore();
    }
    @Test(expected = UnsupportedOperationException.class)
    public void testGetAdsTxtUrls() throws SQLException {
        consoleDataStore.getAdsTxtUrls();
    }

    @Test
    public void testInsertAdsTxtRecords() throws IOException {
        List<AdsTxtRecord> adsTxtRecords = new ArrayList<>();
        AdsTxtRecord adsTxtRecord = new AdsTxtRecord();
        adsTxtRecord.setCertAuthId("CertAuthId");
        adsTxtRecord.setAccType("AccType");
        adsTxtRecord.setAdServingDomain("AdServingDomain");
        adsTxtRecord.setInsertDate(new DateTime());
        adsTxtRecord.setPublisherAccId("PublisherAccId");
        adsTxtRecord.setSourceDomain("SourceDomain");
        adsTxtRecords.add(adsTxtRecord);
        consoleDataStore.insertAdsTxtRecords(adsTxtRecords);
        // Nothing to assert
    }
}

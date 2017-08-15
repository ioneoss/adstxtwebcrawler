package org.ignitionone.datastore;

import org.ignitionone.model.AdsTxtRecord;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileDataStoreTest {
    private Properties config;
    private FileDataStore fileDataStore;
    private String  inputFilePath;
    private String  outputFilePath;

    @Before
    public void setup() {

        inputFilePath = (new File(this.getClass().getResource("/inputfile.csv").getFile())).getAbsolutePath();
        outputFilePath = inputFilePath.replace("inputfile.csv", "files/outputfile.csv");
        config = new Properties();
        config.setProperty("filestore.inputfilename", inputFilePath);
        config.setProperty("filestore.outputfilename", outputFilePath);
        fileDataStore = new FileDataStore(config);
    }

    @After
    public void teardown() throws IOException {
        Files.deleteIfExists(new File(outputFilePath).toPath());
    }

    @Test
    public void testGetAdsTxtUrls() throws SQLException {
        Set<String> urls = fileDataStore.getAdsTxtUrls();

        assertEquals(2, urls.size());
        assertTrue(urls.contains("ignitionone.com"));
        assertTrue(urls.contains("www.ignitionone.com"));
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
        fileDataStore.insertAdsTxtRecords(adsTxtRecords);

        byte[] encoded = Files.readAllBytes(new File(outputFilePath).toPath());
        String outputFile = new String(encoded, StandardCharsets.UTF_8);
        assertTrue(outputFile.contains("SourceDomain,AdServingDomain,PublisherAccId,AccType,CertAuthId"));
    }
}

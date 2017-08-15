package org.ignitionone.datastore;

import org.ignitionone.configuration.ConfigBuilder;
import org.ignitionone.model.AdsTxtRecord;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Sql2o;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class JDBCDataStoreTest {
    private static final String JDBC_URL = "jdbc:derby:memory:adstxt";
    private static final String JDBC_USERNAME = "username";
    private static final String JDBC_PASSWORD = "password";
    private static final String JDBC_INSERTQUERY = "INSERT INTO adstxt_results" +
            "(insert_date, source_domain, adserving_domain, publisher_acc_id, " +
            "acc_type, cert_auth_id) VALUES (:insertDate, :sourceDomain, " +
            ":adServingDomain, :publisherAccId, :accType, :certAuthId)";
    private static final String ADSTXT_URL = "localhost";
    private static final String CREATE_ADS_TXT_URL_TABLE =
            "CREATE TABLE adstxt_urls ("
                    + "url VARCHAR(100))";
    private static final String CREATE_ADS_TXT_RESULTS_TABLE =
            "CREATE TABLE adstxt_results ("
                    + "insert_date TIMESTAMP, "
                    + "source_domain VARCHAR(100), "
                    + "adserving_domain VARCHAR(100), "
                    + "publisher_acc_id VARCHAR(100), "
                    + "acc_type VARCHAR(100), "
                    + "cert_auth_id VARCHAR(100))";
    private static final String insertRow =
            "INSERT INTO adstxt_urls (url)"
                    + " VALUES ( '" + ADSTXT_URL + "')";


    private Properties config;
    private JDBCDataStore jdbcDataStore;

    @Before
    public void setup() throws SQLException {
        Connection conn = DriverManager.getConnection(String.format("%s;user=%s;password=%s;create=true;", JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD));
        conn.createStatement().execute(CREATE_ADS_TXT_URL_TABLE);
        conn.createStatement().execute(CREATE_ADS_TXT_RESULTS_TABLE);
        conn.createStatement().executeUpdate(insertRow);
        conn.close();

        ConfigBuilder.clearConfig();
        config = ConfigBuilder.getConfig();
        config.setProperty("jdbc.postgres.username", JDBC_USERNAME);
        config.setProperty("jdbc.postgres.password", JDBC_PASSWORD);
        config.setProperty("jdbc.postgres.url", JDBC_URL);
        config.setProperty("jdbc.postgres.insertquery", JDBC_INSERTQUERY);

        jdbcDataStore = new JDBCDataStore(config, "postgres");
    }

    @After
    public void teardown() throws SQLException {
        Connection conn = DriverManager.getConnection(JDBC_URL + ";");
        conn.createStatement().execute(String.format("DROP TABLE %s.adstxt_urls", JDBC_USERNAME));
        conn.createStatement().execute(String.format("DROP TABLE %s.adstxt_results", JDBC_USERNAME));
        conn.close();
    }

    @Test
    public void testGetAdsTxtUrls() throws SQLException {
        Set<String> urls = jdbcDataStore.getAdsTxtUrls();
        assertEquals(1, urls.size());
        assertEquals(ADSTXT_URL, urls.iterator().next());
    }

    @Test
    public void testInsertAdsTxtRecords() throws SQLException {
        List<AdsTxtRecord> adsTxtRecords = new ArrayList<>();
        AdsTxtRecord adsTxtRecord = new AdsTxtRecord();
        adsTxtRecord.setInsertDate(new DateTime());
        adsTxtRecord.setCertAuthId("CertAuthId");
        adsTxtRecord.setAccType("AccType");
        adsTxtRecord.setAdServingDomain("AdServingDomain");
        adsTxtRecord.setPublisherAccId("PublisherAccId");
        adsTxtRecord.setSourceDomain("SourceDomain");
        adsTxtRecords.add(adsTxtRecord);
        jdbcDataStore.insertAdsTxtRecords(adsTxtRecords);

        List<String> results;
        try (org.sql2o.Connection con = new Sql2o(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD).open()) {
            results = con.createQuery("SELECT source_domain FROM adstxt_results").executeAndFetch(String.class);
        }
        assertEquals(1, results.size());
        assertEquals("SourceDomain", results.get(0));
    }
}

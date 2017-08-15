/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * @author Shridhar Manvi <Shridhar.Manvi AT ignitionone DOT com>
 * @author Roderick Rodriguez <Roderick.Rodriguez AT ignitionone DOT com>
 */
package org.ignitionone.service;

import com.google.common.annotations.VisibleForTesting;
import org.ignitionone.datastore.core.DataStoreFactory;
import org.ignitionone.datastore.core.DataStoreType;
import org.ignitionone.datastore.core.ReadDataStore;
import org.ignitionone.datastore.core.WriteDataStore;
import org.ignitionone.model.AdsTxtRecord;
import org.ignitionone.model.HttpResponse;
import org.ignitionone.util.ParserUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class AdsTxtService {
    private static final Logger LOG = LoggerFactory.getLogger(AdsTxtService.class);
    private static final String NEWLINE = "\n";
    private final Properties config;
    private final List<ReadDataStore> readDataStores;
    private final List<WriteDataStore> writeDataStores;


    public AdsTxtService(Properties config) {
        this.config = config;

        // Initialize data stores
        this.writeDataStores = new LinkedList<>();
        this.readDataStores = new LinkedList<>();

        initializeStores(config);
    }

    /**
     * <p>
     * This method iterates through the list of stores listed in the config and initializes them.
     * The program aborts if no stores are found in the config.
     * </p>
     * <p>
     * List stores as a comma separated property in this format:
     * <br>
     * datastores.read=store1,store2...store-n
     * datastores.write=store1,store2...store-n
     * </p>
     *
     * @param config
     */
    private void initializeStores(Properties config) {
        // Initialize all read stores
        List<String> readStores = Arrays.asList(config.getProperty("datastores.read").split(","));
        readStores.forEach(store ->
                readDataStores.add(DataStoreFactory.getReadDataStore(DataStoreType.getValueOf(store))));

        // Initialize all write stores
        List<String> writeStores = Arrays.asList(config.getProperty("datastores.write").split(","));
        writeStores.forEach(store -> writeDataStores.add(
                DataStoreFactory.getWriteDataStore(DataStoreType.getValueOf(store))));
    }

    /**
     * Responsible for the orchestration of different components of the app.
     */
    public void executionDirector() {
        try {

            HttpService httpService = new HttpService(config);

            // Get Urls from all stores
            Set<String> urlsFromAllStores = new HashSet<>();
            this.readDataStores.forEach(readDataStore ->
                    urlsFromAllStores.addAll(readDataStore.getAdsTxtUrls())
            );

            // Validate Urls obtained from all stores
            Set<String> validUrls = ParserUtil.filterValidUrls(urlsFromAllStores);
            LOG.debug("Total valid unique urls: {}", validUrls.size());

            // Execute http requests on these valid urls/domains
            List<HttpResponse> responses = httpService.executeHttpRequests(
                    validUrls.stream()
                            .collect(Collectors.toList()));

            // Process http responses
            LOG.info("Beginning processing of http responses!");
            List<AdsTxtRecord> adsTxtRecords = processHttpResponses(responses);


            // Write valid responses to all endpoints
            LOG.debug("Starting data insertion of {} adsTxtRecords", adsTxtRecords.size());
            writeDataStores.forEach(writeDataStore -> writeDataStore.insertAdsTxtRecords(adsTxtRecords));

        } catch (Exception e) {
            LOG.error("Exception occured: {}", e);
            System.exit(100);
        }

    }

    /**
     * <p> Responsible for processing and retaining valid responses </p>
     *
     * @param httpResponses
     * @return
     */
    @VisibleForTesting
    public List<AdsTxtRecord> processHttpResponses(List<HttpResponse> httpResponses) {
        List<AdsTxtRecord> adsTxtRecords = new ArrayList<>();

        httpResponses
                .forEach(httpResponse -> {
                    if (httpResponse.hasAdsTxtData()) {
                        List<String> adsTxtRecordRows = Arrays.asList(httpResponse.getResponseContent().split(NEWLINE));
                        String domain = httpResponse.getDomainName();

                        for (String adsTxtRecordRow : adsTxtRecordRows) {
                            adsTxtRecordRow = adsTxtRecordRow.trim();
                            if (!adsTxtRecordRow.startsWith("#") && !adsTxtRecordRow.isEmpty()) {
                                AdsTxtRecord adsTxtRecord = adsTxtRecordBuilder(adsTxtRecordRow, domain);
                                if (adsTxtRecord != null) {
                                    adsTxtRecords.add(adsTxtRecord);
                                }
                            }
                        }
                    } else
                        LOG.debug("\n===========************\n" +
                                "Non adstxt 200 response: \n {} \n", httpResponse.toString() +
                                "===========************\n");
                });

        return adsTxtRecords;
    }

    /**
     * <p> This method builds the adsTxt objects from http response content </p>
     *
     * @param adsTxtRecordRow
     * @param domain
     * @return
     */

    private AdsTxtRecord adsTxtRecordBuilder(String adsTxtRecordRow, String domain) {

        List<String> adsTxtRecordRowColumns = Arrays.asList(adsTxtRecordRow.split(("\\s*,\\s*")));
        AdsTxtRecord adsTxtRecord = new AdsTxtRecord();

        try {
            if (adsTxtRecordRowColumns.size() > 0) {
                adsTxtRecord.setInsertDate(new DateTime());
                adsTxtRecord.setSourceDomain(removeComments(domain));
                adsTxtRecord.setAdServingDomain(removeComments(adsTxtRecordRowColumns.get(0)));
                adsTxtRecord.setPublisherAccId(removeComments(adsTxtRecordRowColumns.get(1)));
                adsTxtRecord.setAccType(removeComments(adsTxtRecordRowColumns.get(2)));

                // 4th column is optional
                if (adsTxtRecordRowColumns.size() == 4) {
                    adsTxtRecord.setCertAuthId(adsTxtRecordRowColumns.get(3));
                } else {
                    adsTxtRecord.setCertAuthId("");
                }
            }
        } catch (Exception e) {
            LOG.error("Error parsing adstxt data! Invalid adsTxtRecordRow for domain: {}", domain);
        }
        return adsTxtRecord;
    }

    @VisibleForTesting
    public String removeComments(String column) {
        if (StringUtils.isNotBlank(column) && !StringUtils.startsWith(column, "#")) {
            if (column.contains("#")) {
                return column.split("#")[0].trim();
            }
        }
        return column;
    }
}

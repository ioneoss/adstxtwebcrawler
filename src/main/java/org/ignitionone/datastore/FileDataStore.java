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
package org.ignitionone.datastore;

import org.ignitionone.datastore.core.DataStore;
import org.ignitionone.model.AdsTxtRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class FileDataStore implements DataStore {
    private static String READFILE;
    private static String WRITEFILE;

    private static Logger LOG = LoggerFactory.getLogger(FileDataStore.class);

    public FileDataStore(Properties properties) {
        // Read config etc.
        READFILE = properties.getProperty("filestore.inputfilename", "");
        WRITEFILE = properties.getProperty("filestore.outputfilename", "");

        LOG.debug("Initializing file data store...");
    }

    @Override
    public void insertAdsTxtRecords(List<AdsTxtRecord> adsTxtRecords) {

        try (FileWriter fileWriter = new FileWriter(WRITEFILE)) {
            adsTxtRecords.forEach(adsTxtRecord ->
                    writeLine(fileWriter, adsTxtRecord));
        } catch (IOException e) {
            LOG.error("Error writing line: ", e);
        }
    }

    @Override
    public Set<String> getAdsTxtUrls() {
        Set<String> urls = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(READFILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                line = line.replaceAll("\n", "");
                urls.add(line);
            }
        } catch (Exception e) {
            LOG.error("Error reading file: ", e);
        }
        return urls;
    }

    private void writeLine(FileWriter fileWriter, AdsTxtRecord adsTxtRecord) {
        try {
            fileWriter.write(adsTxtRecord.toString());
        } catch (IOException e) {
            LOG.error("Error wring line: ", e);
        }
    }
}

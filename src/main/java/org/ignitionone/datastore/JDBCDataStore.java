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
import org.ignitionone.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class JDBCDataStore implements DataStore {
    private static final Logger LOG = LoggerFactory.getLogger(JDBCDataStore.class);
    private final Sql2o sql2o;
    private final String url;
    private final String username;
    private final String password;
    private final String sqlquery;
    private final String insertquery;
    private final boolean appendDateId;

    public JDBCDataStore(Properties config, String jdbcType) {
        String prefix = "jdbc." + jdbcType + ".";

        this.username = config.getProperty(prefix + "username", "");
        this.password = config.getProperty(prefix + "password", "");
        this.url = config.getProperty(prefix + "url", "");
        this.sqlquery = config.getProperty(prefix + "sqlquery", "");
        this.insertquery = config.getProperty(prefix + "insertquery", "");
        this.appendDateId = Boolean.parseBoolean(config.getProperty(prefix + "appenddate", "false"));

        sql2o = new Sql2o(url, username, password);
        LOG.info("Connected to {} successfully!", url);
    }

    @Override
    public void insertAdsTxtRecords(List<AdsTxtRecord> adsTxtRecords) {
        try (Connection connection = sql2o.open()) {
            for (AdsTxtRecord adsTxtRecord : adsTxtRecords) {
                connection.createQuery(insertquery).bind(adsTxtRecord).executeUpdate();
            }
        } catch (Exception e) {
            LOG.error("Data insertion failed! {}", e);
        }
        LOG.debug("Total number of valid AdsTxtRecords inserted into the DB: {}", adsTxtRecords.size());
    }

    @Override
    public Set<String> getAdsTxtUrls() {
        List<String> results;
        try (Connection connection = sql2o.open()) {
            results = connection.createQuery(buildQuery())
                    .executeAndFetch(String.class);
        }
        LOG.debug("Got {} rows from the database.", results.size());
        return new HashSet<>(results);
    }

    private String buildQuery() {
        return appendDateId ? sqlquery + DateUtils.getYesterdaysDateId() : sqlquery;
    }
}

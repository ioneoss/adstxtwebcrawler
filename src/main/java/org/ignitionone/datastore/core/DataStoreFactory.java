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

package org.ignitionone.datastore.core;

import org.ignitionone.configuration.ConfigBuilder;
import org.ignitionone.datastore.ConsoleDataStore;
import org.ignitionone.datastore.FileDataStore;
import org.ignitionone.datastore.JDBCDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class DataStoreFactory {
    private static final Logger LOG = LoggerFactory.getLogger(DataStoreFactory.class);

    private static DataStore getDataStore(DataStoreType dataStoreType) {
        Properties config = ConfigBuilder.getConfig();
        switch (dataStoreType) {
            case POSTGRES:
                return new JDBCDataStore(config, "postgres");
            case FILE:
                return new FileDataStore(config);
            case CONSOLE:
                return new ConsoleDataStore();
            default:
                LOG.error("Could not find data store! Exiting!!!");
                System.exit(111);
                return null;
        }
    }


    public static ReadDataStore getReadDataStore(DataStoreType dataStoreType) {
        return getDataStore(dataStoreType);
    }

    public static WriteDataStore getWriteDataStore(DataStoreType dataStoreType) {
        return getDataStore(dataStoreType);
    }

}
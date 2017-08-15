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
package org.ignitionone.configuration;

import org.ignitionone.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigBuilder.class);
    private static Properties config;
    static final String CONFIG_PROPERTY = "config.file";
    static final String CONFIG_PROPERTY_DEFAULT_VALUE = "config.properties";

    public static Properties getConfig() {
        if (config == null) {
            config = new Properties();
            setPropertiesFileLocation();
            readConfig();
        }
        return config;
    }

    public static void clearConfig() {
        config = null;
    }

    private static void setPropertiesFileLocation() {
        String propertiesFile = System.getProperty(CONFIG_PROPERTY);
        if (propertiesFile == null || propertiesFile.length() == 0) {
            System.setProperty(CONFIG_PROPERTY, CONFIG_PROPERTY_DEFAULT_VALUE);
        } else {
            System.setProperty(CONFIG_PROPERTY, propertiesFile);
        }
    }

    private static Properties readConfig() {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(System.getProperty(CONFIG_PROPERTY));
        } catch (FileNotFoundException e) {
            inputStream = Application.class.getClassLoader().getResourceAsStream(System.getProperty(CONFIG_PROPERTY));
        }
        try {
            config.load(inputStream);
            LOG.info("Loaded config from properties file.");
        } catch (IOException e) {
            LOG.error("Error loading properties. Exiting...", e);
            System.exit(2);
        } // TODO: 7/31/17 Close inputstream
        return config;
    }
}

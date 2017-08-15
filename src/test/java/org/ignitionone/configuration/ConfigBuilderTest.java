package org.ignitionone.configuration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ConfigBuilderTest {

    @Before
    public void setup() {
        ConfigBuilder.clearConfig();
    }

    @After
    public void teardown() {
        System.clearProperty(ConfigBuilder.CONFIG_PROPERTY);
    }

    @Test
    public void testGetConfigOverride() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("testconfig.properties").getFile());
        System.setProperty(ConfigBuilder.CONFIG_PROPERTY, file.getAbsolutePath());
        Properties properties = ConfigBuilder.getConfig();
        assertEquals("testvalue", properties.getProperty("testkey"));
    }

    @Test
    public void testGetConfigDefault() {
        Properties properties = ConfigBuilder.getConfig();
        assertEquals("500", properties.getProperty("parallec.http.parallelism"));
    }
}

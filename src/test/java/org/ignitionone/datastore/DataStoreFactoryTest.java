package org.ignitionone.datastore;

import org.ignitionone.datastore.core.DataStoreFactory;
import org.ignitionone.datastore.core.DataStoreType;
import org.ignitionone.datastore.core.ReadDataStore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DataStoreFactoryTest {
    @Test
    public void testGetDataStore() {
        ReadDataStore postgresRead = DataStoreFactory.getReadDataStore(DataStoreType.POSTGRES);
        assertTrue(postgresRead instanceof JDBCDataStore);
    }
}

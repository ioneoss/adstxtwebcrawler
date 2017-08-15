# An async, production ready web crawler for ads.txt 
---------

### What is ads.txt?

From iabtechlab.com website: 
 
 "The mission of the ads.txt project is simple: Increase transparency in the programmatic 
 advertising ecosystem. Ads.txt stands for Authorized Digital Sellers and is a simple, 
 flexible and secure method that publishers and distributors can use to publicly 
 declare the companies they authorize to sell their digital inventory.
  
 By creating a public record of Authorized Digital Sellers, ads.txt will create greater 
 transparency in the inventory supply chain, and give publishers control over their 
 inventory in the market, making it harder for bad actors to profit from selling counterfeit 
 inventory across the ecosystem. As publishers adopt ads.txt, buyers will be able to more 
 easily identify the Authorized Digital Sellers for a participating publisher, allowing 
 brands to have confidence they are buying authentic publisher inventory."
 
 You can find more details here: https://iabtechlab.com/ads-txt/

### How does the web crawler work?
In order to query/fetch and store the ads.txt data from advertiser sites, we need a web crawler that can 
execute http requests, handle responses and save them to a datasource in a considerable amount of time so that this data can then be used during the bidding process.
The crawler does exactly that: Query one or more data sources for seed urls/domains, fire http requests to each one of them 
(aync), process and save those responses to one or more datastores. The crawler delegates the actual http querying task
to a library called [parallec by ebay](https://github.com/eBay/parallec). Database interaction is carried out using [Sql2O](https://github.com/aaberg/sql2o) 

### Components of the webcrawler
* Data stores - Data stores are the parts that read and write data from/to data sources
* Http Service - HttpService offers methods to carry out the actual http request/response handling (it is a wrapper on top of parallec)
* AdsTxt Service - This service is responsible for orchestration and execution of the program  
 
### Config and execution

You can pass a config file with connection and other properties:

```
# postgres config
jdbc.postgres.username=username
jdbc.postgres.password=password
jdbc.postgres.url=jdbc:postgresql://servername:5432/dbname
jdbc.postgres.sqlquery=SELECT url FROM adstxt_urls
jdbc.postgres.appenddate=false
jdbc.postgres.insertquery=INSERT INTO adstxt_results(insert_date, source_domain, adserving_domain, publisher_acc_id, acc_type, cert_auth_id) VALUES (:insertDate, :sourceDomain, :adServingDomain, :publisherAccId, :accType, :certAuthId)

# Filestore details
filestore.inputfilename=/Users/shridhar.manvi/Desktop/inputfile.csv
filestore.outputfilename=/Users/shridhar.manvi/Desktop/outputfile.csv

# parallec
parallec.http.parallelism=500
parallec.http.port=80

# Define read and write data stores here. Data will be read from and written to each store.
datastores.read=file
datastores.write=file,console

```

### DataStore table structure

The app assumes the table that we write to is in the following format: (if you choose any other structure, make changes to the query in config accordingly)

```sql
CREATE TABLE adstxt_results (
insert_date TIMESTAMP ,
source_domain VARCHAR(200),
adserving_domain VARCHAR(200),
publisher_acc_id VARCHAR(200),
acc_type VARCHAR(200),
cert_auth_id VARCHAR(200)
);
```

### Adding a new data store

The app readily works for any jdbc store with only some changes in config (no code change needed). If you need to write to any other data stores, you need to implement one. 

Create a new implementaion of DataStore and implement the methods as needed. Datasource config and setup must be taken care in the constructor of that store. 
(Check out one of the implementations of datastore)

Add the new store in the config depending on weather the store will be used to read urls or write the adstxt objects to.

##### Step 1: Create a new implementation of DataStore

```java
/**
* DataStore implements both read and write stores. Make sure to implement atleast 
* one method depending on what the store is used for (either reading or writing) 
*/
public class FileDataStore implements DataStore {

    @Override
    public void insertAdsTxtRecords(List<AdsTxtRecord> adsTxtRecords) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getAdsTxtUrls() {throw new UnsupportedOperationException();}
}

```

##### Step 2: Define constructor and pass config
 
 ```java
 public class FileDataStore implements DataStore {
    
	private static String FILENAME;

    public FileDataStore(Properties properties) {
            // Read config etc.
            READFILE = properties.getProperty("filestore.inputfilename", "");
            WRITEFILE = properties.getProperty("filestore.outputfilename", "");
        }
}
```

##### Step 3: Add those properties to config file

```

# Filestore details
filestore.inputfilename=/path/to/inputfile
filestore.outputfilename=/path/to/outputfile
```

##### Step 4: Implement read/write method depending on the need. 

```java

public class FileDataStore implements DataStore {

    @Override
    public void insertAdsTxtRecords(List<AdsTxtRecord> adsTxtRecords) {

        try (FileWriter fileWriter = new FileWriter(WRITEFILE)) {
            adsTxtRecords.forEach(adsTxtRecord ->
                    writeLine(fileWriter, adsTxtRecord));
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return urls;
    }
}
```

##### Step 5: Add enum 
```java
public enum DataStoreType {
    POSTGRES,
    FILE,
    CONSOLE;
    
    public static DataStoreType getValueOf(String dataStoreType) {
        return valueOf(dataStoreType.toUpperCase());
    }
}
```

##### Step 6: Add the implementation to DataStoreFactory

```java

public class DataStoreFactory {

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
}
```

##### Step 7: Add the store name as read or write in the config

```
datastores.read=file
datastores.write=postgres,file

# This config reads urls from both mssql and file and writes the adstxt objects to postgres and file (provided there are implementations for these stores)
```
Make sure atleast one implementation of read and write stores exists. 

##### Step 8: Execute the code and validate if data is inserted into all the stores listed in the config
 
 ```
 Fri Aug 11 10:40:31 EDT 2017,www.techworld.com,openx.com,537136140,DIRECT,a698e2ec38604c6
 Fri Aug 11 10:40:31 EDT 2017,www.techworld.com,openx.com,538962444,DIRECT,a698e2ec38604c6
 Fri Aug 11 10:40:31 EDT 2017,www.techworld.com,indexexchange.com,184858,DIRECT,50b1c356f2c5c8fc
 Fri Aug 11 10:40:31 EDT 2017,www.techworld.com,indexexchange.com,183980,DIRECT,50b1c356f2c5c8fc
 Fri Aug 11 10:40:31 EDT 2017,www.techworld.com,rubiconproject.com,10778,DIRECT,
 Fri Aug 11 10:40:31 EDT 2017,www.techworld.com,contextweb.com,561182,DIRECT,
 Fri Aug 11 10:40:31 EDT 2017,www.techworld.com,google.com,pub-6768877878518502,DIRECT,f08c47fec0942fa0
 Fri Aug 11 10:40:31 EDT 2017,www.techworld.com,google.com,pub-7443704194229694,DIRECT,f08c47fec0942fa0
 Fri Aug 11 10:40:31 EDT 2017,www.techworld.com,pubmatic.com,156271,DIRECT,
 Fri Aug 11 10:40:31 EDT 2017,www.businessinsider.com,google.com,pub-1037373295371110,DIRECT,
 Fri Aug 11 10:40:31 EDT 2017,www.businessinsider.com,rubiconproject.com,10306,DIRECT,
 Fri Aug 11 10:40:31 EDT 2017,www.businessinsider.com,indexexchange.com,183963,DIRECT,
 Fri Aug 11 10:40:31 EDT 2017,www.businessinsider.com,indexexchange.com,184913,DIRECT,
 Fri Aug 11 10:40:31 EDT 2017,www.businessinsider.com,openx.com,537147789,DIRECT,
 Fri Aug 11 10:40:31 EDT 2017,www.businessinsider.com,openx.com,538986829,DIRECT,
 Fri Aug 11 10:40:31 EDT 2017,www.businessinsider.com,appnexus.com,7161,DIRECT,
 Fri Aug 11 10:40:31 EDT 2017,www.businessinsider.com,appnexus.com,3364,RESELLER,
 Fri Aug 11 10:40:31 EDT 2017,www.businessinsider.com,facebook.com,1325898517502065,DIRECT,
 Fri Aug 11 10:40:31 EDT 2017,www.businessinsider.com,liveintent.com,87,DIRECT,
 ```

--------

#### Safeguards
Since the crawler crawls a variety of ads.txt files, validating the right response is often hard since the files may be written in slightly different formats. We have tried to cover 
all validations but in case you find some exceptions, please submit a PR.
 
* The crawler ensures parsing of valid http responses
* It has checks which will skip html responses, comments, blank lines etc. 

#### External packages used
* [Sql2O](http://www.sql2o.org/)
* [parallec by ebay](https://github.com/eBay/parallec) 
   
#### Build and config:
 * To build the project (using maven) execute
 ``mvn clean package``
 
 * To execute the jar pass config file as parameter

 `path/to/java -Dconfig.file=/path/to/config/file -Dlogback.xml=/path/to/logback.xml -jar ads-webcrawler-<version>.jar`
 
 #### Logging
 Logging can be configured in logback.xml and the file can be passed during startup as shown above.
 
 #### Initial Author
 Shridhar Manvi <Shridhar.Manvi AT ignitionone DOT com>
 
 #### Issues and contribution
 Please feel free to create an issue for questions/bugs etc.
 
 
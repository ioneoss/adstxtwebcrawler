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

import org.ignitionone.model.HttpResponse;
import org.ignitionone.util.ParserUtil;
import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.ResponseOnSingleTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HttpService {
    private static final Logger LOG = LoggerFactory.getLogger(HttpService.class);
    private static final String ADSTXT = "/ads.txt";

    private final String httpPort;
    private final String httpParallelism;

    public HttpService(Properties config) {
        this.httpPort = config.getProperty("parallec.http.port");
        this.httpParallelism = config.getProperty("parallec.http.parallelism");
    }

    public List<HttpResponse> executeHttpRequests(List<String> domains) {
        List<HttpResponse> synchronizedHttpResponses = Collections.synchronizedList(new ArrayList<HttpResponse>());
        AdsTxtResponseHandler adsTxtResponseHandler = new AdsTxtResponseHandler(synchronizedHttpResponses);

        LOG.info("Starting parallel http requests..");
        ParallelClient parallelClient = new ParallelClient();

        ParallelTask task = parallelClient.prepareHttpGet(ADSTXT)
                .async()
                .setHttpPort(Integer.parseInt(httpPort))
                .setConcurrency(Integer.parseInt(httpParallelism))
                .setTargetHostsFromList(domains)
                .execute(adsTxtResponseHandler);

        while (!task.isCompleted()) {
            try {
                Thread.sleep(100L);
                LOG.debug(String.format("POLL_JOB_PROGRESS (%.5g%%)  PT jobid: %s",
                        task.getProgress(), task.getTaskId()));
                parallelClient.logHealth();
            } catch (InterruptedException e) {
                LOG.error("Error occurred waiting for task to complete", e);
            }
        }
        parallelClient.releaseExternalResources();

        return synchronizedHttpResponses;
    }

    private class AdsTxtResponseHandler implements ParallecResponseHandler {

        private List<HttpResponse> synchronizedHttpResponses;

        public AdsTxtResponseHandler(List<HttpResponse> synchronizedHttpResponses) {
            this.synchronizedHttpResponses = synchronizedHttpResponses;
        }

        @Override
        public void onCompleted(ResponseOnSingleTask response, Map<String, Object> responseContext) {
            // Only add 200s to result list
            if (response.getStatusCodeInt() == 200) {
                HttpResponse httpResponse;
                String content = response.getResponseContent();
                if (!ParserUtil.isHtml(content)) {
                    httpResponse = new HttpResponse()
                            .setResponseCode(response.getStatusCodeInt())
                            .setResponseContent(content)
                            .setDomainName(response.getHost())
                            .setHasAdsTxt(ParserUtil.isAdsTXT(content));
                    synchronizedHttpResponses.add(httpResponse);
                }
            }
        }
    }
}
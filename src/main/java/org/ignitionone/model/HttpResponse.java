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
package org.ignitionone.model;

public class HttpResponse {
    private int responseCode;
    private String responseContent;
    private String domainName;
    private boolean hasAdsTxt;

    public int getResponseCode() {
        return responseCode;
    }

    public HttpResponse setResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public HttpResponse setResponseContent(String responseContent) {
        this.responseContent = responseContent;
        return this;
    }

    public String getDomainName() {
        return domainName;
    }

    public HttpResponse setDomainName(String domainName) {
        this.domainName = domainName;
        return this;
    }

    public boolean hasAdsTxtData() {
        return hasAdsTxt;
    }

    public HttpResponse setHasAdsTxt(boolean hasAdsTxt) {
        this.hasAdsTxt = hasAdsTxt;
        return this;
    }

    @Override
    public String toString() {
        return "StatusCode: " + this.getResponseCode() + "\n" +
                "HasAdsTxt : " + this.hasAdsTxtData() + "\n" +
                "DomainName: " + this.getDomainName() + "\n" +
                "ResponseContent: " + this.getResponseContent();
    }
}

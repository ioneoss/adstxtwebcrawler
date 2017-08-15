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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class AdsTxtRecord {
    private DateTime insertDate;
    private String sourceDomain;
    private String adServingDomain;
    private String publisherAccId;
    private String accType;
    private String certAuthId;

    public String getSourceDomain() {
        return sourceDomain;
    }

    public DateTime getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(DateTime insertDate) {
        this.insertDate = insertDate;
    }

    public void setSourceDomain(String sourceDomain) {
        this.sourceDomain = sourceDomain;
    }

    public String getAdServingDomain() {
        return adServingDomain;
    }

    public void setAdServingDomain(String adServingDomain) {
        this.adServingDomain = adServingDomain;
    }

    public String getPublisherAccId() {
        return publisherAccId;
    }

    public void setPublisherAccId(String publisherAccId) {
        this.publisherAccId = publisherAccId;
    }

    public String getAccType() {
        return accType;
    }

    public void setAccType(String accType) {
        this.accType = accType;
    }

    public String getCertAuthId() {
        return certAuthId;
    }

    public void setCertAuthId(String certAuthId) {
        this.certAuthId = certAuthId;
    }

    @Override
    public String toString() {
        return DateTimeFormat.forPattern("YYYY-M-dd HH:MM:SS").print(this.getInsertDate()) + "," +
                this.getSourceDomain() + "," +
                this.getAdServingDomain() + "," +
                this.getPublisherAccId() + "," +
                this.getAccType() + "," +
                this.getCertAuthId() + "\n";
    }
}
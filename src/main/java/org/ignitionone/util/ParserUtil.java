/**
 * Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

/**
 * @author Shridhar Manvi <Shridhar.Manvi AT ignitionone DOT com>
 * @author Roderick Rodriguez <Roderick.Rodriguez AT ignitionone DOT com>
 */
package org.ignitionone.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ParserUtil {
    private static final String WWWPATTERN = "[^(www.)][a-zA-Z0-9]['.'][a-z]"; // Negate starting with www
    private static final String HTMLTAG = "<[('/')?('!')?a-zA-Z ]*.>"; // Matches any html tag, also <!DOCTYPE html>.
    private static final String ADSTXT = "(#Ads.txt[a-zA-Z]*)";
    private static final Pattern wwwPattern = Pattern.compile(WWWPATTERN);
    private static final Pattern htmlPattern = Pattern.compile(HTMLTAG);
    private static final Pattern adstxtPattern = Pattern.compile(ADSTXT);

    public static Set<String> filterValidUrls(Set<String> domains) {

        Set<String> filtered = domains.parallelStream()
                .map(domain -> ParserUtil.sanitizeUrl(domain))
                .filter(domain -> domain != null)
                .filter(domain -> !domain.isEmpty())
                .collect(Collectors.toSet());

        return filtered;
    }

    public static String sanitizeUrl(String content) {
        // missing protocol is handled by parallec library
        try {
            URL url = new URL(content);
            return url.toString();
        } catch (MalformedURLException e) {
            Matcher wwwMatcher = wwwPattern.matcher(content);

            if (wwwMatcher.find()) {
                if (!content.startsWith("www.")) {
                    content = "www." + content;
                }
                return content;
            } else {
                return null;
            }
        }
    }

    public static boolean isHtml(String content) {
        Matcher htmlMatcher = htmlPattern.matcher(content);
        return htmlMatcher.find();
    }

    public static boolean isAdsTXT(String content) {
        Matcher adsTxtMatcher = adstxtPattern.matcher(content);
        return adsTxtMatcher.find();
    }
}

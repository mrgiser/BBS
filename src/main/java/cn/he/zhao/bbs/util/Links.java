/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.he.zhao.bbs.util;

import cn.he.zhao.bbs.entity.Link;
import cn.he.zhao.bbs.entityUtil.LinkUtil;
import cn.he.zhao.bbs.spring.Common;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LinkUtil utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.1, Apr 5, 2018
 * @since 1.6.0
 */
public final class Links {

    /**
     * Logger.
     */
    public static final Logger logger = LoggerFactory.getLogger(Links.class);

    /**
     * Gets links from the specified HTML.
     *
     * @param baseURL the specified base URL
     * @param html    the specified HTML
     * @return a list of links, each of them like this:      <pre>
     * {
     *     "linkAddr": "https://hacpai.com/article/1440573175609",
     *     "linkTitle": "黑客派简介",
     *     "linkKeywords": "",
     *     "linkHTML": "page HTML",
     *     "linkText": "page text",
     *     "linkBaiduRefCnt": int
     * }
     * </pre>
     */
    public static List<JSONObject> getLinks(final String baseURL, final String html) {
        final Document doc = Jsoup.parse(html, baseURL);
        final Elements urlElements = doc.select("a");

        final Set<String> urls = new HashSet<>();
        final List<Spider> spiders = new ArrayList<>();

        String url = null;
        for (final Element urlEle : urlElements) {
            try {
                url = urlEle.absUrl("href");
                if (StringUtils.isBlank(url) || !StringUtils.contains(url, "://")) {
                    url = StringUtils.substringBeforeLast(baseURL, "/") + url;
                }

                final URL formedURL = new URL(url);
                final String protocol = formedURL.getProtocol();
                final String host = formedURL.getHost();
                final int port = formedURL.getPort();
                final String path = formedURL.getPath();

                url = protocol + "://" + host;
                if (-1 != port && 80 != port && 443 != port) {
                    url += ":" + port;
                }
                url += path;

                if (StringUtils.endsWith(url, "/")) {
                    url = StringUtils.substringBeforeLast(url, "/");
                }

                urls.add(url);
            } catch (final Exception e) {
                logger.warn("Can't parse [" + url + "]");
            }
        }

        final List<JSONObject> ret = new ArrayList<>();

        try {
            for (final String u : urls) {
                spiders.add(new Spider(u));
            }

            final List<Future<JSONObject>> results = Symphonys.EXECUTOR_SERVICE.invokeAll(spiders);
            for (final Future<JSONObject> result : results) {
                final JSONObject link = result.get();
                if (null == link) {
                    continue;
                }

                ret.add(link);
            }
        } catch (final Exception e) {
            logger.error( "Parses URLs failed", e);
        }

        Collections.sort(ret, Comparator.comparingInt(link -> link.optInt(LinkUtil.LINK_BAIDU_REF_CNT)));

        return ret;
    }

    private static boolean containsChinese(final String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }

        final Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        final Matcher m = p.matcher(str);

        return m.find();
    }

    static class Spider implements Callable<JSONObject> {
        private final String url;

        Spider(final String url) {
            this.url = url;
        }

        @Override
        public JSONObject call() throws Exception {
            final int TIMEOUT = 2000;

            try {
                final JSONObject ret = new JSONObject();

                // Get meta info of the URL
                final Connection.Response res = Jsoup.connect(url).timeout(TIMEOUT).followRedirects(false).execute();
                if (HttpServletResponse.SC_OK != res.statusCode()) {
                    return null;
                }

                String charset = res.charset();
                if (StringUtils.isBlank(charset)) {
                    charset = "UTF-8";
                }
                final String html = new String(res.bodyAsBytes(), charset);

                String title = StringUtils.substringBetween(html, "<title>", "</title>");
                title = StringUtils.trim(title);
                if (!containsChinese(title)) {
                    return null;
                }
                title = Emotions.toAliases(title);

                final String keywords = StringUtils.substringBetween(html, "eywords\" content=\"", "\"");

                ret.put(LinkUtil.LINK_ADDR, url);
                ret.put(LinkUtil.LINK_TITLE, title);
                ret.put(LinkUtil.LINK_T_KEYWORDS, keywords);
                ret.put(LinkUtil.LINK_T_HTML, html);
                final Document doc = Jsoup.parse(html);
                doc.select("pre").remove();
                ret.put(LinkUtil.LINK_T_TEXT, doc.text());

                // Evaluate the URL
                URL baiduURL = new URL("https://www.baidu.com/s?pn=0&wd=" + URLs.encode(url));
                HttpURLConnection conn = (HttpURLConnection) baiduURL.openConnection();
                conn.setConnectTimeout(TIMEOUT);
                conn.setReadTimeout(TIMEOUT);
                conn.addRequestProperty(Common.USER_AGENT, Symphonys.USER_AGENT_BOT);

                String baiduRes;
                try (final InputStream inputStream = conn.getInputStream()) {
                    baiduRes = IOUtils.toString(inputStream, "UTF-8");
                }
                conn.disconnect();

                int baiduRefCnt = StringUtils.countMatches(baiduRes, "<em>" + url + "</em>");
                if (1 > baiduRefCnt) {
                    ret.put(LinkUtil.LINK_BAIDU_REF_CNT, baiduRefCnt);
                    logger.debug(ret.optString(LinkUtil.LINK_ADDR));

                    return ret;
                } else {
                    baiduURL = new URL("https://www.baidu.com/s?pn=10&wd=" + URLs.encode(url));
                    conn = (HttpURLConnection) baiduURL.openConnection();
                    conn.setConnectTimeout(TIMEOUT);
                    conn.setReadTimeout(TIMEOUT);
                    conn.addRequestProperty(Common.USER_AGENT, Symphonys.USER_AGENT_BOT);

                    try (final InputStream inputStream = conn.getInputStream()) {
                        baiduRes = IOUtils.toString(inputStream, "UTF-8");
                    }
                    conn.disconnect();

                    baiduRefCnt += StringUtils.countMatches(baiduRes, "<em>" + url + "</em>");

                    ret.put(LinkUtil.LINK_BAIDU_REF_CNT, baiduRefCnt);
                    logger.debug(ret.optString(LinkUtil.LINK_ADDR));

                    return ret;
                }
            } catch (final SocketTimeoutException e) {
                return null;
            } catch (final Exception e) {
                logger.warn("Parses URL [" + url + "] failed", e);
                return null;
            }
        }
    }
}

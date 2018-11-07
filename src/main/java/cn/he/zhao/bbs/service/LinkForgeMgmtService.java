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
package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.cache.TagCache;
import cn.he.zhao.bbs.entityUtil.LinkUtil;
import cn.he.zhao.bbs.entityUtil.OptionUtil;
import cn.he.zhao.bbs.entityUtil.TagUtil;
import cn.he.zhao.bbs.entityUtil.UserExtUtil;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.spring.Strings;
import cn.he.zhao.bbs.util.JsonUtil;
import cn.he.zhao.bbs.util.Links;
import cn.he.zhao.bbs.util.Pangu;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.apache.commons.lang.StringUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * LinkUtil forge management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.2.0, Nov 30, 2017
 * @since 1.6.0
 */
@Service
public class LinkForgeMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkForgeMgmtService.class);

    /**
     * LinkUtil Mapper.
     */
    @Autowired
    private LinkMapper linkMapper;

    /**
     * Tag Mapper.
     */
    @Autowired
    private TagMapper tagMapper;

    /**
     * OptionUtil Mapper.
     */
    @Autowired
    private OptionMapper optionMapper;

    /**
     * Tag-User-LinkUtil Mapper.
     */
    @Autowired
    private TagUserLinkMapper tagUserLinkMapper;

    /**
     * Tag cache.
     */
    @Autowired
    private TagCache tagCache;

    /**
     * URL fetch service.
     */
//    private URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();

    /**
     * Forges the specified URL.
     *
     * @param url    the specified URL
     * @param userId the specified user id
     */
    @Transactional
    public void forge(final String url, final String userId) {
        if (!StringUtils.startsWithIgnoreCase(url, "http://") && !StringUtils.startsWithIgnoreCase(url, "https://")) {
            return;
        }

        try {
            final URL u = new URL(url);
            if (StringUtils.containsIgnoreCase( SpringUtil.getServerPath(), u.getHost())) {
                return;
            }
        } catch (final Exception e) {
            return;
        }

        String html;
        String baseURL;
        try {
            final Document doc = Jsoup.connect(url).timeout(5000).userAgent(Symphonys.USER_AGENT_BOT).get();

            doc.select("body").prepend("<a href=\"" + url + "\">" + url + "</a>"); // Add the specified URL itfself

            html = doc.html();
            baseURL = doc.baseUri();
        } catch (final Exception e) {
            LOGGER.error( "Parses link [" + url + "] failed", e);

            return;
        }

        final List<JSONObject> links = Links.getLinks(baseURL, html);
        final List<JSONObject> cachedTags = tagCache.getTags();

//        final Transaction transaction = linkMapper.beginTransaction();
        try {
            for (final JSONObject lnk : links) {
                final String addr = lnk.optString(LinkUtil.LINK_ADDR);
                if (LinkUtil.inAddrBlacklist(addr)) {
                    continue;
                }

                JSONObject link = linkMapper.getLink(addr);

                if (null == link) {
                    link = new JSONObject();
                    link.put(LinkUtil.LINK_ADDR, lnk.optString(LinkUtil.LINK_ADDR));
                    link.put(LinkUtil.LINK_BAD_CNT, 0);
                    link.put(LinkUtil.LINK_BAIDU_REF_CNT, 0);
                    link.put(LinkUtil.LINK_CLICK_CNT, 0);
                    link.put(LinkUtil.LINK_GOOD_CNT, 0);
                    link.put(LinkUtil.LINK_SCORE, 0);
                    link.put(LinkUtil.LINK_SUBMIT_CNT, 0);
                    link.put(LinkUtil.LINK_TITLE, lnk.optString(LinkUtil.LINK_TITLE));
                    link.put(LinkUtil.LINK_TYPE, LinkUtil.LINK_TYPE_C_FORGE);
                    link.put(LinkUtil.LINK_PING_CNT, 0);
                    link.put(LinkUtil.LINK_PING_ERR_CNT, 0);

                    LOGGER.info(link.optString(LinkUtil.LINK_ADDR) + "__" + link.optString(LinkUtil.LINK_TITLE));
                    Link link1 = JsonUtil.json2Bean(link.toString(),Link.class);
                    linkMapper.add(link1);

                    final Option linkCntOption = optionMapper.get(OptionUtil.ID_C_STATISTIC_LINK_COUNT);
                    final int linkCnt = Integer.getInteger(linkCntOption.getOptionValue());
                    linkCntOption.setOptionValue(String.valueOf(linkCnt + 1));
                    optionMapper.update(OptionUtil.ID_C_STATISTIC_LINK_COUNT, linkCntOption);
                } else {
                    link.put(LinkUtil.LINK_BAIDU_REF_CNT, lnk.optInt(LinkUtil.LINK_BAIDU_REF_CNT));
                    link.put(LinkUtil.LINK_TITLE, lnk.optString(LinkUtil.LINK_TITLE));
                    link.put(LinkUtil.LINK_SCORE, lnk.optInt(LinkUtil.LINK_BAIDU_REF_CNT)); // XXX: Need a score algorithm

                    Link link1 = JsonUtil.json2Bean(link.toString(),Link.class);
                    linkMapper.update(link.optString(Keys.OBJECT_ID), link1);
                }

                final String linkId = link.optString(Keys.OBJECT_ID);
                final double linkScore = link.optDouble(LinkUtil.LINK_SCORE, 0D);
                String title = link.optString(LinkUtil.LINK_TITLE) + " " + link.optString(LinkUtil.LINK_T_KEYWORDS);
                title = Pangu.spacingText(title);
                String[] titles = title.split(" ");
                titles = Strings.trimAll(titles);

                for (final JSONObject cachedTag : cachedTags) {
                    final String tagId = cachedTag.optString(Keys.OBJECT_ID);

                    final String tagTitle = cachedTag.optString(TagUtil.TAG_TITLE);
                    if (!Strings.containsIgnoreCase(tagTitle, titles)) {
                        continue;
                    }

                    final Tag tag = tagMapper.get(tagId);

                    // clean
                    tagUserLinkMapper.removeByTagIdUserIdAndLinkId(tagId, userId, linkId);

                    // re-add
                    final TagUserLink tagLinkRel = new TagUserLink();
                    tagLinkRel.setTagId(tagId);
                    tagLinkRel.setUserId(userId);
                    tagLinkRel.setLinkId(linkId);
                    tagLinkRel.setLinkScore(linkScore);
                    tagUserLinkMapper.add(tagLinkRel);

                    // refresh link score
                    tagUserLinkMapper.updateTagLinkScore(tagId, linkId, linkScore);

                    // re-calc tag link count
                    final int tagLinkCnt = tagUserLinkMapper.countTagLink(tagId);
                    tag.setTagLinkCount(tagLinkCnt);
                    tagMapper.update(tagId, tag);
                }
            }

//            transaction.commit();

            LOGGER.info("Forged link [" + url + "]");
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error( "Saves links failed", e);
        }
    }

    /**
     * Purges link forge.
     */
    @Transactional
    public void purge() {
        new Thread(() -> {
//            final Transaction transaction = optionMapper.beginTransaction();

            try {
                Thread.sleep(15 * 1000);

                final Option linkCntOption = optionMapper.get(OptionUtil.ID_C_STATISTIC_LINK_COUNT);
                int linkCnt = Integer.getInteger(linkCntOption.getOptionValue());

                int slags = 0;
                List<Link> links = linkMapper.getAll();
                for (int i = 0; i < links.size(); i++) {
                    final Link link = links.get(i);
                    final String linkAddr = link.getLinkAddr();

                    if (!LinkUtil.inAddrBlacklist(linkAddr) && link.getLinkPingErrCnt() < 7) {
                        continue;
                    }

                    final String linkId = link.getOid();

                    // clean slags

                    linkMapper.remove(linkId);
                    ++slags;

                    final List<String> tagIds = tagUserLinkMapper.getTagIdsByLinkId(linkId, Integer.MAX_VALUE);
                    for (final String tagId : tagIds) {
                        final Tag tag = tagMapper.get(tagId);

                        tagUserLinkMapper.removeByLinkId(linkId);

                        final int tagLinkCnt = tagUserLinkMapper.countTagLink(tagId);
                        tag.setTagLinkCount(tagLinkCnt);
                        tagMapper.update(tagId, tag);
                    }
                }

                linkCntOption.setOptionValue(String.valueOf(linkCnt - slags));
                optionMapper.update(OptionUtil.ID_C_STATISTIC_LINK_COUNT, linkCntOption);

//                transaction.commit();

                LOGGER.info("Purged link forge [slags=" + slags + "]");

                // Ping
                links = linkMapper.getAll();
                LOGGER.info("Ping links [size=" + links.size() + "]");
                final CountDownLatch countDownLatch = new CountDownLatch(links.size());
                for (int i = 0; i < links.size(); i++) {
                    final Link link = links.get(i);
                    Symphonys.EXECUTOR_SERVICE.submit(new CheckTask(link, countDownLatch));
                }
                countDownLatch.await(1, TimeUnit.HOURS);
                LOGGER.info("Pinged links [size=" + links.size()
                        + ", countDownLatch=" + countDownLatch.getCount() + "]");
            } catch (final Exception e) {
//                if (null != transaction) {
//                    transaction.rollback();
//                }

                LOGGER.error( "Purges link forge failed", e);
            } finally {
                // TODO: 2018/11/3 ????
//                JdbcMapper.dispose();
            }
        }).start();
    }

    /**
     * LinkUtil accessibility check task.
     *
     * @author <a href="http://88250.b3log.org">Liang Ding</a>
     * @version 1.0.0.0, Jun 14, 2017
     * @since 2.2.0
     */
    private class CheckTask implements Runnable {

        /**
         * LinkUtil to check.
         */
        private final Link link;

        /**
         * Count down latch.
         */
        private final CountDownLatch countDownLatch;

        /**
         * Constructs a check task with the specified link.
         *
         * @param link           the specified link
         * @param countDownLatch the specified count down latch
         */
        public CheckTask(final Link link, final CountDownLatch countDownLatch) {
            this.link = link;
            this.countDownLatch = countDownLatch;
        }

        @Override
        @Transactional
        public void run() {
            final String linkAddr = link.getLinkAddr();

            LOGGER.debug("Checks link [url=" + linkAddr + "] accessibility");
            final long start = System.currentTimeMillis();
            int responseCode = 0;
            CloseableHttpClient httpClient = HttpClients.createDefault();
            try {
                final HttpGet request = new HttpGet(linkAddr);
                request.addHeader("User-Agent", Symphonys.USER_AGENT_BOT);
//                request.setURL(new URL(linkAddr));
                RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(1000)
                        .setConnectionRequestTimeout(1000).setSocketTimeout(1000 * 5).build();
                request.setConfig(defaultRequestConfig);
//                request.setConnectTimeout(1000);
//                request.setReadTimeout(1000 * 5);

                final CloseableHttpResponse response = httpClient.execute(request);
//                final HTTPResponse response = urlFetchService.fetch(request);
                StatusLine status = response.getStatusLine();
                responseCode = status.getStatusCode();

                LOGGER.debug( "Accesses link [url=" + linkAddr + "] response [code={0}]", responseCode);
            } catch (final Exception e) {
                LOGGER.warn("LinkUtil [url=" + linkAddr + "] accessibility check failed [msg=" + e.getMessage() + "]");
            } finally {
                countDownLatch.countDown();

                final long elapsed = System.currentTimeMillis() - start;
                LOGGER.debug( "Accesses link [url=" + linkAddr + "] response [code=" + responseCode + "], "
                        + "elapsed [" + elapsed + ']');

                link.setLinkPingCnt(link.getLinkPingCnt() + 1);
                if (HttpServletResponse.SC_OK != responseCode) {
                    link.setLinkPingErrCnt(link.getLinkPingErrCnt() + 1);
                }

//                final Transaction transaction = linkMapper.beginTransaction();
                try {
                    linkMapper.update(link.getOid(), link);

//                    transaction.commit();
                } catch (final Exception e) {
//                    if (null != transaction && transaction.isActive()) {
//                        transaction.rollback();
//                    }

                    LOGGER.error( "Updates link failed", e);
                }
            }
        }
    }
}
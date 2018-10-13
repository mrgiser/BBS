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

import cn.he.zhao.bbs.entity.DomainTag;
import cn.he.zhao.bbs.entityUtil.DomainUtil;
import cn.he.zhao.bbs.entityUtil.TagUtil;
import cn.he.zhao.bbs.mapper.DomainMapper;
import cn.he.zhao.bbs.mapper.DomainTagMapper;
import cn.he.zhao.bbs.mapper.TagMapper;
import cn.he.zhao.bbs.entity.Domain;
import cn.he.zhao.bbs.entity.Tag;
import cn.he.zhao.bbs.entityUtil.my.CollectionUtils;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.Pagination;
import cn.he.zhao.bbs.spring.Paginator;
import cn.he.zhao.bbs.util.Markdowns;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Domain query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Apr 1, 2018
 * @since 1.4.0
 */
@Service
public class DomainQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DomainQueryService.class);

    /**
     * Domain Mapper.
     */
    @Autowired
    private DomainMapper domainMapper;

    /**
     * Tag Mapper.
     */
    @Autowired
    private TagMapper tagMapper;

    /**
     * Domain tag Mapper.
     */
    @Autowired
    private DomainTagMapper domainTagMapper;

    /**
     * Short link query service.
     */
    @Autowired
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Gets all domains.
     *
     * @return domains, returns an empty list if not found
     */
    public List<Domain> getAllDomains() {
//        final Query query = new Query().
//                addSort(DomainUtil.DOMAIN_SORT, SortDirection.ASCENDING).
//                addSort(DomainUtil.DOMAIN_TAG_COUNT, SortDirection.DESCENDING).
//                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
//                setPageSize(Integer.MAX_VALUE).setPageCount(1);

        PageHelper.startPage(1,Integer.MAX_VALUE);
        try {
            final List<Domain> ret = domainMapper.getAllByOrder();
            for (final Domain domain : ret) {
                final List<Tag> tags = getTags(domain.getOid());

                domain.setDomainTags( (Object) tags);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets all domains failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets most tag navigation domains.
     *
     * @param fetchSize the specified fetch size
     * @return domains, returns an empty list if not found
     */
    public List<Domain> getMostTagNaviDomains(final int fetchSize) {
//        final Query query = new Query().
//                setFilter(new PropertyFilter(DomainUtil.DOMAIN_NAV, FilterOperator.EQUAL, DomainUtil.DOMAIN_NAV_C_ENABLED)).
//                addSort(Domain.DOMAIN_SORT, SortDirection.ASCENDING).
//                addSort(Domain.DOMAIN_TAG_COUNT, SortDirection.DESCENDING).
//                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
//                setPageSize(fetchSize).setPageCount(1);

        PageHelper.startPage(1,fetchSize);
        try {
            final List<Domain> ret = domainMapper.getByDomainNav(DomainUtil.DOMAIN_NAV_C_ENABLED);
            for (final Domain domain : ret) {
                final List<Tag> tags = getTags(domain.getOid());

                domain.setDomainTags((Object) tags);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets most tag navigation domains failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets a domain's tags.
     *
     * @param domainId the specified domain id
     * @return tags, returns an empty list if not found
     */
    public List<Tag> getTags(final String domainId) {
        final List<Tag> ret = new ArrayList<>();

//        final Query query = new Query().
//                setFilter(new PropertyFilter(Domain.DOMAIN + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, domainId));
        try {
//            final List<JSONObject> relations = CollectionUtils.jsonArrayToList(
//                    domainTagMapper.get(query).optJSONArray(Keys.RESULTS));

            final List<DomainTag> relations = domainTagMapper.getByDomainId(domainId);

            for (final DomainTag relation : relations) {
                final String tagId = relation.getOid();
                final Tag tag = tagMapper.get(tagId);

                ret.add(tag);
            }
        } catch (final Exception e) {
            LOGGER.error( "Gets domain [id=" + domainId + "] tags error", e);
        }

        return ret;
    }

    /**
     * Gets a domain by the specified domain URI.
     *
     * @param domainURI the specified domain URI
     * @return domain, returns {@code null} if not null
     * @throws Exception service exception
     */
    public Domain getByURI(final String domainURI) throws Exception {
        try {
            final Domain ret = domainMapper.getByDomainURI(domainURI);
            if (null == ret) {
                return null;
            }

            if (DomainUtil.DOMAIN_STATUS_C_VALID != ret.getDomainStatus()) {
                return null;
            }

            String description = ret.getDomainDescription();
            String descriptionText = ret.getDomainTitle();
            if (StringUtils.isNotBlank(description)) {
                description = shortLinkQueryService.linkTag(description);
                description = Markdowns.toHTML(description);

                ret.setDomainDescription(description);
                descriptionText = Jsoup.parse(description).text();
            }

            final String domainTitle = ret.getDomainTitle();

            if (StringUtils.isBlank(ret.getDomainSeoTitle())) {
                ret.setDomainSeoTitle(domainTitle);
            }

            if (StringUtils.isBlank(ret.getDomainSeoDesc())) {
                ret.setDomainSeoDesc(descriptionText);
            }

            if (StringUtils.isBlank(ret.getDomainSeoKeywords())) {
                ret.setDomainSeoKeywords(domainTitle);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets domain [URI=" + domainURI + "] failed", e);

            throw new Exception(e);
        }
    }

    /**
     * Gets a domain by the specified domain title.
     *
     * @param domainTitle the specified domain title
     * @return domain, returns {@code null} if not null
     * @throws Exception service exception
     */
    public Domain getByTitle(final String domainTitle) throws Exception {
        try {
            final Domain ret = domainMapper.getByTitle(domainTitle);
            if (null == ret) {
                return null;
            }

            if (DomainUtil.DOMAIN_STATUS_C_VALID != ret.getDomainStatus()) {
                return null;
            }

            String description = ret.getDomainDescription();
            String descriptionText = ret.getDomainTitle();
            if (StringUtils.isNotBlank(description)) {
                description = shortLinkQueryService.linkTag(description);
                description = Markdowns.toHTML(description);

                ret.setDomainDescription( description);
                descriptionText = Jsoup.parse(description).text();
            }

            if (StringUtils.isBlank(ret.getDomainSeoTitle())) {
                ret.setDomainSeoTitle( domainTitle);
            }

            if (StringUtils.isBlank(ret.getDomainSeoDesc())) {
                ret.setDomainSeoDesc( descriptionText);
            }

            if (StringUtils.isBlank(ret.getDomainSeoKeywords())) {
                ret.setDomainSeoKeywords( domainTitle);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets domain [title=" + domainTitle + "] failed", e);

            throw new Exception(e);
        }
    }

    /**
     * Gets domains by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          {
     *                          "domainTitle": "", // optional
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10
     *                          }, see {@link Pagination} for more details
     * @param domainFields      the specified domain fields to return
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "domains": [{
     *         "oId": "",
     *         "domainTitle": "",
     *         "domainDescription": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws Exception service exception
     * @see Pagination
     */
    public JSONObject getDomains(final JSONObject requestJSONObject, final Map<String, Class<?>> domainFields) throws Exception {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);

        PageHelper.startPage(currentPageNum,pageSize);
//        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
//                addSort(Domain.DOMAIN_SORT, SortDirection.ASCENDING).
//                addSort(Domain.DOMAIN_TAG_COUNT, SortDirection.DESCENDING).
//                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
//        for (final Map.Entry<String, Class<?>> field : domainFields.entrySet()) {
//            query.addProjection(field.getKey(), field.getValue());
//        }

//        if (requestJSONObject.has(DomainUtil.DOMAIN_TITLE)) {
//            query.setFilter(new PropertyFilter(DomainUtil.DOMAIN_TITLE, FilterOperator.EQUAL, requestJSONObject.optString(DomainUtil.DOMAIN_TITLE)));

//        }

        PageInfo<Domain> result;
        try{
            if (requestJSONObject.has(DomainUtil.DOMAIN_TITLE)) {
                result = new PageInfo<> (domainMapper.getByDomainTitle(requestJSONObject.optString(DomainUtil.DOMAIN_TITLE)));
            } else {
                result = new PageInfo<>(domainMapper.getAllByOrder());
            }

        } catch (final Exception e) {
            LOGGER.error( "Gets domains failed", e);

            throw new Exception(e);
        }

//        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);
        final int pageCount = result.getPages();

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final List<Domain> domains = result.getList();
//        final List<JSONObject> domains = CollectionUtils.jsonArrayToList(data);

        ret.put(DomainUtil.DOMAINS, domains);

        return ret;
    }

    /**
     * Gets a domain by the specified id.
     *
     * @param domainId the specified id
     * @return a domain, return {@code null} if not found
     * @throws Exception service exception
     */
    public Domain getDomain(final String domainId) throws Exception {
        try {
            final Domain ret = domainMapper.getByOId(domainId);
            final List<Tag> tags = getTags(domainId);
            ret.setDomainTags((Object) tags);

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets a domain [domainId=" + domainId + "] failed", e);

            throw new Exception(e);
        }
    }

    /**
     * Whether a tag specified by the given tag title in a domain specified by the given domain id.
     *
     * @param tagTitle the given tag title
     * @param domainId the given domain id
     * @return {@code true} if the tag in the domain, returns {@code false} otherwise
     */
    public boolean containTag(final String tagTitle, final String domainId) {
        try {
            final Domain domain = domainMapper.getByOId(domainId);
            if (null == domain) {
                return true;
            }

            final Tag tag = tagMapper.getByTitle(tagTitle);
            if (null == tag) {
                return true;
            }

//            final Query query = new Query().setFilter(
//                    CompositeFilterOperator.and(
//                            new PropertyFilter(DomainUtil.DOMAIN + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, domainId),
//                            new PropertyFilter(TagUtil.TAG + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.getOid())));

            return domainTagMapper.countByDomain_oIdAndTag_oId(domainId, tag.getOid()) > 0;
        } catch (final Exception e) {
            LOGGER.error( "Check domain tag [tagTitle=" + tagTitle + ", domainId=" + domainId + "] failed", e);

            return true;
        }
    }
}

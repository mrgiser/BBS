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

import cn.he.zhao.bbs.cache.DomainCache;
import cn.he.zhao.bbs.mapper.DomainMapper;
import cn.he.zhao.bbs.mapper.DomainTagMapper;
import cn.he.zhao.bbs.mapper.OptionMapper;
import cn.he.zhao.bbs.model.Domain;
import cn.he.zhao.bbs.model.Option;
import cn.he.zhao.bbs.model.my.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

/**
 * Domain management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.3.3, Mar 30, 2018
 * @since 1.4.0
 */
@Service
public class DomainMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DomainMgmtService.class);

    /**
     * Domain Mapper.
     */
    @Autowired
    private DomainMapper domainMapper;

    /**
     * Domain tag Mapper.
     */
    @Autowired
    private DomainTagMapper domainTagMapper;

    /**
     * Option Mapper.
     */
    @Autowired
    private OptionMapper optionMapper;

    /**
     * Domain cache.
     */
    @Autowired
    private DomainCache domainCache;

    /**
     * Removes a domain-tag relation.
     *
     * @param domainId the specified domain id
     * @param tagId the specified tag id
     * @throws ServiceException service exception
     */
    @Transactional
    public void removeDomainTag(final String domainId, final String tagId) throws ServiceException {
        try {
            final JSONObject domain = domainMapper.get(domainId);
            domain.put(Domain.DOMAIN_TAG_COUNT, domain.optInt(Domain.DOMAIN_TAG_COUNT) - 1);

            domainMapper.update(domainId, domain);

            final Query query = new Query().setFilter(
                    CompositeFilterOperator.and(
                            new PropertyFilter(Domain.DOMAIN + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, domainId),
                            new PropertyFilter(Tag.TAG + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, tagId)));

            final JSONArray relations = domainTagMapper.get(query).optJSONArray(Keys.RESULTS);
            if (relations.length() < 1) {
                return;
            }

            final JSONObject relation = relations.optJSONObject(0);
            domainTagMapper.remove(relation.optString(Keys.OBJECT_ID));

            // Refresh cache
            domainCache.loadDomains();
        } catch (final MapperException e) {
            LOGGER.error( "Adds a domain-tag relation failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Adds a domain-tag relation.
     *
     * @param domainTag the specified domain-tag relation
     * @throws ServiceException service exception
     */
    @Transactional
    public void addDomainTag(final JSONObject domainTag) throws ServiceException {
        try {
            final String domainId = domainTag.optString(Domain.DOMAIN + "_" + Keys.OBJECT_ID);
            final JSONObject domain = domainMapper.get(domainId);
            domain.put(Domain.DOMAIN_TAG_COUNT, domain.optInt(Domain.DOMAIN_TAG_COUNT) + 1);

            domainMapper.update(domainId, domain);
            domainTagMapper.add(domainTag);

            // Refresh cache
            domainCache.loadDomains();
        } catch (final MapperException e) {
            LOGGER.error( "Adds a domain-tag relation failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Adds a domain relation.
     *
     * @param domain the specified domain relation
     * @return domain id
     * @throws ServiceException service exception
     */
    @Transactional
    public String addDomain(final JSONObject domain) throws ServiceException {
        try {
            final JSONObject record = new JSONObject();
            record.put(Domain.DOMAIN_CSS, domain.optString(Domain.DOMAIN_CSS));
            record.put(Domain.DOMAIN_DESCRIPTION, domain.optString(Domain.DOMAIN_DESCRIPTION));
            record.put(Domain.DOMAIN_ICON_PATH, domain.optString(Domain.DOMAIN_ICON_PATH));
            record.put(Domain.DOMAIN_SEO_DESC, domain.optString(Domain.DOMAIN_SEO_DESC));
            record.put(Domain.DOMAIN_SEO_KEYWORDS, domain.optString(Domain.DOMAIN_SEO_KEYWORDS));
            record.put(Domain.DOMAIN_SEO_TITLE, domain.optString(Domain.DOMAIN_SEO_TITLE));
            record.put(Domain.DOMAIN_STATUS, domain.optInt(Domain.DOMAIN_STATUS));
            record.put(Domain.DOMAIN_TITLE, domain.optString(Domain.DOMAIN_TITLE));
            record.put(Domain.DOMAIN_URI, domain.optString(Domain.DOMAIN_URI));
            record.put(Domain.DOMAIN_TAG_COUNT, 0);
            record.put(Domain.DOMAIN_TYPE, "");
            record.put(Domain.DOMAIN_SORT, 10);
            record.put(Domain.DOMAIN_NAV, Domain.DOMAIN_NAV_C_ENABLED);

            final JSONObject domainCntOption = optionMapper.get(Option.ID_C_STATISTIC_DOMAIN_COUNT);
            final int domainCnt = domainCntOption.optInt(Option.OPTION_VALUE);
            domainCntOption.put(Option.OPTION_VALUE, domainCnt + 1);
            optionMapper.update(Option.ID_C_STATISTIC_DOMAIN_COUNT, domainCntOption);

            final String ret = domainMapper.add(record);

            // Refresh cache
            domainCache.loadDomains();

            return ret;
        } catch (final MapperException e) {
            LOGGER.error( "Adds a domain failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Updates the specified domain by the given domain id.
     *
     * @param domainId the given domain id
     * @param domain the specified domain
     * @throws ServiceException service exception
     */
    @Transactional
    public void updateDomain(final String domainId, final JSONObject domain) throws ServiceException {
        try {
            domainMapper.update(domainId, domain);

            // Refresh cache
            domainCache.loadDomains();
        } catch (final MapperException e) {
            LOGGER.error( "Updates a domain [id=" + domainId + "] failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Removes the specified domain by the given domain id.
     *
     * @param domainId the given domain id
     * @throws ServiceException service exception
     */
    @Transactional
    public void removeDomain(final String domainId) throws ServiceException {
        try {
            domainTagMapper.removeByDomainId(domainId);
            domainMapper.remove(domainId);

            final JSONObject domainCntOption = optionMapper.get(Option.ID_C_STATISTIC_DOMAIN_COUNT);
            final int domainCnt = domainCntOption.optInt(Option.OPTION_VALUE);
            domainCntOption.put(Option.OPTION_VALUE, domainCnt - 1);
            optionMapper.update(Option.ID_C_STATISTIC_DOMAIN_COUNT, domainCntOption);

            // Refresh cache
            domainCache.loadDomains();
        } catch (final MapperException e) {
            LOGGER.error( "Updates a domain [id=" + domainId + "] failed", e);

            throw new ServiceException(e);
        }
    }
}

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
import cn.he.zhao.bbs.entity.DomainTag;
import cn.he.zhao.bbs.entityUtil.DomainUtil;
import cn.he.zhao.bbs.entityUtil.OptionUtil;
import cn.he.zhao.bbs.mapper.DomainMapper;
import cn.he.zhao.bbs.mapper.DomainTagMapper;
import cn.he.zhao.bbs.mapper.OptionMapper;
import cn.he.zhao.bbs.entity.Domain;
import cn.he.zhao.bbs.entity.Option;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
     * OptionUtil Mapper.
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
     * @throws Exception service exception
     */
    @Transactional
    public void removeDomainTag(final String domainId, final String tagId) throws Exception {
        try {
            final Domain domain = domainMapper.getByOId(domainId);
            domain.setDomainTagCnt(domain.getDomainTagCnt() - 1);

            domainMapper.update( domain);

//            final Query query = new Query().setFilter(
//                    CompositeFilterOperator.and(
//                            new PropertyFilter(Domain.DOMAIN + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, domainId),
//                            new PropertyFilter(Tag.TAG + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, tagId)));

            final List<DomainTag> relations = domainTagMapper.getByDomain_oIdAndTag_oId(domainId, tagId);
            if (relations.size() < 1) {
                return;
            }

//            final JSONObject relation = relations.optJSONObject(0);
            domainTagMapper.remove(relations.get(0).getOid());

            // Refresh cache
            domainCache.loadDomains();
        } catch (final Exception e) {
            LOGGER.error( "Adds a domain-tag relation failed", e);

            throw new Exception(e);
        }
    }

    /**
     * Adds a domain-tag relation.
     *
     * @param domainTag the specified domain-tag relation
     * @throws Exception service exception
     */
    @Transactional
    public void addDomainTag(final DomainTag domainTag) throws Exception {
        try {
            final String domainId = domainTag.getDomain_oId();
            final Domain domain = domainMapper.getByOId(domainId);
            domain.setDomainTagCnt( domain.getDomainTagCnt() + 1);

            domainMapper.update( domain);
            domainTagMapper.add(domainTag);

            // Refresh cache
            domainCache.loadDomains();
        } catch (final Exception e) {
            LOGGER.error( "Adds a domain-tag relation failed", e);

            throw new Exception(e);
        }
    }

    /**
     * Adds a domain relation.
     *
     * @param domain the specified domain relation
     * @return domain id
     * @throws Exception service exception
     */
    @Transactional
    public String addDomain(final JSONObject domain) throws Exception {
        try {
            final Domain record = new Domain();
            record.setDomainCSS(domain.optString(DomainUtil.DOMAIN_CSS));
            record.setDomainDescription( domain.optString(DomainUtil.DOMAIN_DESCRIPTION));
            record.setDomainIconPath( domain.optString(DomainUtil.DOMAIN_ICON_PATH));
            record.setDomainSeoDesc(domain.optString(DomainUtil.DOMAIN_SEO_DESC));
            record.setDomainSeoKeywords(domain.optString(DomainUtil.DOMAIN_SEO_KEYWORDS));
            record.setDomainSeoTitle(domain.optString(DomainUtil.DOMAIN_SEO_TITLE));
            record.setDomainStatus(domain.optInt(DomainUtil.DOMAIN_STATUS));
            record.setDomainTitle(domain.optString(DomainUtil.DOMAIN_TITLE));
            record.setDomainURI(domain.optString(DomainUtil.DOMAIN_URI));
            record.setDomainTagCnt( 0);
            record.setDomainType("");
            record.setDomainSort( 10);
            record.setDomainNav(DomainUtil.DOMAIN_NAV_C_ENABLED);

            final Option domainCntOption = optionMapper.get(OptionUtil.ID_C_STATISTIC_DOMAIN_COUNT);
            final int domainCnt = Integer.parseInt(domainCntOption.getOptionValue());
            domainCntOption.setOptionValue(String.valueOf(domainCnt + 1));
            optionMapper.update( domainCntOption);

            final String ret = domainMapper.add(record);

            // Refresh cache
            domainCache.loadDomains();

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Adds a domain failed", e);

            throw new Exception(e);
        }
    }

    /**
     * Updates the specified domain by the given domain id.
     *
     * @param domainId the given domain id
     * @param domain the specified domain
     * @throws Exception service exception
     */
    @Transactional
    public void updateDomain(final String domainId, final Domain domain) throws Exception {
        try {
            domainMapper.update(domain);

            // Refresh cache
            domainCache.loadDomains();
        } catch (final Exception e) {
            LOGGER.error( "Updates a domain [id=" + domainId + "] failed", e);

            throw new Exception(e);
        }
    }

    /**
     * Removes the specified domain by the given domain id.
     *
     * @param domainId the given domain id
     * @throws Exception service exception
     */
    @Transactional
    public void removeDomain(final String domainId) throws Exception {
        try {
            domainTagMapper.removeByDomainId(domainId);
            domainMapper.remove(domainId);

            final Option domainCntOption = optionMapper.get(OptionUtil.ID_C_STATISTIC_DOMAIN_COUNT);
            final String domainCnt = domainCntOption.getOptionValue();
            int newcnt = Integer.parseInt(domainCnt) -1;
            domainCntOption.setOptionValue(String.valueOf(newcnt));
            optionMapper.update( domainCntOption);

            // Refresh cache
            domainCache.loadDomains();
        } catch (final Exception e) {
            LOGGER.error( "Updates a domain [id=" + domainId + "] failed", e);

            throw new Exception(e);
        }
    }
}

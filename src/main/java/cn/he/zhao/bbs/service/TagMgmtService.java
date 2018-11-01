package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.cache.DomainCache;
import cn.he.zhao.bbs.cache.TagCache;
import cn.he.zhao.bbs.entityUtil.OptionUtil;
import cn.he.zhao.bbs.entityUtil.TagUtil;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Common;
import cn.he.zhao.bbs.util.URLs;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Tag management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.1.6, Apr 16, 2017
 * @since 1.1.0
 */
@Service
public class TagMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TagMgmtService.class);

    /**
     * OptionUtil Mapper.
     */
    @Autowired
    private OptionMapper optionMapper;

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * Tag Mapper.
     */
    @Autowired
    private TagMapper tagMapper;

    /**
     * Tag-Tag Mapper.
     */
    @Autowired
    private TagTagMapper tagTagMapper;

    /**
     * User-Tag Mapper.
     */
    @Autowired
    private UserTagMapper userTagMapper;

    /**
     * Domain-Tag Mapper.
     */
    @Autowired
    private DomainTagMapper domainTagMapper;

    /**
     * Tag-User-LinkUtil Mapper.
     */
    @Autowired
    private TagUserLinkMapper tagUserLinkMapper;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Domain cache.
     */
    @Autowired
    private DomainCache domainCache;

    /**
     * Tag cache.
     */
    @Autowired
    private TagCache tagCache;

    /**
     * Removes unused tags.
     */
    @Transactional
    public synchronized void removeUnusedTags() {
        LOGGER.info("Starting remove unused tags....");

        int removedCnt = 0;
        try {
            final List<Tag> tags = tagMapper.getALL();

            for (int i = 0; i < tags.size(); i++) {
                final Tag tag = tags.get(i);
                final String tagId = tag.getOid();

                if (0 == tag.getTagReferenceCount() // article ref cnt
                        && 0 == domainTagMapper.getByTagOId(tagId, 1, Integer.MAX_VALUE)
                        .size() // domainTagRefCnt
                        && 0 == tagUserLinkMapper.countTagLink(tagId) // tagUserLinkRefCnt
                        ) {
                    final List<UserTag> userTagRels = userTagMapper.getByTagId(tagId, 1, Integer.MAX_VALUE);
                    if (1 == userTagRels.size()
                            && TagUtil.TAG_TYPE_C_CREATOR == userTagRels.get(0).getType()) {
                        // Just the tag's creator but not use it now
                        tagMapper.remove(tagId);
                        removedCnt++;

                        LOGGER.info("Removed a unused tag [title=" + tag.getTagTitle() + "]");
                    }
                }
            }

            final Option tagCntOption = optionMapper.get(OptionUtil.ID_C_STATISTIC_TAG_COUNT);
            final int tagCnt = Integer.getInteger(tagCntOption.getOptionValue());
            tagCntOption.setOptionValue(String.valueOf(tagCnt - removedCnt));
            optionMapper.update(OptionUtil.ID_C_STATISTIC_TAG_COUNT, tagCntOption);

            LOGGER.info("Removed [" + removedCnt + "] unused tags");
        } catch (final Exception e) {
            LOGGER.error( "Removes unused tags failed", e);
        }
    }

    /**
     * Adds a tag.
     * <p>
     * <b>Note</b>: This method just for admin console.
     * </p>
     *
     * @param userId   the specified user id
     * @param tagTitle the specified tag title
     * @return tag id
     * @throws Exception service exception
     */
    public String addTag(final String userId, final String tagTitle) throws Exception {
        String ret;

//        final Transaction transaction = tagMapper.beginTransaction();

        try {
            if (null != tagMapper.getByTitle(tagTitle)) {
                throw new Exception(langPropsService.get("tagExistLabel"));
            }

            final UserExt author = userMapper.get(userId);

            Tag tag = new Tag();
            tag.setTagTitle(tagTitle);
            String tagURI = tagTitle;
            tagURI = URLs.encode(tagTitle);
            tag.setTagURI(tagURI);
            tag.setTagCSS( "");
            tag.setTagReferenceCount(0);
            tag.setTagCommentCount(0);
            tag.setTagFollowerCount( 0);
            tag.setTagLinkCount(0);
            tag.setTagDescription( "");
            tag.setTagIconPath("");
            tag.setTagStatus( 0);
            tag.setTagGoodCnt( 0);
            tag.setTagBadCnt( 0);
            tag.setTagSeoTitle( tagTitle);
            tag.setTagSeoKeywords(tagTitle);
            tag.setTagSeoDesc("");
            tag.setTagRandomDouble(Math.random());

            ret = tagMapper.add(tag);
            tag.setOid(ret);

            final Option tagCntOption = optionMapper.get(OptionUtil.ID_C_STATISTIC_TAG_COUNT);
            final int tagCnt = Integer.getInteger(tagCntOption.getOptionValue());
            tagCntOption.setOptionValue(String.valueOf( tagCnt + 1));
            optionMapper.update(OptionUtil.ID_C_STATISTIC_TAG_COUNT, tagCntOption);

            author.setUserTagCount(author.getUserTagCount() + 1);
            userMapper.update(userId, author);

            // User-Tag relation
            final UserTag userTagRelation = new UserTag();
            userTagRelation.setTag_oId(ret);
            userTagRelation.setUser_oId(userId);
            userTagRelation.setType(TagUtil.TAG_TYPE_C_CREATOR);
            userTagMapper.add(userTagRelation);

//            transaction.commit();

            tagCache.loadAllTags();
            domainCache.loadDomains();

            return ret;
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error( "Adds tag failed", e);

            throw new Exception(e.getMessage());
        }
    }

    /**
     * Updates the specified tag by the given tag id.
     * <p>
     * <b>Note</b>: This method just for admin console.
     * </p>
     *
     * @param tagId the given tag id
     * @param tag   the specified tag
     * @throws Exception service exception
     */
    public void updateTag(final String tagId, final Tag tag) throws Exception {
//        final Transaction transaction = tagMapper.beginTransaction();

        try {
            tag.setTagRandomDouble( Math.random());

            tagMapper.update(tagId, tag);

//            transaction.commit();

            tagCache.loadTags();

            domainCache.loadDomains();
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error( "Updates a tag[id=" + tagId + "] failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Adds a tag-tag relation.
     *
     * @param tagRelation the specified tag-tag relation
     * @throws Exception service exception
     */
    void addTagRelation(final TagTag tagRelation) throws Exception {
//        final Transaction transaction = tagTagMapper.beginTransaction();

        try {
            tagTagMapper.add(tagRelation);

//            transaction.commit();
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error( "Adds a tag-tag failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Updates the specified tag-tag relation by the given tag relation id.
     *
     * @param tagRelationId the given tag relation id
     * @param tagRelation   the specified tag-tag relation
     * @throws Exception service exception
     */
    void updateTagRelation(final String tagRelationId, final TagTag tagRelation) throws Exception {
//        final Transaction transaction = tagTagMapper.beginTransaction();

        try {
            tagTagMapper.update(tagRelationId, tagRelation);

//            transaction.commit();
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error( "Updates a tag-tag relation [id=" + tagRelationId + "] failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Relates the specified tag string.
     *
     * @param tagString the specified tag string
     * @throws Exception service exception
     */
    public void relateTags(final String tagString) throws Exception {
        final List<Tag> tags = new ArrayList<>();

        try {
            final String[] tagTitles = tagString.split(",");
            for (final String tagTitle : tagTitles) {
                final Tag tag = tagMapper.getByTitle(tagTitle.trim());

                if (null != tag) {
                    tags.add(tag);
                }
            }

            for (int i = 0; i < tags.size(); i++) {
                final Tag tag1 = tags.get(i);
                final String tag1Id = tag1.getOid();

                for (int j = i + 1; j < tags.size(); j++) {
                    final Tag tag2 = tags.get(j);
                    final String tag2Id = tag2.getOid();

                    TagTag relation = tagTagMapper.getByTag1IdAndTag2Id(tag1Id, tag2Id);
                    if (null != relation) {
                        relation.setWeight(relation.getWeight() + 1);

                        updateTagRelation(relation.getOid(), relation);

                        continue;
                    }

                    relation = tagTagMapper.getByTag1IdAndTag2Id(tag2Id, tag1Id);
                    if (null != relation) {
                        relation.setWeight(relation.getWeight() + 1);

                        updateTagRelation(relation.getOid(), relation);

                        continue;
                    }

                    relation = new TagTag();
                    relation.setTag1_oId(tag1Id);
                    relation.setTag2_oId(tag2Id);
                    relation.setWeight(1);

                    addTagRelation(relation);
                }
            }
        } catch (final Exception e) {
            LOGGER.error( "Relates tag and tag [" + tagString + "] failed", e);
            throw new Exception(e);
        }
    }
}

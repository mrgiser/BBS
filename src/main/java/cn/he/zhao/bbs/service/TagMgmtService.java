package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            final JSONArray tags = tagMapper.get(new Query()).optJSONArray(Keys.RESULTS);

            for (int i = 0; i < tags.length(); i++) {
                final JSONObject tag = tags.optJSONObject(i);
                final String tagId = tag.optString(Keys.OBJECT_ID);

                if (0 == tag.optInt(Tag.TAG_REFERENCE_CNT) // article ref cnt
                        && 0 == domainTagMapper.getByTagId(tagId, 1, Integer.MAX_VALUE)
                        .optJSONArray(Keys.RESULTS).length() // domainTagRefCnt
                        && 0 == tagUserLinkMapper.countTagLink(tagId) // tagUserLinkRefCnt
                        ) {
                    final JSONArray userTagRels = userTagMapper.getByTagId(tagId, 1, Integer.MAX_VALUE)
                            .optJSONArray(Keys.RESULTS);
                    if (1 == userTagRels.length()
                            && Tag.TAG_TYPE_C_CREATOR == userTagRels.optJSONObject(0).optInt(Common.TYPE)) {
                        // Just the tag's creator but not use it now
                        tagMapper.remove(tagId);
                        removedCnt++;

                        LOGGER.info("Removed a unused tag [title=" + tag.optString(Tag.TAG_TITLE) + "]");
                    }
                }
            }

            final JSONObject tagCntOption = optionMapper.get(Option.ID_C_STATISTIC_TAG_COUNT);
            final int tagCnt = tagCntOption.optInt(Option.OPTION_VALUE);
            tagCntOption.put(Option.OPTION_VALUE, tagCnt - removedCnt);
            optionMapper.update(Option.ID_C_STATISTIC_TAG_COUNT, tagCntOption);

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
     * @throws ServiceException service exception
     */
    public String addTag(final String userId, final String tagTitle) throws ServiceException {
        String ret;

        final Transaction transaction = tagMapper.beginTransaction();

        try {
            if (null != tagMapper.getByTitle(tagTitle)) {
                throw new ServiceException(langPropsService.get("tagExistLabel"));
            }

            final JSONObject author = userMapper.get(userId);

            JSONObject tag = new JSONObject();
            tag.put(Tag.TAG_TITLE, tagTitle);
            String tagURI = tagTitle;
            tagURI = URLs.encode(tagTitle);
            tag.put(Tag.TAG_URI, tagURI);
            tag.put(Tag.TAG_CSS, "");
            tag.put(Tag.TAG_REFERENCE_CNT, 0);
            tag.put(Tag.TAG_COMMENT_CNT, 0);
            tag.put(Tag.TAG_FOLLOWER_CNT, 0);
            tag.put(Tag.TAG_LINK_CNT, 0);
            tag.put(Tag.TAG_DESCRIPTION, "");
            tag.put(Tag.TAG_ICON_PATH, "");
            tag.put(Tag.TAG_STATUS, 0);
            tag.put(Tag.TAG_GOOD_CNT, 0);
            tag.put(Tag.TAG_BAD_CNT, 0);
            tag.put(Tag.TAG_SEO_TITLE, tagTitle);
            tag.put(Tag.TAG_SEO_KEYWORDS, tagTitle);
            tag.put(Tag.TAG_SEO_DESC, "");
            tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

            ret = tagMapper.add(tag);
            tag.put(Keys.OBJECT_ID, ret);

            final JSONObject tagCntOption = optionMapper.get(Option.ID_C_STATISTIC_TAG_COUNT);
            final int tagCnt = tagCntOption.optInt(Option.OPTION_VALUE);
            tagCntOption.put(Option.OPTION_VALUE, tagCnt + 1);
            optionMapper.update(Option.ID_C_STATISTIC_TAG_COUNT, tagCntOption);

            author.put(UserExt.USER_TAG_COUNT, author.optInt(UserExt.USER_TAG_COUNT) + 1);
            userMapper.update(userId, author);

            // User-Tag relation
            final JSONObject userTagRelation = new JSONObject();
            userTagRelation.put(Tag.TAG + '_' + Keys.OBJECT_ID, ret);
            userTagRelation.put(User.USER + '_' + Keys.OBJECT_ID, userId);
            userTagRelation.put(Common.TYPE, Tag.TAG_TYPE_C_CREATOR);
            userTagMapper.add(userTagRelation);

            transaction.commit();

            tagCache.loadAllTags();
            domainCache.loadDomains();

            return ret;
        } catch (final MapperException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.error( "Adds tag failed", e);

            throw new ServiceException(e.getMessage());
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
     * @throws ServiceException service exception
     */
    public void updateTag(final String tagId, final JSONObject tag) throws ServiceException {
        final Transaction transaction = tagMapper.beginTransaction();

        try {
            tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

            tagMapper.update(tagId, tag);

            transaction.commit();

            tagCache.loadTags();

            domainCache.loadDomains();
        } catch (final MapperException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.error( "Updates a tag[id=" + tagId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Adds a tag-tag relation.
     *
     * @param tagRelation the specified tag-tag relation
     * @throws ServiceException service exception
     */
    void addTagRelation(final JSONObject tagRelation) throws ServiceException {
        final Transaction transaction = tagTagMapper.beginTransaction();

        try {
            tagTagMapper.add(tagRelation);

            transaction.commit();
        } catch (final MapperException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.error( "Adds a tag-tag failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Updates the specified tag-tag relation by the given tag relation id.
     *
     * @param tagRelationId the given tag relation id
     * @param tagRelation   the specified tag-tag relation
     * @throws ServiceException service exception
     */
    void updateTagRelation(final String tagRelationId, final JSONObject tagRelation) throws ServiceException {
        final Transaction transaction = tagTagMapper.beginTransaction();

        try {
            tagTagMapper.update(tagRelationId, tagRelation);

            transaction.commit();
        } catch (final MapperException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.error( "Updates a tag-tag relation [id=" + tagRelationId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Relates the specified tag string.
     *
     * @param tagString the specified tag string
     * @throws ServiceException service exception
     */
    public void relateTags(final String tagString) throws ServiceException {
        final List<JSONObject> tags = new ArrayList<>();

        try {
            final String[] tagTitles = tagString.split(",");
            for (final String tagTitle : tagTitles) {
                final JSONObject tag = tagMapper.getByTitle(tagTitle.trim());

                if (null != tag) {
                    tags.add(tag);
                }
            }

            for (int i = 0; i < tags.size(); i++) {
                final JSONObject tag1 = tags.get(i);
                final String tag1Id = tag1.optString(Keys.OBJECT_ID);

                for (int j = i + 1; j < tags.size(); j++) {
                    final JSONObject tag2 = tags.get(j);
                    final String tag2Id = tag2.optString(Keys.OBJECT_ID);

                    JSONObject relation = tagTagMapper.getByTag1IdAndTag2Id(tag1Id, tag2Id);
                    if (null != relation) {
                        relation.put(Common.WEIGHT, relation.optInt(Common.WEIGHT) + 1);

                        updateTagRelation(relation.optString(Keys.OBJECT_ID), relation);

                        continue;
                    }

                    relation = tagTagMapper.getByTag1IdAndTag2Id(tag2Id, tag1Id);
                    if (null != relation) {
                        relation.put(Common.WEIGHT, relation.optInt(Common.WEIGHT) + 1);

                        updateTagRelation(relation.optString(Keys.OBJECT_ID), relation);

                        continue;
                    }

                    relation = new JSONObject();
                    relation.put(Tag.TAG + "1_" + Keys.OBJECT_ID, tag1Id);
                    relation.put(Tag.TAG + "2_" + Keys.OBJECT_ID, tag2Id);
                    relation.put(Common.WEIGHT, 1);

                    addTagRelation(relation);
                }
            }
        } catch (final MapperException e) {
            LOGGER.error( "Relates tag and tag [" + tagString + "] failed", e);
            throw new ServiceException(e);
        }
    }
}

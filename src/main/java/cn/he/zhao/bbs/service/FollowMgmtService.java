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

import cn.he.zhao.bbs.entityUtil.FollowUtil;
import cn.he.zhao.bbs.entityUtil.TagUtil;
import cn.he.zhao.bbs.mapper.ArticleMapper;
import cn.he.zhao.bbs.mapper.FollowMapper;
import cn.he.zhao.bbs.mapper.TagMapper;
import cn.he.zhao.bbs.entity.Article;
import cn.he.zhao.bbs.entity.Follow;
import cn.he.zhao.bbs.entity.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

/**
 * FollowUtil management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.1.2, Jan 18, 2017
 * @since 0.2.5
 */
@Service
public class FollowMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FollowMgmtService.class);

    /**
     * FollowUtil Mapper.
     */
    @Autowired
    private FollowMapper followMapper;

    /**
     * Tag Mapper.
     */
    @Autowired
    private TagMapper tagMapper;

    /**
     * Article Mapper.
     */
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * The specified follower follows the specified following tag.
     *
     * @param followerId     the specified follower id
     * @param followingTagId the specified following tag id
     * @throws Exception service exception
     */
    @Transactional
    public void followTag(final String followerId, final String followingTagId) throws Exception {
        try {
            follow(followerId, followingTagId, FollowUtil.FOLLOWING_TYPE_C_TAG);
        } catch (final Exception e) {
            final String msg = "User[id=" + followerId + "] follows a tag[id=" + followingTagId + "] failed";
            LOGGER.error( msg, e);

            throw new Exception(msg);
        }
    }

    /**
     * The specified follower follows the specified following user.
     *
     * @param followerId      the specified follower id
     * @param followingUserId the specified following user id
     * @throws Exception service exception
     */
    @Transactional
    public void followUser(final String followerId, final String followingUserId) throws Exception {
        try {
            follow(followerId, followingUserId, FollowUtil.FOLLOWING_TYPE_C_USER);
        } catch (final Exception e) {
            final String msg = "User[id=" + followerId + "] follows a user[id=" + followingUserId + "] failed";
            LOGGER.error( msg, e);

            throw new Exception(msg);
        }
    }

    /**
     * The specified follower follows the specified following article.
     *
     * @param followerId         the specified follower id
     * @param followingArticleId the specified following article id
     * @throws Exception service exception
     */
    @Transactional
    public void followArticle(final String followerId, final String followingArticleId) throws Exception {
        try {
            follow(followerId, followingArticleId, FollowUtil.FOLLOWING_TYPE_C_ARTICLE);
        } catch (final Exception e) {
            final String msg = "User[id=" + followerId + "] follows an article[id=" + followingArticleId + "] failed";
            LOGGER.error( msg, e);

            throw new Exception(msg);
        }
    }

    /**
     * The specified follower watches the specified following article.
     *
     * @param followerId         the specified follower id
     * @param followingArticleId the specified following article id
     * @throws Exception service exception
     */
    @Transactional
    public void watchArticle(final String followerId, final String followingArticleId) throws Exception {
        try {
            follow(followerId, followingArticleId, FollowUtil.FOLLOWING_TYPE_C_ARTICLE_WATCH);
        } catch (final Exception e) {
            final String msg = "User[id=" + followerId + "] watches an article[id=" + followingArticleId + "] failed";
            LOGGER.error( msg, e);

            throw new Exception(msg);
        }
    }

    /**
     * The specified follower unfollows the specified following tag.
     *
     * @param followerId     the specified follower id
     * @param followingTagId the specified following tag id
     * @throws Exception service exception
     */
    @Transactional
    public void unfollowTag(final String followerId, final String followingTagId) throws Exception {
        try {
            unfollow(followerId, followingTagId, FollowUtil.FOLLOWING_TYPE_C_TAG);
        } catch (final Exception e) {
            final String msg = "User[id=" + followerId + "] unfollows a tag[id=" + followingTagId + "] failed";
            LOGGER.error( msg, e);

            throw new Exception(msg);
        }
    }

    /**
     * The specified follower unfollows the specified following user.
     *
     * @param followerId      the specified follower id
     * @param followingUserId the specified following user id
     * @throws Exception service exception
     */
    @Transactional
    public void unfollowUser(final String followerId, final String followingUserId) throws Exception {
        try {
            unfollow(followerId, followingUserId, FollowUtil.FOLLOWING_TYPE_C_USER);
        } catch (final Exception e) {
            final String msg = "User[id=" + followerId + "] unfollows a user[id=" + followingUserId + "] failed";
            LOGGER.error( msg, e);

            throw new Exception(msg);
        }
    }

    /**
     * The specified follower unfollows the specified following article.
     *
     * @param followerId         the specified follower id
     * @param followingArticleId the specified following article id
     * @throws Exception service exception
     */
    @Transactional
    public void unfollowArticle(final String followerId, final String followingArticleId) throws Exception {
        try {
            unfollow(followerId, followingArticleId, FollowUtil.FOLLOWING_TYPE_C_ARTICLE);
        } catch (final Exception e) {
            final String msg = "User[id=" + followerId + "] unfollows an article[id=" + followingArticleId + "] failed";
            LOGGER.error( msg, e);

            throw new Exception(msg);
        }
    }


    /**
     * The specified follower unwatches the specified following article.
     *
     * @param followerId         the specified follower id
     * @param followingArticleId the specified following article id
     * @throws Exception service exception
     */
    @Transactional
    public void unwatchArticle(final String followerId, final String followingArticleId) throws Exception {
        try {
            unfollow(followerId, followingArticleId, FollowUtil.FOLLOWING_TYPE_C_ARTICLE_WATCH);
        } catch (final Exception e) {
            final String msg = "User[id=" + followerId + "] unwatches an article[id=" + followingArticleId + "] failed";
            LOGGER.error( msg, e);

            throw new Exception(msg);
        }
    }

    /**
     * The specified follower follows the specified following entity with the specified following type.
     *
     * @param followerId    the specified follower id
     * @param followingId   the specified following entity id
     * @param followingType the specified following type
     * @throws Exception Mapper exception
     */
    private synchronized void follow(final String followerId, final String followingId, final int followingType) throws Exception {
        if (followMapper.exists(followerId, followingId, followingType)) {
            return;
        }

        if (FollowUtil.FOLLOWING_TYPE_C_TAG == followingType) {
            final Tag tag = tagMapper.get(followingId);
            if (null == tag) {
                LOGGER.error( "Not found tag [id={0}] to follow", followingId);

                return;
            }

            tag.setTagFollowerCount(tag.getTagFollowerCount() + 1);
            tag.setTagRandomDouble(Math.random());

            tagMapper.update(followingId, tag);
        } else if (FollowUtil.FOLLOWING_TYPE_C_ARTICLE == followingType) {
            final Article article = articleMapper.getByOid(followingId);
            if (null == article) {
                LOGGER.error( "Not found article [id={0}] to follow", followingId);

                return;
            }

            article.setArticleCollectCnt(article.getArticleCollectCnt() + 1);

            articleMapper.update( article);
        } else if (FollowUtil.FOLLOWING_TYPE_C_ARTICLE_WATCH == followingType) {
            final Article article = articleMapper.getByOid(followingId);
            if (null == article) {
                LOGGER.error( "Not found article [id={0}] to watch", followingId);

                return;
            }

            article.setArticleWatchCnt(article.getArticleWatchCnt() + 1);

            articleMapper.update( article);
        }

        final Follow follow = new Follow();
        follow.setFollowerId( followerId);
        follow.setFollowingId( followingId);
        follow.setFollowingType( followingType);

        followMapper.add(follow);
    }

    /**
     * Removes a follow relationship.
     *
     * @param followerId    the specified follower id
     * @param followingId   the specified following entity id
     * @param followingType the specified following type
     * @throws Exception Mapper exception
     */
    public synchronized void unfollow(final String followerId, final String followingId, final int followingType) throws Exception {
        followMapper.removeByFollowerIdAndFollowingId(followerId, followingId, followingType);

        if (FollowUtil.FOLLOWING_TYPE_C_TAG == followingType) {
            final Tag tag = tagMapper.get(followingId);
            if (null == tag) {
                LOGGER.error( "Not found tag [id={0}] to unfollow", followingId);

                return;
            }

            tag.setTagFollowerCount(tag.getTagFollowerCount() - 1);
            if (tag.getTagFollowerCount() < 0) {
                tag.setTagFollowerCount( 0);
            }

            tag.setTagRandomDouble( Math.random());

            tagMapper.update(followingId, tag);
        } else if (FollowUtil.FOLLOWING_TYPE_C_ARTICLE == followingType) {
            final Article article = articleMapper.getByOid(followingId);
            if (null == article) {
                LOGGER.error( "Not found article [id={0}] to unfollow", followingId);

                return;
            }

            article.setArticleCollectCnt( article.getArticleCollectCnt() - 1);
            if (article.getArticleCollectCnt() < 0) {
                article.setArticleCollectCnt( 0);
            }

            articleMapper.update( article);
        } else if (FollowUtil.FOLLOWING_TYPE_C_ARTICLE_WATCH == followingType) {
            final Article article = articleMapper.getByOid(followingId);
            if (null == article) {
                LOGGER.error( "Not found article [id={0}] to unwatch", followingId);

                return;
            }

            article.setArticleWatchCnt(article.getArticleWatchCnt() - 1);
            if (article.getArticleWatchCnt() < 0) {
                article.setArticleWatchCnt(0);
            }

            articleMapper.update( article);
        }
    }
}

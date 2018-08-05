package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.model.my.*;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.model.*;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Vote management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.1.0, Jul 31, 2016
 * @since 1.3.0
 */
@Service
public class VoteMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VoteMgmtService.class);

    /**
     * Vote Mapper.
     */
    @Autowired
    private VoteMapper voteMapper;

    /**
     * Article Mapper.
     */
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * Tag-Article Mapper.
     */
    @Autowired
    private TagArticleMapper tagArticleMapper;

    /**
     * Comment Mapper.
     */
    @Autowired
    private CommentMapper commentMapper;

    /**
     * Liveness management service.
     */
    @Autowired
    private LivenessMgmtService livenessMgmtService;

    /**
     * Gets Reddit article score.
     *
     * @param ups   the specified vote up count
     * @param downs the specified vote down count
     * @param t     time (epoch seconds)
     * @return reddit score
     */
    private static double redditArticleScore(final int ups, final int downs, final long t) {
        final int x = ups - downs;
        final double z = Math.max(Math.abs(x), 1);
        int y = 0;
        if (x > 0) {
            y = 1;
        } else if (x < 0) {
            y = -1;
        }

        return Math.log10(z) + y * (t - 1353745196) / 45000;
    }

    private static double redditCommentScore(final int ups, final int downs) {
        final int n = ups + downs;
        if (0 == n) {
            return 0;
        }

        final double z = 1.281551565545; // 1.0: 85%, 1.6: 95%, 1.281551565545: 80%
        final double p = (double) ups / n;

        return (p + z * z / (2 * n) - z * Math.sqrt((p * (1 - p) + z * z / (4 * n)) / n)) / (1 + z * z / n);
    }

    /**
     * Cancels the vote.
     *
     * @param userId   the specified user id
     * @param dataId   the specified data id
     * @param dataType the specified data type
     */
    @Transactional
    public void voteCancel(final String userId, final String dataId, final int dataType) {
        try {
            final int oldType = voteMapper.removeIfExists(userId, dataId, dataType);

            if (Vote.DATA_TYPE_C_ARTICLE == dataType) {
                final JSONObject article = articleMapper.get(dataId);
                if (null == article) {
                    LOGGER.error( "Not found article [id={0}] to vote cancel", dataId);

                    return;
                }

                if (Vote.TYPE_C_UP == oldType) {
                    article.put(Article.ARTICLE_GOOD_CNT, article.optInt(Article.ARTICLE_GOOD_CNT) - 1);
                } else if (Vote.TYPE_C_DOWN == oldType) {
                    article.put(Article.ARTICLE_BAD_CNT, article.optInt(Article.ARTICLE_BAD_CNT) - 1);
                }

                final int ups = article.optInt(Article.ARTICLE_GOOD_CNT);
                final int downs = article.optInt(Article.ARTICLE_BAD_CNT);
                final long t = article.optLong(Keys.OBJECT_ID) / 1000;

                final double redditScore = redditArticleScore(ups, downs, t);
                article.put(Article.REDDIT_SCORE, redditScore);

                updateTagArticleScore(article);

                articleMapper.update(dataId, article);
            } else if (Vote.DATA_TYPE_C_COMMENT == dataType) {
                final JSONObject comment = commentMapper.get(dataId);
                if (null == comment) {
                    LOGGER.error( "Not found comment [id={0}] to vote cancel", dataId);

                    return;
                }

                if (Vote.TYPE_C_UP == oldType) {
                    comment.put(Comment.COMMENT_GOOD_CNT, comment.optInt(Comment.COMMENT_GOOD_CNT) - 1);
                } else if (Vote.TYPE_C_DOWN == oldType) {
                    comment.put(Comment.COMMENT_BAD_CNT, comment.optInt(Comment.COMMENT_BAD_CNT) - 1);
                }

                final int ups = comment.optInt(Comment.COMMENT_GOOD_CNT);
                final int downs = comment.optInt(Comment.COMMENT_BAD_CNT);

                final double redditScore = redditCommentScore(ups, downs);
                comment.put(Comment.COMMENT_SCORE, redditScore);

                commentMapper.update(dataId, comment);
            } else {
                LOGGER.warn("Wrong data type [" + dataType + "]");
            }
        } catch (final MapperException e) {
            LOGGER.error( e.getMessage());
        }
    }

    /**
     * The specified user vote up the specified article/comment.
     *
     * @param userId   the specified user id
     * @param dataId   the specified article/comment id
     * @param dataType the specified data type
     * @throws ServiceException service exception
     */
    @Transactional
    public void voteUp(final String userId, final String dataId, final int dataType) throws ServiceException {
        try {
            up(userId, dataId, dataType);
        } catch (final MapperException e) {
            final String msg = "User[id=" + userId + "] vote up an [" + dataType + "][id=" + dataId + "] failed";
            LOGGER.error( msg, e);

            throw new ServiceException(msg);
        }

        livenessMgmtService.incLiveness(userId, Liveness.LIVENESS_VOTE);
    }

    /**
     * The specified user vote down the specified article、comment.
     *
     * @param userId the specified user id
     * @param dataId the specified article id
     * @throws ServiceException service exception
     */
    @Transactional
    public void voteDown(final String userId, final String dataId, final int dataType) throws ServiceException {
        try {
            down(userId, dataId, dataType);
        } catch (final MapperException e) {
            final String msg = "User[id=" + userId + "] vote down an [" + dataType + "][id=" + dataId + "] failed";
            LOGGER.error( msg, e);

            throw new ServiceException(msg);
        }

        livenessMgmtService.incLiveness(userId, Liveness.LIVENESS_VOTE);
    }

    /**
     * The specified user vote up the specified data entity with the specified data type.
     *
     * @param userId   the specified user id
     * @param dataId   the specified data entity id
     * @param dataType the specified data type
     * @throws MapperException Mapper exception
     */
    private void up(final String userId, final String dataId, final int dataType) throws MapperException {
        final int oldType = voteMapper.removeIfExists(userId, dataId, dataType);

        if (Vote.DATA_TYPE_C_ARTICLE == dataType) {
            final JSONObject article = articleMapper.get(dataId);
            if (null == article) {
                LOGGER.error( "Not found article [id={0}] to vote up", dataId);

                return;
            }

            if (-1 == oldType) {
                article.put(Article.ARTICLE_GOOD_CNT, article.optInt(Article.ARTICLE_GOOD_CNT) + 1);
            } else if (Vote.TYPE_C_DOWN == oldType) {
                article.put(Article.ARTICLE_BAD_CNT, article.optInt(Article.ARTICLE_BAD_CNT) - 1);
                article.put(Article.ARTICLE_GOOD_CNT, article.optInt(Article.ARTICLE_GOOD_CNT) + 1);
            }

            final int ups = article.optInt(Article.ARTICLE_GOOD_CNT);
            final int downs = article.optInt(Article.ARTICLE_BAD_CNT);
            final long t = article.optLong(Keys.OBJECT_ID) / 1000;

            final double redditScore = redditArticleScore(ups, downs, t);
            article.put(Article.REDDIT_SCORE, redditScore);

            updateTagArticleScore(article);

            articleMapper.update(dataId, article);
        } else if (Vote.DATA_TYPE_C_COMMENT == dataType) {
            final JSONObject comment = commentMapper.get(dataId);
            if (null == comment) {
                LOGGER.error( "Not found comment [id={0}] to vote up", dataId);

                return;
            }

            if (-1 == oldType) {
                comment.put(Comment.COMMENT_GOOD_CNT, comment.optInt(Comment.COMMENT_GOOD_CNT) + 1);
            } else if (Vote.TYPE_C_DOWN == oldType) {
                comment.put(Comment.COMMENT_BAD_CNT, comment.optInt(Comment.COMMENT_BAD_CNT) - 1);
                comment.put(Comment.COMMENT_GOOD_CNT, comment.optInt(Comment.COMMENT_GOOD_CNT) + 1);
            }

            final int ups = comment.optInt(Comment.COMMENT_GOOD_CNT);
            final int downs = comment.optInt(Comment.COMMENT_BAD_CNT);

            final double redditScore = redditCommentScore(ups, downs);
            comment.put(Comment.COMMENT_SCORE, redditScore);

            commentMapper.update(dataId, comment);
        } else {
            LOGGER.warn("Wrong data type [" + dataType + "]");
        }

        final JSONObject vote = new JSONObject();
        vote.put(Vote.USER_ID, userId);
        vote.put(Vote.DATA_ID, dataId);
        vote.put(Vote.TYPE, Vote.TYPE_C_UP);
        vote.put(Vote.DATA_TYPE, dataType);

        voteMapper.add(vote);
    }

    /**
     * The specified user vote down the specified data entity with the specified data type.
     *
     * @param userId   the specified user id
     * @param dataId   the specified data entity id
     * @param dataType the specified data type
     * @throws MapperException Mapper exception
     */
    private void down(final String userId, final String dataId, final int dataType) throws MapperException {
        final int oldType = voteMapper.removeIfExists(userId, dataId, dataType);

        if (Vote.DATA_TYPE_C_ARTICLE == dataType) {
            final JSONObject article = articleMapper.get(dataId);
            if (null == article) {
                LOGGER.error( "Not found article [id={0}] to vote down", dataId);

                return;
            }

            if (-1 == oldType) {
                article.put(Article.ARTICLE_BAD_CNT, article.optInt(Article.ARTICLE_BAD_CNT) + 1);
            } else if (Vote.TYPE_C_UP == oldType) {
                article.put(Article.ARTICLE_GOOD_CNT, article.optInt(Article.ARTICLE_GOOD_CNT) - 1);
                article.put(Article.ARTICLE_BAD_CNT, article.optInt(Article.ARTICLE_BAD_CNT) + 1);
            }

            final int ups = article.optInt(Article.ARTICLE_GOOD_CNT);
            final int downs = article.optInt(Article.ARTICLE_BAD_CNT);
            final long t = article.optLong(Keys.OBJECT_ID) / 1000;

            final double redditScore = redditArticleScore(ups, downs, t);
            article.put(Article.REDDIT_SCORE, redditScore);

            updateTagArticleScore(article);

            articleMapper.update(dataId, article);
        } else if (Vote.DATA_TYPE_C_COMMENT == dataType) {
            final JSONObject comment = commentMapper.get(dataId);
            if (null == comment) {
                LOGGER.error( "Not found comment [id={0}] to vote up", dataId);

                return;
            }

            if (-1 == oldType) {
                comment.put(Comment.COMMENT_BAD_CNT, comment.optInt(Comment.COMMENT_BAD_CNT) + 1);
            } else if (Vote.TYPE_C_UP == oldType) {
                comment.put(Comment.COMMENT_GOOD_CNT, comment.optInt(Comment.COMMENT_GOOD_CNT) - 1);
                comment.put(Comment.COMMENT_BAD_CNT, comment.optInt(Comment.COMMENT_BAD_CNT) + 1);
            }

            final int ups = comment.optInt(Comment.COMMENT_GOOD_CNT);
            final int downs = comment.optInt(Comment.COMMENT_BAD_CNT);

            final double redditScore = redditCommentScore(ups, downs);
            comment.put(Comment.COMMENT_SCORE, redditScore);

            commentMapper.update(dataId, comment);
        } else {
            LOGGER.warn("Wrong data type [" + dataType + "]");
        }

        final JSONObject vote = new JSONObject();
        vote.put(Vote.USER_ID, userId);
        vote.put(Vote.DATA_ID, dataId);
        vote.put(Vote.TYPE, Vote.TYPE_C_DOWN);
        vote.put(Vote.DATA_TYPE, dataType);

        voteMapper.add(vote);
    }

    private void updateTagArticleScore(final JSONObject article) throws MapperException {
        final List<JSONObject> tagArticleRels = tagArticleMapper.getByArticleId(article.optString(Keys.OBJECT_ID));
        for (final JSONObject tagArticleRel : tagArticleRels) {
            tagArticleRel.put(Article.REDDIT_SCORE, article.optDouble(Article.REDDIT_SCORE, 0D));

            tagArticleMapper.update(tagArticleRel.optString(Keys.OBJECT_ID), tagArticleRel);
        }
    }
}

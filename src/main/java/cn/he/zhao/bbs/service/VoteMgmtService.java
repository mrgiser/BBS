package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.LivenessUtil;
import cn.he.zhao.bbs.entityUtil.VoteUtil;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * LivenessUtil management service.
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

            if (VoteUtil.DATA_TYPE_C_ARTICLE == dataType) {
                final Article article = articleMapper.getByPrimaryKey(dataId);
                if (null == article) {
                    LOGGER.error( "Not found article [id={0}] to vote cancel", dataId);

                    return;
                }

                if (VoteUtil.TYPE_C_UP == oldType) {
                    article.setArticleGoodCnt( article.getArticleGoodCnt() - 1);
                } else if (VoteUtil.TYPE_C_DOWN == oldType) {
                    article.setArticleBadCnt(article.getArticleBadCnt() - 1);
                }

                final int ups = article.getArticleGoodCnt();
                final int downs = article.getArticleBadCnt();
                final long t = Long.parseLong(article.getOid()) / 1000;

                final double redditScore = redditArticleScore(ups, downs, t);
                article.setRedditScore(redditScore);

                updateTagArticleScore(article);

                articleMapper.update(article);
            } else if (VoteUtil.DATA_TYPE_C_COMMENT == dataType) {
                final Comment comment = commentMapper.get(dataId);
                if (null == comment) {
                    LOGGER.error( "Not found comment [id={0}] to vote cancel", dataId);

                    return;
                }

                if (VoteUtil.TYPE_C_UP == oldType) {
                    comment.setCommentGoodCnt( comment.getCommentGoodCnt() - 1);
                } else if (VoteUtil.TYPE_C_DOWN == oldType) {
                    comment.setCommentBadCnt(comment.getCommentBadCnt() - 1);
                }

                final int ups = comment.getCommentGoodCnt();
                final int downs = comment.getCommentBadCnt();

                final double redditScore = redditCommentScore(ups, downs);
                comment.setCommentScore( redditScore);

                commentMapper.update(dataId, comment);
            } else {
                LOGGER.warn("Wrong data type [" + dataType + "]");
            }
        } catch (final Exception e) {
            LOGGER.error( e.getMessage());
        }
    }

    /**
     * The specified user vote up the specified article/comment.
     *
     * @param userId   the specified user id
     * @param dataId   the specified article/comment id
     * @param dataType the specified data type
     * @throws Exception service exception
     */
    @Transactional
    public void voteUp(final String userId, final String dataId, final int dataType) throws Exception {
        try {
            up(userId, dataId, dataType);
        } catch (final Exception e) {
            final String msg = "User[id=" + userId + "] vote up an [" + dataType + "][id=" + dataId + "] failed";
            LOGGER.error( msg, e);

            throw new Exception(msg);
        }

        livenessMgmtService.incLiveness(userId, LivenessUtil.LIVENESS_VOTE);
    }

    /**
     * The specified user vote down the specified article、comment.
     *
     * @param userId the specified user id
     * @param dataId the specified article id
     * @throws Exception service exception
     */
    @Transactional
    public void voteDown(final String userId, final String dataId, final int dataType) throws Exception {
        try {
            down(userId, dataId, dataType);
        } catch (final Exception e) {
            final String msg = "User[id=" + userId + "] vote down an [" + dataType + "][id=" + dataId + "] failed";
            LOGGER.error( msg, e);

            throw new Exception(msg);
        }

        livenessMgmtService.incLiveness(userId, LivenessUtil.LIVENESS_VOTE);
    }

    /**
     * The specified user vote up the specified data entity with the specified data type.
     *
     * @param userId   the specified user id
     * @param dataId   the specified data entity id
     * @param dataType the specified data type
     * @throws Exception Mapper exception
     */
    private void up(final String userId, final String dataId, final int dataType) throws Exception {
        final int oldType = voteMapper.removeIfExists(userId, dataId, dataType);

        if (VoteUtil.DATA_TYPE_C_ARTICLE == dataType) {
            final Article article = articleMapper.getByPrimaryKey(dataId);
            if (null == article) {
                LOGGER.error( "Not found article [id={0}] to vote up", dataId);

                return;
            }

            if (-1 == oldType) {
                article.setArticleGoodCnt( article.getArticleGoodCnt() + 1);
            } else if (VoteUtil.TYPE_C_DOWN == oldType) {
                article.setArticleBadCnt( article.getArticleBadCnt() - 1);
                article.setArticleGoodCnt(article.getArticleGoodCnt() + 1);
            }

            final int ups = article.getArticleGoodCnt();
            final int downs = article.getArticleBadCnt();
            final long t = Long.parseLong(article.getOid()) / 1000;

            final double redditScore = redditArticleScore(ups, downs, t);
            article.setRedditScore(redditScore);

            updateTagArticleScore(article);

            articleMapper.update( article);
        } else if (VoteUtil.DATA_TYPE_C_COMMENT == dataType) {
            final Comment comment = commentMapper.get(dataId);
            if (null == comment) {
                LOGGER.error( "Not found comment [id={0}] to vote up", dataId);

                return;
            }

            if (-1 == oldType) {
                comment.setCommentGoodCnt( comment.getCommentGoodCnt() + 1);
            } else if (VoteUtil.TYPE_C_DOWN == oldType) {
                comment.setCommentBadCnt( comment.getCommentBadCnt() - 1);
                comment.setCommentGoodCnt(comment.getCommentGoodCnt() + 1);
            }

            final int ups = comment.getCommentGoodCnt();
            final int downs = comment.getCommentBadCnt();

            final double redditScore = redditCommentScore(ups, downs);
            comment.setCommentScore(redditScore);

            commentMapper.update(dataId, comment);
        } else {
            LOGGER.warn("Wrong data type [" + dataType + "]");
        }

        final Vote vote = new Vote();
        vote.setUserId( userId);
        vote.setDataId( dataId);
        vote.setType(VoteUtil.TYPE_C_UP);
        vote.setDataType(dataType);

        voteMapper.add(vote);
    }

    /**
     * The specified user vote down the specified data entity with the specified data type.
     *
     * @param userId   the specified user id
     * @param dataId   the specified data entity id
     * @param dataType the specified data type
     * @throws Exception Mapper exception
     */
    private void down(final String userId, final String dataId, final int dataType) throws Exception {
        final int oldType = voteMapper.removeIfExists(userId, dataId, dataType);

        if (VoteUtil.DATA_TYPE_C_ARTICLE == dataType) {
            final Article article = articleMapper.getByPrimaryKey(dataId);
            if (null == article) {
                LOGGER.error( "Not found article [id={0}] to vote down", dataId);

                return;
            }

            if (-1 == oldType) {
                article.setArticleBadCnt( article.getArticleBadCnt() + 1);
            } else if (VoteUtil.TYPE_C_UP == oldType) {
                article.setArticleGoodCnt(article.getArticleGoodCnt() - 1);
                article.setArticleBadCnt( article.getArticleBadCnt() + 1);
            }

            final int ups = article.getArticleGoodCnt();
            final int downs = article.getArticleBadCnt();
            final long t = Long.parseLong(article.getOid()) / 1000;

            final double redditScore = redditArticleScore(ups, downs, t);
            article.setRedditScore(redditScore);

            updateTagArticleScore(article);

            articleMapper.update( article);
        } else if (VoteUtil.DATA_TYPE_C_COMMENT == dataType) {
            final Comment comment = commentMapper.get(dataId);
            if (null == comment) {
                LOGGER.error( "Not found comment [id={0}] to vote up", dataId);

                return;
            }

            if (-1 == oldType) {
                comment.setCommentBadCnt( comment.getCommentBadCnt() + 1);
            } else if (VoteUtil.TYPE_C_UP == oldType) {
                comment.setCommentGoodCnt( comment.getCommentGoodCnt() - 1);
                comment.setCommentBadCnt( comment.getCommentBadCnt() + 1);
            }

            final int ups = comment.getCommentGoodCnt();
            final int downs = comment.getCommentBadCnt();

            final double redditScore = redditCommentScore(ups, downs);
            comment.setCommentScore(redditScore);

            commentMapper.update(dataId, comment);
        } else {
            LOGGER.warn("Wrong data type [" + dataType + "]");
        }

        final Vote vote = new Vote();
        vote.setUserId( userId);
        vote.setDataId( dataId);
        vote.setType(VoteUtil.TYPE_C_DOWN);
        vote.setDataType(dataType);

        voteMapper.add(vote);

        voteMapper.add(vote);
    }

    private void updateTagArticleScore(final Article article) throws Exception {
        final List<TagArticle> tagArticleRels = tagArticleMapper.getByArticleId(article.getOid());
        for (final TagArticle tagArticleRel : tagArticleRels) {
            // TODO: 2018/9/2  默认为 0
            tagArticleRel.setRedditScore( article.getRedditScore( ));

            tagArticleMapper.update(tagArticleRel.getOid(), tagArticleRel);
        }
    }
}

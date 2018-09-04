
package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.ArticleUtil;
import cn.he.zhao.bbs.entityUtil.CommentUtil;
import cn.he.zhao.bbs.entityUtil.VoteUtil;
import cn.he.zhao.bbs.entityUtil.my.Keys;
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
 * Vote query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.1, Oct 17, 2016
 * @since 1.3.0
 */
@Service
public class VoteQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VoteQueryService.class);

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
     * Comment Mapper.
     */
    @Autowired
    private CommentMapper commentMapper;

    /**
     * Determines whether the specified user dose vote the specified entity.
     *
     * @param userId the specified user id
     * @param dataId the specified entity id
     * @return voted type, returns {@code -1} if has not voted yet
     */
    public int isVoted(final String userId, final String dataId) {
        try {
//            final List<Filter> filters = new ArrayList<>();
//            filters.add(new PropertyFilter(Vote.USER_ID, FilterOperator.EQUAL, userId));
//            filters.add(new PropertyFilter(Vote.DATA_ID, FilterOperator.EQUAL, dataId));
//
//            final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

            final List<Vote> results = voteMapper.getByUserIdAndDataId( userId, dataId);

            if (0 == results.size()) {
                return -1;
            }

            final Vote vote = results.get(0);

            return vote.getType();
        } catch (final Exception e) {
            LOGGER.error( e.getMessage());

            return -1;
        }
    }

    /**
     * Determines whether the specified data dose belong to the specified user.
     *
     * @param userId the specified user id
     * @param dataId the specified data id
     * @param dataType the specified data type
     * @return {@code true} if it belongs to the user, otherwise returns {@code false}
     */
    public boolean isOwn(final String userId, final String dataId, final int dataType) {
        try {
            if (VoteUtil.DATA_TYPE_C_ARTICLE == dataType) {
                final Article article = articleMapper.get(dataId);
                if (null == article) {
                    LOGGER.error( "Not found article [id={0}]", dataId);

                    return false;
                }

                return article.getArticleAuthorId().equals(userId);
            } else if (VoteUtil.DATA_TYPE_C_COMMENT == dataType) {
                final JSONObject comment = commentMapper.get(dataId);
                if (null == comment) {
                    LOGGER.error( "Not found comment [id={0}]", dataId);

                    return false;
                }

                return comment.optString(CommentUtil.COMMENT_AUTHOR_ID).equals(userId);
            }

            return false;
        } catch (final Exception e) {
            LOGGER.error( e.getMessage());

            return false;
        }
    }
}

package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entity.my.*;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.encoder.Encode;

import java.util.Collections;
import java.util.List;

/**
 * Revision query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.0, Nov 14, 2017
 * @since 2.1.0
 */
@Service
public class RevisionQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RevisionQueryService.class);

    /**
     * Revision Mapper.
     */
    @Autowired
    private RevisionMapper revisionMapper;

    /**
     * Gets a comment's revisions.
     *
     * @param commentId the specified comment id
     * @return comment revisions, returns an empty list if not found
     */
    public List<JSONObject> getCommentRevisions(final String commentId) {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Revision.REVISION_DATA_ID, FilterOperator.EQUAL, commentId),
                new PropertyFilter(Revision.REVISION_DATA_TYPE, FilterOperator.EQUAL, Revision.DATA_TYPE_C_COMMENT)
        )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);

        try {
            final List<JSONObject> ret = CollectionUtils.jsonArrayToList(revisionMapper.get(query).optJSONArray(Keys.RESULTS));
            for (final JSONObject rev : ret) {
                final JSONObject data = new JSONObject(rev.optString(Revision.REVISION_DATA));
                String commentContent = data.optString(Comment.COMMENT_CONTENT);
                commentContent = commentContent.replace("\n", "_esc_br_");
                commentContent = Markdowns.clean(commentContent, "");
                commentContent = commentContent.replace("_esc_br_", "\n");
                data.put(Comment.COMMENT_CONTENT, commentContent);

                rev.put(Revision.REVISION_DATA, data);
            }

            return ret;
        } catch (final MapperException | JSONException e) {
            LOGGER.error( "Gets comment revisions failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets an article's revisions.
     *
     * @param articleId the specified article id
     * @return article revisions, returns an empty list if not found
     */
    public List<JSONObject> getArticleRevisions(final String articleId) {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Revision.REVISION_DATA_ID, FilterOperator.EQUAL, articleId),
                new PropertyFilter(Revision.REVISION_DATA_TYPE, FilterOperator.EQUAL, Revision.DATA_TYPE_C_ARTICLE)
        )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);

        try {
            final List<JSONObject> ret = CollectionUtils.jsonArrayToList(revisionMapper.get(query).optJSONArray(Keys.RESULTS));
            for (final JSONObject rev : ret) {
                final JSONObject data = new JSONObject(rev.optString(Revision.REVISION_DATA));
                final String articleTitle = Encode.forHtml(data.optString(Article.ARTICLE_TITLE));
                data.put(Article.ARTICLE_TITLE, articleTitle);

                String articleContent = data.optString(Article.ARTICLE_CONTENT);
                // articleContent = Markdowns.toHTML(articleContent); https://hacpai.com/article/1490233597586
                articleContent = articleContent.replace("\n", "_esc_br_");
                articleContent = Markdowns.clean(articleContent, "");
                articleContent = articleContent.replace("_esc_br_", "\n");
                data.put(Article.ARTICLE_CONTENT, articleContent);

                rev.put(Revision.REVISION_DATA, data);
            }

            return ret;
        } catch (final MapperException | JSONException e) {
            LOGGER.error( "Gets article revisions failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Counts revision specified by the given data id and data type.
     *
     * @param dataId   the given data id
     * @param dataType the given data type
     * @return count result
     */
    public int count(final String dataId, final int dataType) {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Revision.REVISION_DATA_ID, FilterOperator.EQUAL, dataId),
                new PropertyFilter(Revision.REVISION_DATA_TYPE, FilterOperator.EQUAL, dataType)
        ));

        Stopwatchs.start("Revision count");
        try {
            return (int) revisionMapper.count(query);
        } catch (final Exception e) {
            LOGGER.error( "Counts revisions failed", e);

            return 0;
        } finally {
            Stopwatchs.end();
        }
    }
}

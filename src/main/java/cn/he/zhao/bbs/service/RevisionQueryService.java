package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.ArticleUtil;
import cn.he.zhao.bbs.entityUtil.CommentUtil;
import cn.he.zhao.bbs.entityUtil.RevisionUtil;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import cn.he.zhao.bbs.spring.Stopwatchs;
import cn.he.zhao.bbs.util.Markdowns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;
import org.owasp.encoder.Encode;

import java.util.Collections;
import java.util.List;

@Service
public class RevisionQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RevisionQueryService.class);

    /**
     * RevisionUtil Mapper.
     */
    @Autowired
    private RevisionMapper revisionMapper;

    /**
     * Gets a comment's revisions.
     *
     * @param commentId the specified comment id
     * @return comment revisions, returns an empty list if not found
     */
    public List<Revision> getCommentRevisions(final String commentId) {
//        final Query query = new Query().setFilter(CompositeFilterOperator.and(
//                new PropertyFilter(RevisionUtil.REVISION_DATA_ID, FilterOperator.EQUAL, commentId),
//                new PropertyFilter(RevisionUtil.REVISION_DATA_TYPE, FilterOperator.EQUAL, RevisionUtil.DATA_TYPE_C_COMMENT)
//        )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);

        try {
            final List<Revision> ret = revisionMapper.getByRevisionDataIdAndRevisionDataType(commentId, RevisionUtil.DATA_TYPE_C_COMMENT);
            for (final Revision rev : ret) {
                final JSONObject data = new JSONObject(rev.getRevisionData());
                String commentContent = data.getString(CommentUtil.COMMENT_CONTENT);
                commentContent = commentContent.replace("\n", "_esc_br_");
                commentContent = Markdowns.clean(commentContent, "");
                commentContent = commentContent.replace("_esc_br_", "\n");
                data.put(CommentUtil.COMMENT_CONTENT, commentContent);

                rev.setRevisionData(data.toString());
            }

            return ret;
        } catch (final Exception  e) {
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
    public List<Revision> getArticleRevisions(final String articleId) {
//        final Query query = new Query().setFilter(CompositeFilterOperator.and(
//                new PropertyFilter(RevisionUtil.REVISION_DATA_ID, FilterOperator.EQUAL, articleId),
//                new PropertyFilter(RevisionUtil.REVISION_DATA_TYPE, FilterOperator.EQUAL, RevisionUtil.DATA_TYPE_C_ARTICLE)
//        )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);

        try {
            final List<Revision> ret = revisionMapper.getByRevisionDataIdAndRevisionDataType(articleId, RevisionUtil.DATA_TYPE_C_ARTICLE);
            for (final Revision rev : ret) {
                final JSONObject data = new JSONObject(rev.getRevisionData());
                final String articleTitle = Encode.forHtml(data.getString(ArticleUtil.ARTICLE_TITLE));
                data.put(ArticleUtil.ARTICLE_TITLE, articleTitle);

                String articleContent = data.optString(ArticleUtil.ARTICLE_CONTENT);
                // articleContent = Markdowns.toHTML(articleContent); https://hacpai.com/article/1490233597586
                articleContent = articleContent.replace("\n", "_esc_br_");
                articleContent = Markdowns.clean(articleContent, "");
                articleContent = articleContent.replace("_esc_br_", "\n");
                data.put(ArticleUtil.ARTICLE_CONTENT, articleContent);

                rev.setRevisionData( data.toString());
            }

            return ret;
        } catch (final  Exception  e) {
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
//        final Query query = new Query().setFilter(CompositeFilterOperator.and(
//                new PropertyFilter(Revision.REVISION_DATA_ID, FilterOperator.EQUAL, dataId),
//                new PropertyFilter(Revision.REVISION_DATA_TYPE, FilterOperator.EQUAL, dataType)
//        ));

        Stopwatchs.start("RevisionUtil count");
        try {
            return revisionMapper.countByRevisionDataIdAndRevisionDataType(dataId, dataType);
        } catch (final Exception e) {
            LOGGER.error( "Counts revisions failed", e);

            return 0;
        } finally {
            Stopwatchs.end();
        }
    }
}

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

import cn.he.zhao.bbs.entityUtil.ArticleUtil;
import cn.he.zhao.bbs.entityUtil.CommentUtil;
import cn.he.zhao.bbs.entityUtil.PointtransferUtil;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import jodd.io.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

/**
 * Post (article/comment) export service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.2, Apr 5, 2018
 * @since 1.4.0
 */
@Service
public class PostExportService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PostExportService.class);

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

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
     * PointtransferUtil management service.
     */
    @Autowired
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Exports all posts of a user's specified with the given user id.
     *
     * @param userId the given user id
     * @return download URL, returns {@code "-1"} if in sufficient balance, returns {@code null} if other exceptions
     */
    public String exportPosts(final String userId) {
        final int pointDataExport = Symphonys.getInt("pointDataExport");
        try {
            final UserExt user = userMapper.get(userId);
            final int balance = user.getUserPoint();

            if (balance - pointDataExport < 0) {
                return "-1";
            }
        } catch (final Exception e) {
            LOGGER.error( "Checks user failed", e);

            return null;
        }

        final JSONArray posts = new JSONArray();

//        Query query = new Query().setFilter(
//                new PropertyFilter(ArticleUtil.ARTICLE_AUTHOR_ID, FilterOperator.EQUAL, userId)).
//                addProjection(Keys.OBJECT_ID, String.class).
//                addProjection(Article.ARTICLE_TITLE, String.class).
//                addProjection(Article.ARTICLE_TAGS, String.class).
//                addProjection(Article.ARTICLE_CONTENT, String.class).
//                addProjection(Article.ARTICLE_CREATE_TIME, Long.class);

        try {
            final List<Article> articles = articleMapper.getByArticleAuthorId(userId);

            for (int i = 0; i < articles.size(); i++) {
                final Article article = articles.get(i);
                final JSONObject post = new JSONObject();

                post.put("id", article.getOid());

                final JSONObject content = new JSONObject();
                content.put("title", article.getArticleTitle());
                content.put("tags", article.getArticleTags());
                content.put("body", article.getArticleContent());

                post.put("content", content.toString());
                post.put("created", ArticleUtil.ARTICLE_CREATE_TIME);
                post.put("type", "article");

                posts.put(post);
            }
        } catch (final Exception e) {
            LOGGER.error( "Export articles failed", e);

            return null;
        }

//        query = new Query().setFilter(
//                new PropertyFilter(CommentUtil.COMMENT_AUTHOR_ID, FilterOperator.EQUAL, userId)).
//                addProjection(Keys.OBJECT_ID, String.class).
//                addProjection(Comment.COMMENT_CONTENT, String.class).
//                addProjection(Comment.COMMENT_CREATE_TIME, Long.class);

        try {
            final List<Comment> comments = commentMapper.getByCommentAuthorId(userId);

            for (int i = 0; i < comments.size(); i++) {
                final Comment comment = comments.get(i);
                final JSONObject post = new JSONObject();

                post.put("id", comment.getOid());

                final JSONObject content = new JSONObject();
                content.put("title", "");
                content.put("tags", "");
                content.put("body", comment.getCommentContent());

                post.put("content", content.toString());
                post.put("created", CommentUtil.COMMENT_CREATE_TIME);
                post.put("type", "comment");

                posts.put(post);
            }
        } catch (final Exception e) {
            LOGGER.error( "Export comments failed", e);

            return null;
        }

        LOGGER.info("Exporting posts [size=" + posts.length() + "]");

        final boolean succ = null != pointtransferMgmtService.transfer(userId, PointtransferUtil.ID_C_SYS,
                PointtransferUtil.TRANSFER_TYPE_C_DATA_EXPORT, PointtransferUtil.TRANSFER_SUM_C_DATA_EXPORT,
                String.valueOf(posts.length()), System.currentTimeMillis());
        if (!succ) {
            return null;
        }

        final String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String fileKey = "export/" + userId + "/" + uuid + ".zip";

        final String tmpDir = System.getProperty("java.io.tmpdir");
        String localFilePath = tmpDir + "/" + uuid + ".json";
        LOGGER.info(localFilePath);
        final File localFile = new File(localFilePath);

        try {
            final byte[] data = posts.toString(2).getBytes("UTF-8");

            try (final OutputStream output = new FileOutputStream(localFile)) {
                IOUtils.write(data, output);
            }

            final File zipFile = ZipUtil.zip(localFile);

            final FileInputStream inputStream = new FileInputStream(zipFile);
            final byte[] zipData = IOUtils.toByteArray(inputStream);

            if (Symphonys.getBoolean("qiniu.enabled")) {
                final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
                final UploadManager uploadManager = new UploadManager(new Configuration());

                uploadManager.put(zipData, fileKey, auth.uploadToken(Symphonys.get("qiniu.bucket")),
                        null, "application/zip", false);

                return Symphonys.get("qiniu.domain") + "/" + fileKey;
            } else {
                final String filePath = Symphonys.get("upload.dir") + fileKey;

                FileUtils.copyFile(zipFile, new File(filePath));

                return  SpringUtil.getServerPath() + "/upload/" + fileKey;
            }
        } catch (final Exception e) {
            LOGGER.error( "Uploading exprted data failed", e);

            return null;
        }
    }
}

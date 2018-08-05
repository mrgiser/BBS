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
package cn.he.zhao.bbs.validate;

import cn.he.zhao.bbs.exception.RequestProcessAdviceException;
import cn.he.zhao.bbs.spring.Strings;
import cn.he.zhao.bbs.model.Article;
import cn.he.zhao.bbs.model.Role;
import cn.he.zhao.bbs.model.Tag;
import cn.he.zhao.bbs.model.my.Keys;
import cn.he.zhao.bbs.model.my.User;
import cn.he.zhao.bbs.service.OptionQueryService;
import cn.he.zhao.bbs.service.TagQueryService;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.util.StatusCodes;
import cn.he.zhao.bbs.util.Symphonys;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Component
public class ArticleAddValidation {

    @Autowired
    private static LangPropsService langPropsService ;

    @Autowired
    private static TagQueryService tagQueryService ;

    @Autowired
    private static OptionQueryService optionQueryService ;

    /**
     * Max article title length.
     */
    public static final int MAX_ARTICLE_TITLE_LENGTH = 255;

    /**
     * Max article content length.
     */
    public static final int MAX_ARTICLE_CONTENT_LENGTH = 102400;

    /**
     * Min article content length.
     */
    public static final int MIN_ARTICLE_CONTENT_LENGTH = 4;

    /**
     * Max article reward content length.
     */
    public static final int MAX_ARTICLE_REWARD_CONTENT_LENGTH = 102400;

    /**
     * Min article reward content length.
     */
    public static final int MIN_ARTICLE_REWARD_CONTENT_LENGTH = 4;

    /**
     * Validates article fields.
     *
     * @param request           the specified HTTP servlet request
     * @param requestJSONObject the specified request object
     * @throws RequestProcessAdviceException if validate failed
     */
    public static void validateArticleFields(final HttpServletRequest request,
                                             final JSONObject requestJSONObject) throws RequestProcessAdviceException {

        final JSONObject exception = new JSONObject();
        exception.put(Keys.STATUS_CODE, StatusCodes.ERR);

        String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        articleTitle = StringUtils.trim(articleTitle);
        if (Strings.isEmptyOrNull(articleTitle) || articleTitle.length() > MAX_ARTICLE_TITLE_LENGTH) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("articleTitleErrorLabel")));
        }
        if (optionQueryService.containReservedWord(articleTitle)) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("contentContainReservedWordLabel")));
        }

        requestJSONObject.put(Article.ARTICLE_TITLE, articleTitle);

        final int articleType = requestJSONObject.optInt(Article.ARTICLE_TYPE);
        if (Article.isInvalidArticleType(articleType)) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("articleTypeErrorLabel")));
        }

        String articleTags = requestJSONObject.optString(Article.ARTICLE_TAGS);
        articleTags = Tag.formatTags(articleTags);

        if (StringUtils.isBlank(articleTags)) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("tagsEmptyErrorLabel")));
        }

        if (optionQueryService.containReservedWord(articleTags)) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("contentContainReservedWordLabel")));
        }

        if (StringUtils.isNotBlank(articleTags)) {
            String[] tagTitles = articleTags.split(",");

            tagTitles = new LinkedHashSet<String>(Arrays.asList(tagTitles)).toArray(new String[0]);
            final List<String> invalidTags = tagQueryService.getInvalidTags();

            final StringBuilder tagBuilder = new StringBuilder();
            for (int i = 0; i < tagTitles.length; i++) {
                final String tagTitle = tagTitles[i].trim();

                if (Strings.isEmptyOrNull(tagTitle)) {
                    throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("tagsErrorLabel")));
                }

                if (!Tag.containsWhiteListTags(tagTitle)) {
                    if (!Tag.TAG_TITLE_PATTERN.matcher(tagTitle).matches()) {
                        throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("tagsErrorLabel")));
                    }

                    if (tagTitle.length() > Tag.MAX_TAG_TITLE_LENGTH) {
                        throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("tagsErrorLabel")));
                    }
                }

                final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
                if (!Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))
                        && ArrayUtils.contains(Symphonys.RESERVED_TAGS, tagTitle)) {
                    throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("articleTagReservedLabel")
                            + " [" + tagTitle + "]"));
                }

                if (invalidTags.contains(tagTitle)) {
                    throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("articleTagInvalidLabel")
                            + " [" + tagTitle + "]"));
                }

                tagBuilder.append(tagTitle).append(",");
            }
            if (tagBuilder.length() > 0) {
                tagBuilder.deleteCharAt(tagBuilder.length() - 1);
            }
            requestJSONObject.put(Article.ARTICLE_TAGS, tagBuilder.toString());
        }

        String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
        articleContent = StringUtils.trim(articleContent);
        if (Strings.isEmptyOrNull(articleContent) || articleContent.length() > MAX_ARTICLE_CONTENT_LENGTH
                || articleContent.length() < MIN_ARTICLE_CONTENT_LENGTH) {
            String msg = langPropsService.get("articleContentErrorLabel");
            msg = msg.replace("{maxArticleContentLength}", String.valueOf(MAX_ARTICLE_CONTENT_LENGTH));

            throw new RequestProcessAdviceException(exception.put(Keys.MSG, msg));
        }

        if (optionQueryService.containReservedWord(articleContent)) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("contentContainReservedWordLabel")));
        }

        final int rewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT, 0);
        if (rewardPoint < 0) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("invalidRewardPointLabel")));
        }

        final int articleQnAOfferPoint = requestJSONObject.optInt(Article.ARTICLE_QNA_OFFER_POINT, 0);
        if (articleQnAOfferPoint < 0) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("invalidQnAOfferPointLabel")));
        }

        final String articleRewardContnt = requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT);
        if (StringUtils.isNotBlank(articleRewardContnt) && 1 > rewardPoint) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("invalidRewardPointLabel")));
        }

        if (rewardPoint > 0) {
            if (Strings.isEmptyOrNull(articleRewardContnt) || articleRewardContnt.length() > MAX_ARTICLE_CONTENT_LENGTH
                    || articleRewardContnt.length() < MIN_ARTICLE_CONTENT_LENGTH) {
                String msg = langPropsService.get("articleRewardContentErrorLabel");
                msg = msg.replace("{maxArticleRewardContentLength}", String.valueOf(MAX_ARTICLE_REWARD_CONTENT_LENGTH));

                throw new RequestProcessAdviceException(exception.put(Keys.MSG, msg));
            }
        }
    }

    public void doAdvice(final HttpServletRequest request, final Map<String, Object> args) throws RequestProcessAdviceException {
        final JSONObject requestJSONObject = (JSONObject) args.get("requestJSONObject");

        validateArticleFields(request, requestJSONObject);
    }
}

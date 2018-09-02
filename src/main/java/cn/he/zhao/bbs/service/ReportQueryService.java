package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.encoder.Encode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ReportUtil management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.2, Jun 27, 2018
 * @since 3.1.0
 */
@Service
public class ReportQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportQueryService.class);

    /**
     * ReportUtil Mapper.
     */
    @Autowired
    private ReportMapper reportMapper;

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
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Comment query service.
     */
    @Autowired
    private CommentQueryService commentQueryService;

    /**
     * Gets report by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          {
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10
     *                          }, see {@link Pagination} for more details
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "reports": [{
     *         "oId": "",
     *         "reportUserName": "<a>/member/username</a>",
     *         "reportData": "<a>Article or user</a>",
     *         "reportDataType": int,
     *         "reportDataTypeStr": "",
     *         "reportType": int,
     *         "reportTypeStr": "",
     *         "reportMemo": "",
     *         "reportHandled": int,
     *
     *      }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     * @see Pagination
     */
    public JSONObject getReports(final JSONObject requestJSONObject) throws ServiceException {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                addSort(Report.REPORT_HANDLED, SortDirection.ASCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        JSONObject result;
        try {
            result = reportMapper.get(query);
        } catch (final MapperException e) {
            LOGGER.error( "Get reports failed", e);

            throw new ServiceException(e);
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> records = CollectionUtils.jsonArrayToList(data);
        final List<JSONObject> reports = new ArrayList<>();
        for (final JSONObject record : records) {
            final JSONObject report = new JSONObject();
            report.put(Keys.OBJECT_ID, record.optString(Keys.OBJECT_ID));
            try {
                final String reportUserId = record.optString(Report.REPORT_USER_ID);
                final JSONObject reporter = userMapper.get(reportUserId);
                report.put(Report.REPORT_T_USERNAME, UserExt.getUserLink(reporter));
                report.put(Report.REPORT_T_TIME, new Date(record.optLong(Keys.OBJECT_ID)));

                final String dataId = record.optString(Report.REPORT_DATA_ID);
                final int dataType = record.optInt(Report.REPORT_DATA_TYPE);
                report.put(Report.REPORT_DATA_TYPE, dataType);
                String reportData = langPropsService.get("removedLabel");
                switch (dataType) {
                    case Report.REPORT_DATA_TYPE_C_ARTICLE:
                        report.put(Report.REPORT_T_DATA_TYPE_STR, langPropsService.get("articleLabel"));
                        final JSONObject article = articleMapper.get(dataId);
                        if (null != article) {
                            final String title = Encode.forHtml(article.optString(Article.ARTICLE_TITLE));
                            reportData = "<a href=\"" +  SpringUtil.getServerPath() + "/article/" + article.optString(Keys.OBJECT_ID) +
                                    "\" target=\"_blank\">" + Emotions.convert(title) + "</a>";
                        }

                        break;
                    case Report.REPORT_DATA_TYPE_C_COMMENT:
                        report.put(Report.REPORT_T_DATA_TYPE_STR, langPropsService.get("cmtLabel"));
                        final JSONObject comment = commentMapper.get(dataId);
                        if (null != comment) {
                            final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
                            final JSONObject cmtArticle = articleMapper.get(articleId);
                            final String title = Encode.forHtml(cmtArticle.optString(Article.ARTICLE_TITLE));
                            final String commentId = comment.optString(Keys.OBJECT_ID);
                            final int cmtViewMode = UserExt.USER_COMMENT_VIEW_MODE_C_REALTIME;
                            reportData = commentQueryService.getCommentURL(commentId, cmtViewMode, Symphonys.getInt("articleCommentsPageSize"));
                        }

                        break;
                    case Report.REPORT_DATA_TYPE_C_USER:
                        report.put(Report.REPORT_T_DATA_TYPE_STR, langPropsService.get("accountLabel"));
                        final JSONObject reported = userMapper.get(dataId);
                        reportData = UserExt.getUserLink(reported);

                        break;
                    default:
                        LOGGER.error( "Unknown report data type [" + dataType + "]");

                        continue;
                }
                report.put(Report.REPORT_T_DATA, reportData);
            } catch (final Exception e) {
                LOGGER.error( "Builds report data failed", e);

                continue;
            }

            final int type = record.optInt(Report.REPORT_TYPE);
            report.put(Report.REPORT_TYPE, type);
            switch (type) {
                case Report.REPORT_TYPE_C_SPAM_AD:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("spamADLabel"));

                    break;
                case Report.REPORT_TYPE_C_PORNOGRAPHIC:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("pornographicLabel"));

                    break;
                case Report.REPORT_TYPE_C_VIOLATION_OF_REGULATIONS:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("violationOfRegulationsLabel"));

                    break;
                case Report.REPORT_TYPE_C_ALLEGEDLY_INFRINGING:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("allegedlyInfringingLabel"));

                    break;
                case Report.REPORT_TYPE_C_PERSONAL_ATTACKS:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("personalAttacksLabel"));

                    break;
                case Report.REPORT_TYPE_C_POSING_ACCOUNT:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("posingAccountLabel"));

                    break;
                case Report.REPORT_TYPE_C_SPAM_AD_ACCOUNT:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("spamADAccountLabel"));

                    break;
                case Report.REPORT_TYPE_C_PERSONAL_INFO_VIOLATION:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("personalInfoViolationLabel"));

                    break;
                case Report.REPORT_TYPE_C_OTHER:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("miscLabel"));

                    break;
                default:
                    LOGGER.error( "Unknown report type [" + type + "]");

                    continue;
            }

            String memo = record.optString(Report.REPORT_MEMO);
            memo = Markdowns.toHTML(memo);
            memo = Markdowns.clean(memo, "");
            report.put(Report.REPORT_MEMO, memo);
            report.put(Report.REPORT_HANDLED, record.optInt(Report.REPORT_HANDLED));

            reports.add(report);
        }
        ret.put(Report.REPORTS, reports);

        return ret;
    }
}

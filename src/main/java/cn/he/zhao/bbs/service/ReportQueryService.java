package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.ReportUtil;
import cn.he.zhao.bbs.entityUtil.UserExtUtil;
import cn.he.zhao.bbs.entityUtil.my.CollectionUtils;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.Pagination;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Paginator;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.util.Emotions;
import cn.he.zhao.bbs.util.Markdowns;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
     * @throws Exception service exception
     * @see Pagination
     */
    public JSONObject getReports(final JSONObject requestJSONObject) throws Exception {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);

        PageHelper.startPage(currentPageNum, pageSize);

//        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
//                addSort(ReportUtil.REPORT_HANDLED, SortDirection.ASCENDING).
//                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        PageInfo<Report> result;
        try {
            result = new PageInfo<>(reportMapper.getALL());
        } catch (final Exception e) {
            LOGGER.error( "Get reports failed", e);

            throw new Exception(e);
        }

        final int pageCount = result.getPages();
        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final List<Report> records = result.getList();
//        final List<JSONObject> records = CollectionUtils.jsonArrayToList(data);
        final List<JSONObject> reports = new ArrayList<>();
        for (final Report record : records) {
            final JSONObject report = new JSONObject();
            report.put(Keys.OBJECT_ID, record.getOid());
            try {
                final String reportUserId = record.getReportUserId();
                final UserExt reporter = userMapper.get(reportUserId);
                report.put(ReportUtil.REPORT_T_USERNAME, UserExtUtil.getUserLink(reporter));
                report.put(ReportUtil.REPORT_T_TIME, new Date(Long.parseLong(record.getOid())));

                final String dataId = record.getReportDataId();
                final int dataType = record.getReportDataType();
                report.put(ReportUtil.REPORT_DATA_TYPE, dataType);
                String reportData = langPropsService.get("removedLabel");
                switch (dataType) {
                    case ReportUtil.REPORT_DATA_TYPE_C_ARTICLE:
                        report.put(ReportUtil.REPORT_T_DATA_TYPE_STR, langPropsService.get("articleLabel"));
                        final Article article = articleMapper.getByOid(dataId);
                        if (null != article) {
                            final String title = Encode.forHtml(article.getArticleTitle());
                            reportData = "<a href=\"" +  SpringUtil.getServerPath() + "/article/" + article.getOid() +
                                    "\" target=\"_blank\">" + Emotions.convert(title) + "</a>";
                        }

                        break;
                    case ReportUtil.REPORT_DATA_TYPE_C_COMMENT:
                        report.put(ReportUtil.REPORT_T_DATA_TYPE_STR, langPropsService.get("cmtLabel"));
                        final Comment comment = commentMapper.get(dataId);
                        if (null != comment) {
                            final String articleId = comment.getCommentOnArticleId();
                            final Article cmtArticle = articleMapper.getByOid(articleId);
                            final String title = Encode.forHtml(cmtArticle.getArticleTitle());
                            final String commentId = comment.getOid();
                            final int cmtViewMode = UserExtUtil.USER_COMMENT_VIEW_MODE_C_REALTIME;
                            reportData = commentQueryService.getCommentURL(commentId, cmtViewMode, Symphonys.getInt("articleCommentsPageSize"));
                        }

                        break;
                    case ReportUtil.REPORT_DATA_TYPE_C_USER:
                        report.put(ReportUtil.REPORT_T_DATA_TYPE_STR, langPropsService.get("accountLabel"));
                        final UserExt reported = userMapper.get(dataId);
                        reportData = UserExtUtil.getUserLink(reported);

                        break;
                    default:
                        LOGGER.error( "Unknown report data type [" + dataType + "]");

                        continue;
                }
                report.put(ReportUtil.REPORT_T_DATA, reportData);
            } catch (final Exception e) {
                LOGGER.error( "Builds report data failed", e);

                continue;
            }

            final int type = record.getReportType();
            report.put(ReportUtil.REPORT_TYPE, type);
            switch (type) {
                case ReportUtil.REPORT_TYPE_C_SPAM_AD:
                    report.put(ReportUtil.REPORT_T_TYPE_STR, langPropsService.get("spamADLabel"));

                    break;
                case ReportUtil.REPORT_TYPE_C_PORNOGRAPHIC:
                    report.put(ReportUtil.REPORT_T_TYPE_STR, langPropsService.get("pornographicLabel"));

                    break;
                case ReportUtil.REPORT_TYPE_C_VIOLATION_OF_REGULATIONS:
                    report.put(ReportUtil.REPORT_T_TYPE_STR, langPropsService.get("violationOfRegulationsLabel"));

                    break;
                case ReportUtil.REPORT_TYPE_C_ALLEGEDLY_INFRINGING:
                    report.put(ReportUtil.REPORT_T_TYPE_STR, langPropsService.get("allegedlyInfringingLabel"));

                    break;
                case ReportUtil.REPORT_TYPE_C_PERSONAL_ATTACKS:
                    report.put(ReportUtil.REPORT_T_TYPE_STR, langPropsService.get("personalAttacksLabel"));

                    break;
                case ReportUtil.REPORT_TYPE_C_POSING_ACCOUNT:
                    report.put(ReportUtil.REPORT_T_TYPE_STR, langPropsService.get("posingAccountLabel"));

                    break;
                case ReportUtil.REPORT_TYPE_C_SPAM_AD_ACCOUNT:
                    report.put(ReportUtil.REPORT_T_TYPE_STR, langPropsService.get("spamADAccountLabel"));

                    break;
                case ReportUtil.REPORT_TYPE_C_PERSONAL_INFO_VIOLATION:
                    report.put(ReportUtil.REPORT_T_TYPE_STR, langPropsService.get("personalInfoViolationLabel"));

                    break;
                case ReportUtil.REPORT_TYPE_C_OTHER:
                    report.put(ReportUtil.REPORT_T_TYPE_STR, langPropsService.get("miscLabel"));

                    break;
                default:
                    LOGGER.error( "Unknown report type [" + type + "]");

                    continue;
            }

            String memo = record.getReportMemo();
            memo = Markdowns.toHTML(memo);
            memo = Markdowns.clean(memo, "");
            report.put(ReportUtil.REPORT_MEMO, memo);
            report.put(ReportUtil.REPORT_HANDLED, record.getReportHandled());

            reports.add(report);
        }
        ret.put(ReportUtil.REPORTS, reports);

        return ret;
    }
}

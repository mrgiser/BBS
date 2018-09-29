package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.NotificationUtil;
import cn.he.zhao.bbs.entityUtil.PointtransferUtil;
import cn.he.zhao.bbs.entityUtil.ReportUtil;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import cn.he.zhao.bbs.service.interf.LangPropsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

/**
 * ReportUtil management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.0, Jul 15, 2018
 * @since 3.1.0
 */
@Service
public class ReportMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportMgmtService.class);

    /**
     * ReportUtil Mapper.
     */
    @Autowired
    private ReportMapper reportMapper;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * PointtransferUtil management service.
     */
    @Autowired
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * NotificationUtil management service.
     */
    @Autowired
    private NotificationMgmtService notificationMgmtService;

    /**
     * Makes the specified report as ignored.
     *
     * @param reportId the specified report id
     */
    @Transactional
    public void makeReportIgnored(final String reportId) {
        try {
            final Report report = reportMapper.getByOId(reportId);
            report.setReportHandled( ReportUtil.REPORT_HANDLED_C_IGNORED);

            reportMapper.updateByPrimaryKey( report);
        } catch (final Exception e) {
            LOGGER.error( "Makes report [id=" + reportId + "] as ignored failed", e);
        }
    }

    /**
     * Makes the specified report as handled.
     *
     * @param reportId the specified report id
     */
    @Transactional
    public void makeReportHandled(final String reportId) {
        try {
            final Report report = reportMapper.getByOId(reportId);
            report.setReportHandled( ReportUtil.REPORT_HANDLED_C_YES);

            reportMapper.updateByPrimaryKey(report);

            final String reporterId = report.getReportUserId();
            final String transferId = pointtransferMgmtService.transfer(PointtransferUtil.ID_C_SYS, reporterId,
                    PointtransferUtil.TRANSFER_TYPE_C_REPORT_HANDLED, PointtransferUtil.TRANSFER_SUM_C_REPORT_HANDLED, reportId, System.currentTimeMillis());

            final JSONObject notification = new JSONObject();
            notification.put(NotificationUtil.NOTIFICATION_USER_ID, reporterId);
            notification.put(NotificationUtil.NOTIFICATION_DATA_ID, transferId);
            notificationMgmtService.addReportHandledNotification(notification);
        } catch (final Exception e) {
            LOGGER.error( "Makes report [id=" + reportId + "] as handled failed", e);
        }
    }

    /**
     * Adds a report.
     *
     * @param report the specified report, for example,
     *               {
     *               "reportUserId": "",
     *               "reportDataId": "",
     *               "reportDataType": int,
     *               "reportType": int,
     *               "reportMemo": ""
     *               }
     * @throws Exception service exception
     */
    @Transactional
    public void addReport(final Report report) throws Exception {
        report.setReportHandled( ReportUtil.REPORT_HANDLED_C_NOT);

        try {
            reportMapper.add(report);
        } catch (final Exception e) {
            LOGGER.error( "Adds a report failed", e);

            throw new Exception(langPropsService.get("systemErrLabel"));
        }
    }
}

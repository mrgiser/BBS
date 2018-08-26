package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

/**
 * Report management service.
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
     * Report Mapper.
     */
    @Autowired
    private ReportMapper reportMapper;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Pointtransfer management service.
     */
    @Autowired
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Notification management service.
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
            final JSONObject report = reportMapper.get(reportId);
            report.put(Report.REPORT_HANDLED, Report.REPORT_HANDLED_C_IGNORED);

            reportMapper.update(reportId, report);
        } catch (final Exception e) {
            LOGGER.error( "Makes report [id=" + reportId + "] as ignored failed", e);
        }
    }

    /**
     * Makes the specified report as handled.
     *
     * @param reportId the specified report id
     */
    public void makeReportHandled(final String reportId) {
        final Transaction transaction = reportMapper.beginTransaction();
        try {
            final JSONObject report = reportMapper.get(reportId);
            report.put(Report.REPORT_HANDLED, Report.REPORT_HANDLED_C_YES);

            reportMapper.update(reportId, report);
            transaction.commit();

            final String reporterId = report.optString(Report.REPORT_USER_ID);
            final String transferId = pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, reporterId,
                    Pointtransfer.TRANSFER_TYPE_C_REPORT_HANDLED, Pointtransfer.TRANSFER_SUM_C_REPORT_HANDLED, reportId, System.currentTimeMillis());

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, reporterId);
            notification.put(Notification.NOTIFICATION_DATA_ID, transferId);
            notificationMgmtService.addReportHandledNotification(notification);
        } catch (final Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

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
     * @throws ServiceException service exception
     */
    @Transactional
    public void addReport(final JSONObject report) throws ServiceException {
        report.put(Report.REPORT_HANDLED, Report.REPORT_HANDLED_C_NOT);

        try {
            reportMapper.add(report);
        } catch (final Exception e) {
            LOGGER.error( "Adds a report failed", e);

            throw new ServiceException(langPropsService.get("systemErrLabel"));
        }
    }
}

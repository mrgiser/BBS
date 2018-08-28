package cn.he.zhao.bbs.entity;

public class Report {

    private String oid;

    /**
     * Key of report user id.
     */
    private String reportUserId;

    /**
     * Key of report data id.
     */
    private String reportDataId;

    /**
     * Key of report data type.
     */
    private Integer reportDataType;

    /**
     * Key of report type.
     */
    private Integer reportType;

    /**
     * Key of report memo.
     */
    private String reportMemo;

    /**
     * Key of report handled.
     */
    private Integer reportHandled;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getReportUserId() {
        return reportUserId;
    }

    public void setReportUserId(String reportUserId) {
        this.reportUserId = reportUserId;
    }

    public String getReportDataId() {
        return reportDataId;
    }

    public void setReportDataId(String reportDataId) {
        this.reportDataId = reportDataId;
    }

    public Integer getReportDataType() {
        return reportDataType;
    }

    public void setReportDataType(Integer reportDataType) {
        this.reportDataType = reportDataType;
    }

    public Integer getReportType() {
        return reportType;
    }

    public void setReportType(Integer reportType) {
        this.reportType = reportType;
    }

    public String getReportMemo() {
        return reportMemo;
    }

    public void setReportMemo(String reportMemo) {
        this.reportMemo = reportMemo;
    }

    public Integer getReportHandled() {
        return reportHandled;
    }

    public void setReportHandled(Integer reportHandled) {
        this.reportHandled = reportHandled;
    }
}

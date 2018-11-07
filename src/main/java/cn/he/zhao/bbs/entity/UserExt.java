package cn.he.zhao.bbs.entity;

import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.util.Symphonys;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.util.Date;

public class UserExt {

    private String oid;
    private Integer userNo;
    private String userEmail;
    private String userName;
    private String userPassword;
    private String userNickname;
    private String userTags;
    private String userURL;
    private String userQQ;
    private String userIntro;
    private Integer userAvatarType;
    private String userAvatarURL;
    private String userOnlineFlag;
    private String userB3Key;
    private String userB3ClientAddArticleURL;
    private String userB3ClientUpdateArticleURL;
    private String userB3ClientAddCommentURL;
    private String userRole;
    private Long userUpdateTime;
    private String userCity;
    private String userCountry;
    private String userProvince;
    private String userSkin;
    private String userMobileSkin;
    private Long userCheckinTime;
    private Integer userLongestCheckinStreakStart;
    private Integer userLongestCheckinStreakEnd;
    private Integer userCurrentCheckinStreakStart;
    private Integer userCurrentCheckinStreakEnd;
    private Integer userLongestCheckinStreak;
    private Integer userCurrentCheckinStreak;
    private Integer userArticleCount;
    private Integer userCommentCount;
    private Integer userTagCount;
    private Integer userStatus;
    private Integer userPoint;
    private Integer userUsedPoint;
    private Integer userJoinPointRank;
    private Integer userJoinUsedPointRank;
    private Long userLatestArticleTime;
    private Long userLatestCmtTime;
    private Long userLatestLoginTime;
    private String userLatestLoginIP;
    private Integer userAppRole;
    private Integer userCommentViewMode;
    private String syncWithSymphonyClient;
    private Integer userGeoStatus;
    private Integer userAvatarViewMode;
    private Integer userListPageSize;
    private Integer userListViewMode;
    private Integer userForgeLinkStatus;
    private Integer userBreezemoonStatus;
    private Integer userPointStatus;
    private Integer userFollowerStatus;
    private Integer userFollowingArticleStatus;
    private Integer userWatchingArticleStatus;
    private Integer userFollowingTagStatus;
    private Integer userFollowingUserStatus;
    private Integer userCommentStatus;
    private Integer userArticleStatus;
    private Integer userOnlineStatus;
    private Integer userTimelineStatus;
    private Integer userUAStatus;
    private Integer userNotifyStatus;
    private Integer userSubMailStatus;
    private Long userSubMailSendTime;
    private Integer userKeyboardShortcutsStatus;
    private Integer userReplyWatchArticleStatus;
    private Integer userGuideStep;
    private String userLanguage;
    private String userTimezone;

    private transient String userPointHex;
    private transient String userPointCC;
    private transient Date userCreateTime;
    private transient String roleName;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Date getUserCreateTime() {
        return userCreateTime;
    }

    public void setUserCreateTime(Date userCreateTime) {
        this.userCreateTime = userCreateTime;
    }

    public String getUserPointCC() {
        return userPointCC;
    }

    public void setUserPointCC(String userPointCC) {
        this.userPointCC = userPointCC;
    }

    public String getUserPointHex() {
        return userPointHex;
    }

    public void setUserPointHex(String userPointHex) {
        this.userPointHex = userPointHex;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Integer getUserNo() {
        return userNo;
    }

    public void setUserNo(Integer userNo) {
        this.userNo = userNo;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getUserTags() {
        return userTags;
    }

    public void setUserTags(String userTags) {
        this.userTags = userTags;
    }

    public String getUserURL() {
        return userURL;
    }

    public void setUserURL(String userURL) {
        this.userURL = userURL;
    }

    public String getUserQQ() {
        return userQQ;
    }

    public void setUserQQ(String userQQ) {
        this.userQQ = userQQ;
    }

    public String getUserIntro() {
        return userIntro;
    }

    public void setUserIntro(String userIntro) {
        this.userIntro = userIntro;
    }

    public Integer getUserAvatarType() {
        return userAvatarType;
    }

    public void setUserAvatarType(Integer userAvatarType) {
        this.userAvatarType = userAvatarType;
    }

    public String getUserAvatarURL() {
        return userAvatarURL;
    }

    public void setUserAvatarURL(String userAvatarURL) {
        this.userAvatarURL = userAvatarURL;
    }

    public String getUserOnlineFlag() {
        return userOnlineFlag;
    }

    public void setUserOnlineFlag(String userOnlineFlag) {
        this.userOnlineFlag = userOnlineFlag;
    }

    public String getUserB3Key() {
        return userB3Key;
    }

    public void setUserB3Key(String userB3Key) {
        this.userB3Key = userB3Key;
    }

    public String getUserB3ClientAddArticleURL() {
        return userB3ClientAddArticleURL;
    }

    public void setUserB3ClientAddArticleURL(String userB3ClientAddArticleURL) {
        this.userB3ClientAddArticleURL = userB3ClientAddArticleURL;
    }

    public String getUserB3ClientUpdateArticleURL() {
        return userB3ClientUpdateArticleURL;
    }

    public void setUserB3ClientUpdateArticleURL(String userB3ClientUpdateArticleURL) {
        this.userB3ClientUpdateArticleURL = userB3ClientUpdateArticleURL;
    }

    public String getUserB3ClientAddCommentURL() {
        return userB3ClientAddCommentURL;
    }

    public void setUserB3ClientAddCommentURL(String userB3ClientAddCommentURL) {
        this.userB3ClientAddCommentURL = userB3ClientAddCommentURL;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public Long getUserUpdateTime() {
        return userUpdateTime;
    }

    public void setUserUpdateTime(Long userUpdateTime) {
        this.userUpdateTime = userUpdateTime;
    }

    public String getUserCity() {
        return userCity;
    }

    public void setUserCity(String userCity) {
        this.userCity = userCity;
    }

    public String getUserCountry() {
        return userCountry;
    }

    public void setUserCountry(String userCountry) {
        this.userCountry = userCountry;
    }

    public String getUserProvince() {
        return userProvince;
    }

    public void setUserProvince(String userProvince) {
        this.userProvince = userProvince;
    }

    public String getUserSkin() {
        return userSkin;
    }

    public void setUserSkin(String userSkin) {
        this.userSkin = userSkin;
    }

    public String getUserMobileSkin() {
        return userMobileSkin;
    }

    public void setUserMobileSkin(String userMobileSkin) {
        this.userMobileSkin = userMobileSkin;
    }

    public Long getUserCheckinTime() {
        return userCheckinTime;
    }

    public void setUserCheckinTime(Long userCheckinTime) {
        this.userCheckinTime = userCheckinTime;
    }

    public Integer getUserLongestCheckinStreakStart() {
        return userLongestCheckinStreakStart;
    }

    public void setUserLongestCheckinStreakStart(Integer userLongestCheckinStreakStart) {
        this.userLongestCheckinStreakStart = userLongestCheckinStreakStart;
    }

    public Integer getUserLongestCheckinStreakEnd() {
        return userLongestCheckinStreakEnd;
    }

    public void setUserLongestCheckinStreakEnd(Integer userLongestCheckinStreakEnd) {
        this.userLongestCheckinStreakEnd = userLongestCheckinStreakEnd;
    }

    public Integer getUserCurrentCheckinStreakStart() {
        return userCurrentCheckinStreakStart;
    }

    public void setUserCurrentCheckinStreakStart(Integer userCurrentCheckinStreakStart) {
        this.userCurrentCheckinStreakStart = userCurrentCheckinStreakStart;
    }

    public Integer getUserCurrentCheckinStreakEnd() {
        return userCurrentCheckinStreakEnd;
    }

    public void setUserCurrentCheckinStreakEnd(Integer userCurrentCheckinStreakEnd) {
        this.userCurrentCheckinStreakEnd = userCurrentCheckinStreakEnd;
    }

    public Integer getUserLongestCheckinStreak() {
        return userLongestCheckinStreak;
    }

    public void setUserLongestCheckinStreak(Integer userLongestCheckinStreak) {
        this.userLongestCheckinStreak = userLongestCheckinStreak;
    }

    public Integer getUserCurrentCheckinStreak() {
        return userCurrentCheckinStreak;
    }

    public void setUserCurrentCheckinStreak(Integer userCurrentCheckinStreak) {
        this.userCurrentCheckinStreak = userCurrentCheckinStreak;
    }

    public Integer getUserArticleCount() {
        return userArticleCount;
    }

    public void setUserArticleCount(Integer userArticleCount) {
        this.userArticleCount = userArticleCount;
    }

    public Integer getUserCommentCount() {
        return userCommentCount;
    }

    public void setUserCommentCount(Integer userCommentCount) {
        this.userCommentCount = userCommentCount;
    }

    public Integer getUserTagCount() {
        return userTagCount;
    }

    public void setUserTagCount(Integer userTagCount) {
        this.userTagCount = userTagCount;
    }

    public Integer getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }

    public Integer getUserPoint() {
        return userPoint;
    }

    public void setUserPoint(Integer userPoint) {
        this.userPoint = userPoint;
    }

    public Integer getUserUsedPoint() {
        return userUsedPoint;
    }

    public void setUserUsedPoint(Integer userUsedPoint) {
        this.userUsedPoint = userUsedPoint;
    }

    public Integer getUserJoinPointRank() {
        return userJoinPointRank;
    }

    public void setUserJoinPointRank(Integer userJoinPointRank) {
        this.userJoinPointRank = userJoinPointRank;
    }

    public Integer getUserJoinUsedPointRank() {
        return userJoinUsedPointRank;
    }

    public void setUserJoinUsedPointRank(Integer userJoinUsedPointRank) {
        this.userJoinUsedPointRank = userJoinUsedPointRank;
    }

    public Long getUserLatestArticleTime() {
        return userLatestArticleTime;
    }

    public void setUserLatestArticleTime(Long userLatestArticleTime) {
        this.userLatestArticleTime = userLatestArticleTime;
    }

    public Long getUserLatestCmtTime() {
        return userLatestCmtTime;
    }

    public void setUserLatestCmtTime(Long userLatestCmtTime) {
        this.userLatestCmtTime = userLatestCmtTime;
    }

    public Long getUserLatestLoginTime() {
        return userLatestLoginTime;
    }

    public void setUserLatestLoginTime(Long userLatestLoginTime) {
        this.userLatestLoginTime = userLatestLoginTime;
    }

    public String getUserLatestLoginIP() {
        return userLatestLoginIP;
    }

    public void setUserLatestLoginIP(String userLatestLoginIP) {
        this.userLatestLoginIP = userLatestLoginIP;
    }

    public Integer getUserAppRole() {
        return userAppRole;
    }

    public void setUserAppRole(Integer userAppRole) {
        this.userAppRole = userAppRole;
    }

    public Integer getUserCommentViewMode() {
        return userCommentViewMode;
    }

    public void setUserCommentViewMode(Integer userCommentViewMode) {
        this.userCommentViewMode = userCommentViewMode;
    }

    public String getSyncWithSymphonyClient() {
        return syncWithSymphonyClient;
    }

    public void setSyncWithSymphonyClient(String syncWithSymphonyClient) {
        this.syncWithSymphonyClient = syncWithSymphonyClient;
    }

    public Integer getUserGeoStatus() {
        return userGeoStatus;
    }

    public void setUserGeoStatus(Integer userGeoStatus) {
        this.userGeoStatus = userGeoStatus;
    }

    public Integer getUserAvatarViewMode() {
        return userAvatarViewMode;
    }

    public void setUserAvatarViewMode(Integer userAvatarViewMode) {
        this.userAvatarViewMode = userAvatarViewMode;
    }

    public Integer getUserListPageSize() {
        return userListPageSize;
    }

    public void setUserListPageSize(Integer userListPageSize) {
        this.userListPageSize = userListPageSize;
    }

    public Integer getUserListViewMode() {
        return userListViewMode;
    }

    public void setUserListViewMode(Integer userListViewMode) {
        this.userListViewMode = userListViewMode;
    }

    public Integer getUserForgeLinkStatus() {
        return userForgeLinkStatus;
    }

    public void setUserForgeLinkStatus(Integer userForgeLinkStatus) {
        this.userForgeLinkStatus = userForgeLinkStatus;
    }

    public Integer getUserBreezemoonStatus() {
        return userBreezemoonStatus;
    }

    public void setUserBreezemoonStatus(Integer userBreezemoonStatus) {
        this.userBreezemoonStatus = userBreezemoonStatus;
    }

    public Integer getUserPointStatus() {
        return userPointStatus;
    }

    public void setUserPointStatus(Integer userPointStatus) {
        this.userPointStatus = userPointStatus;
    }

    public Integer getUserFollowerStatus() {
        return userFollowerStatus;
    }

    public void setUserFollowerStatus(Integer userFollowerStatus) {
        this.userFollowerStatus = userFollowerStatus;
    }

    public Integer getUserFollowingArticleStatus() {
        return userFollowingArticleStatus;
    }

    public void setUserFollowingArticleStatus(Integer userFollowingArticleStatus) {
        this.userFollowingArticleStatus = userFollowingArticleStatus;
    }

    public Integer getUserWatchingArticleStatus() {
        return userWatchingArticleStatus;
    }

    public void setUserWatchingArticleStatus(Integer userWatchingArticleStatus) {
        this.userWatchingArticleStatus = userWatchingArticleStatus;
    }

    public Integer getUserFollowingTagStatus() {
        return userFollowingTagStatus;
    }

    public void setUserFollowingTagStatus(Integer userFollowingTagStatus) {
        this.userFollowingTagStatus = userFollowingTagStatus;
    }

    public Integer getUserFollowingUserStatus() {
        return userFollowingUserStatus;
    }

    public void setUserFollowingUserStatus(Integer userFollowingUserStatus) {
        this.userFollowingUserStatus = userFollowingUserStatus;
    }

    public Integer getUserCommentStatus() {
        return userCommentStatus;
    }

    public void setUserCommentStatus(Integer userCommentStatus) {
        this.userCommentStatus = userCommentStatus;
    }

    public Integer getUserArticleStatus() {
        return userArticleStatus;
    }

    public void setUserArticleStatus(Integer userArticleStatus) {
        this.userArticleStatus = userArticleStatus;
    }

    public Integer getUserOnlineStatus() {
        return userOnlineStatus;
    }

    public void setUserOnlineStatus(Integer userOnlineStatus) {
        this.userOnlineStatus = userOnlineStatus;
    }

    public Integer getUserTimelineStatus() {
        return userTimelineStatus;
    }

    public void setUserTimelineStatus(Integer userTimelineStatus) {
        this.userTimelineStatus = userTimelineStatus;
    }

    public Integer getUserUAStatus() {
        return userUAStatus;
    }

    public void setUserUAStatus(Integer userUAStatus) {
        this.userUAStatus = userUAStatus;
    }

    public Integer getUserNotifyStatus() {
        return userNotifyStatus;
    }

    public void setUserNotifyStatus(Integer userNotifyStatus) {
        this.userNotifyStatus = userNotifyStatus;
    }

    public Integer getUserSubMailStatus() {
        return userSubMailStatus;
    }

    public void setUserSubMailStatus(Integer userSubMailStatus) {
        this.userSubMailStatus = userSubMailStatus;
    }

    public Long getUserSubMailSendTime() {
        return userSubMailSendTime;
    }

    public void setUserSubMailSendTime(Long userSubMailSendTime) {
        this.userSubMailSendTime = userSubMailSendTime;
    }

    public Integer getUserKeyboardShortcutsStatus() {
        return userKeyboardShortcutsStatus;
    }

    public void setUserKeyboardShortcutsStatus(Integer userKeyboardShortcutsStatus) {
        this.userKeyboardShortcutsStatus = userKeyboardShortcutsStatus;
    }

    public Integer getUserReplyWatchArticleStatus() {
        return userReplyWatchArticleStatus;
    }

    public void setUserReplyWatchArticleStatus(Integer userReplyWatchArticleStatus) {
        this.userReplyWatchArticleStatus = userReplyWatchArticleStatus;
    }

    public Integer getUserGuideStep() {
        return userGuideStep;
    }

    public void setUserGuideStep(Integer userGuideStep) {
        this.userGuideStep = userGuideStep;
    }

    public String getUserLanguage() {
        return userLanguage;
    }

    public void setUserLanguage(String userLanguage) {
        this.userLanguage = userLanguage;
    }

    public String getUserTimezone() {
        return userTimezone;
    }

    public void setUserTimezone(String userTimezone) {
        this.userTimezone = userTimezone;
    }
}

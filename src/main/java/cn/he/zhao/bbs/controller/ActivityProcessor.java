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
package cn.he.zhao.bbs.controller;

import cn.he.zhao.bbs.advice.*;
import cn.he.zhao.bbs.exception.RequestProcessAdviceException;
import cn.he.zhao.bbs.model.*;
import cn.he.zhao.bbs.model.my.*;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Requests;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.util.GeetestLib;
import cn.he.zhao.bbs.util.Results;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.validate.Activity1A0001CollectValidation;
import cn.he.zhao.bbs.validate.Activity1A0001Validation;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Key;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Activity processor.
 * <ul>
 * <li>Shows activities (/activities), GET</li>
 * <li>Daily checkin (/activity/daily-checkin), GET</li>
 * <li>Shows 1A0001 (/activity/1A0001), GET</li>
 * <li>Bets 1A0001 (/activity/1A0001/bet), POST</li>
 * <li>Collects 1A0001 (/activity/1A0001/collect), POST</li>
 * <li>Shows character (/activity/character), GET</li>
 * <li>Submit character (/activity/character/submit), POST</li>
 * <li>Shows eating snake (/activity/eating-snake), GET</li>
 * <li>Starts eating snake (/activity/eating-snake/start), POST</li>
 * <li>Collects eating snake(/activity/eating-snake/collect), POST</li>
 * <li>Shows gobang (/activity/gobang), GET</li>
 * <li>Starts gobang (/activity/gobang/start), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.9.1.12, Jun 18, 2018
 * @since 1.3.0
 */
@Controller
public class ActivityProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityProcessor.class);

    /**
     * Activity management service.
     */
    @Autowired
    private ActivityMgmtService activityMgmtService;

    /**
     * Activity query service.
     */
    @Autowired
    private ActivityQueryService activityQueryService;

    /**
     * Character query service.
     */
    @Autowired
    private CharacterQueryService characterQueryService;

    /**
     * Pointtransfer query service.
     */
    @Autowired
    private PointtransferQueryService pointtransferQueryService;

    /**
     * Data model service.
     */
    @Autowired
    private DataModelService dataModelService;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Shows 1A0001.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/activity/character", method = RequestMethod.GET)
    @StopWatchStartAnno
    @LoginCheckAnno
    @CSRFTokenAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showCharacter(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/activity/character.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String result = "/activity/character.ftl";

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final String userId = user.optString(Keys.OBJECT_ID);

        String activityCharacterGuideLabel = langPropsService.get("activityCharacterGuideLabel");

        final String character = characterQueryService.getUnwrittenCharacter(userId);
        if (StringUtils.isBlank(character)) {
            dataModel.put("noCharacter", true);

            return result;
        }

        final int totalCharacterCount = characterQueryService.getTotalCharacterCount();
        final int writtenCharacterCount = characterQueryService.getWrittenCharacterCount();
        final String totalProgress = String.format("%.2f", (double) writtenCharacterCount / (double) totalCharacterCount * 100);
        dataModel.put("totalProgress", totalProgress);

        final int userWrittenCharacterCount = characterQueryService.getWrittenCharacterCount(userId);
        final String userProgress = String.format("%.2f", (double) userWrittenCharacterCount / (double) totalCharacterCount * 100);
        dataModel.put("userProgress", userProgress);

        activityCharacterGuideLabel = activityCharacterGuideLabel.replace("{character}", character);
        dataModel.put("activityCharacterGuideLabel", activityCharacterGuideLabel);

        return result;
    }

    /**
     * Submits character.
     *
     * @param request the specified request
     */
    @RequestMapping(value = "/activity/character/submit", method = RequestMethod.POST)
    @StopWatchStartAnno
    @LoginCheckAnno
    @StopWatchEndAnno
    public void submitCharacter(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) {
//        context.renderJSON().renderFalseResult();
        dataModel.put(Keys.STATUS_CODE,false);

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.error( "Submits character failed", e);

//            context.renderJSON(false).renderMsg(langPropsService.get("activityCharacterRecognizeFailedLabel"));
            dataModel.put(Keys.STATUS_CODE,false);
            dataModel.put(Keys.MSG, langPropsService.get("activityCharacterRecognizeFailedLabel"));

            return;
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final String dataURL = requestJSONObject.optString("dataURL");
        final String dataPart = StringUtils.substringAfter(dataURL, ",");
        final String character = requestJSONObject.optString("character");

        final JSONObject result = activityMgmtService.submitCharacter(userId, dataPart, character);
//        context.renderJSON(result);
        dataModel.put(Keys.STATUS_CODE,result.get(Keys.STATUS_CODE));
        dataModel.put(Keys.MSG,result.get(Keys.MSG));
    }

    /**
     * Shows activity page.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/activities", method = RequestMethod.GET)
    @StopWatchStartAnno
    @LoginCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showActivities(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/home/activities.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String result = "/home/activities.ftl";

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        dataModel.put("pointActivityCheckinMin", Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MIN);
        dataModel.put("pointActivityCheckinMax", Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MAX);
        dataModel.put("pointActivityCheckinStreak", Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKINT_STREAK);
        dataModel.put("activitYesterdayLivenessRewardMaxPoint",
                Symphonys.getInt("activitYesterdayLivenessReward.maxPoint"));
        return result;
    }

    /**
     * Shows daily checkin page.
     *
//     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/activity/checkin", method = RequestMethod.GET)
    @StopWatchStartAnno
    @LoginCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showDailyCheckin(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final String userId = user.optString(Keys.OBJECT_ID);
        if (activityQueryService.isCheckedinToday(userId)) {
//            response.sendRedirect(SpringUtil.getServerPath() + "/member/" + user.optString(User.USER_NAME) + "/points");

            return "redirect:" + SpringUtil.getServerPath() + "/member/" + user.optString(User.USER_NAME) + "/points";
        }

//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/activity/checkin.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String result = "/activity/checkin.ftl";

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        return result;
    }

    /**
     * Daily checkin.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/activity/daily-checkin", method = RequestMethod.GET)
    @StopWatchStartAnno
    @LoginCheckAnno
    @StopWatchEndAnno
    public String dailyCheckin(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final String userId = user.optString(Keys.OBJECT_ID);

        if (!Symphonys.getBoolean("geetest.enabled")) {
            activityMgmtService.dailyCheckin(userId);
        } else {
            final String challenge = request.getParameter(GeetestLib.fn_geetest_challenge);
            final String validate = request.getParameter(GeetestLib.fn_geetest_validate);
            final String seccode = request.getParameter(GeetestLib.fn_geetest_seccode);
            if (StringUtils.isBlank(challenge) || StringUtils.isBlank(validate) || StringUtils.isBlank(seccode)) {
//                response.sendRedirect(SpringUtil.getServerPath() + "/member/" + user.optString(User.USER_NAME) + "/points");

                return "redirect:" + SpringUtil.getServerPath() + "/member/" + user.optString(User.USER_NAME) + "/points";
            }

            final GeetestLib gtSdk = new GeetestLib(Symphonys.get("geetest.id"), Symphonys.get("geetest.key"));
            final int gt_server_status_code = (Integer) request.getSession().getAttribute(gtSdk.gtServerStatusSessionKey);
            int gtResult = 0;
            if (gt_server_status_code == 1) {
                gtResult = gtSdk.enhencedValidateRequest(challenge, validate, seccode, userId);
            } else {
                gtResult = gtSdk.failbackValidateRequest(challenge, validate, seccode);
            }

            if (gtResult == 1) {
                activityMgmtService.dailyCheckin(userId);
            }
        }

//        response.sendRedirect(SpringUtil.getServerPath() + "/member/" + user.optString(User.USER_NAME) + "/points");
        return "redirect:" + SpringUtil.getServerPath() + "/member/" + user.optString(User.USER_NAME) + "/points";
    }

    /**
     * Yesterday liveness reward.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/activity/yesterday-liveness-reward", method = RequestMethod.GET)
    @StopWatchStartAnno
    @LoginCheckAnno
    @StopWatchEndAnno
    public String yesterdayLivenessReward(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final String userId = user.optString(Keys.OBJECT_ID);

        activityMgmtService.yesterdayLivenessReward(userId);

//        response.sendRedirect(SpringUtil.getServerPath() + "/member/" + user.optString(User.USER_NAME) + "/points");
        return "redirect:" + SpringUtil.getServerPath() + "/member/" + user.optString(User.USER_NAME) + "/points";
    }

    /**
     * Shows 1A0001.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/activity/1A0001", method = RequestMethod.GET)
    @StopWatchStartAnno
    @LoginCheckAnno
    @CSRFTokenAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String show1A0001(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/activity/1A0001.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String result = "/activity/1A0001.ftl";

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        final boolean closed = Symphonys.getBoolean("activity1A0001Closed");
        dataModel.put(Common.CLOSED, closed);

        final Calendar calendar = Calendar.getInstance();
        final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        final boolean closed1A0001 = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
        dataModel.put(Common.CLOSED_1A0001, closed1A0001);

        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        final boolean end = hour > 14 || (hour == 14 && minute > 55);
        dataModel.put(Common.END, end);

        final boolean collected = activityQueryService.isCollected1A0001Today(userId);
        dataModel.put(Common.COLLECTED, collected);

        final boolean participated = activityQueryService.is1A0001Today(userId);
        dataModel.put(Common.PARTICIPATED, participated);

        while (true) {
            if (closed) {
                dataModel.put(Keys.MSG, langPropsService.get("activityClosedLabel"));
                break;
            }

            if (closed1A0001) {
                dataModel.put(Keys.MSG, langPropsService.get("activity1A0001CloseLabel"));
                break;
            }

            if (collected) {
                dataModel.put(Keys.MSG, langPropsService.get("activityParticipatedLabel"));
                break;
            }

            if (participated) {
                dataModel.put(Common.HOUR, hour);

                final List<JSONObject> records = pointtransferQueryService.getLatestPointtransfers(userId,
                        Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_1A0001, 1);
                final JSONObject pointtransfer = records.get(0);
                final String data = pointtransfer.optString(Pointtransfer.DATA_ID);
                final String smallOrLarge = data.split("-")[1];
                final int sum = pointtransfer.optInt(Pointtransfer.SUM);
                String msg = langPropsService.get("activity1A0001BetedLabel");
                final String small = langPropsService.get("activity1A0001BetSmallLabel");
                final String large = langPropsService.get("activity1A0001BetLargeLabel");
                msg = msg.replace("{smallOrLarge}", StringUtils.equals(smallOrLarge, "0") ? small : large);
                msg = msg.replace("{point}", String.valueOf(sum));

                dataModel.put(Keys.MSG, msg);

                break;
            }

            if (end) {
                dataModel.put(Keys.MSG, langPropsService.get("activityEndLabel"));
                break;
            }

            break;
        }

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        return result;
    }

    /**
     * Bets 1A0001.
     *
     * @param request the specified request
     */
    @RequestMapping(value = "/activity/1A0001/bet", method = RequestMethod.POST)
//    @Before(adviceClass = { Activity1A0001Validation.class})
    @StopWatchStartAnno
    @LoginCheckAnno
    @CSRFCheckAnno
    @StopWatchEndAnno
    public void bet1A0001(Map<String, Object> dataModel, final HttpServletRequest request) {
//        context.renderJSON().renderFalseResult();
        dataModel.put(Keys.STATUS_CODE,false);

        try {
            Activity1A0001Validation.doAdvice(request);
        } catch (RequestProcessAdviceException e) {
            dataModel.put(Keys.MSG,e.getJsonObject().get(Keys.MSG));
            return;
        }

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final int amount = requestJSONObject.optInt(Common.AMOUNT);
        final int smallOrLarge = requestJSONObject.optInt(Common.SMALL_OR_LARGE);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String fromId = currentUser.optString(Keys.OBJECT_ID);

        final JSONObject ret = activityMgmtService.bet1A0001(fromId, amount, smallOrLarge);

        if (ret.optBoolean(Keys.STATUS_CODE)) {
            String msg = langPropsService.get("activity1A0001BetedLabel");
            final String small = langPropsService.get("activity1A0001BetSmallLabel");
            final String large = langPropsService.get("activity1A0001BetLargeLabel");
            msg = msg.replace("{smallOrLarge}", smallOrLarge == 0 ? small : large);
            msg = msg.replace("{point}", String.valueOf(amount));

//            context.renderTrueResult().renderMsg(msg);
            dataModel.put(Keys.STATUS_CODE,true);
            dataModel.put(Keys.MSG,msg);
        }
    }

    /**
     * Collects 1A0001.
     *
     * @param request the specified request
     */
    @RequestMapping(value = "/activity/1A0001/collect", method = RequestMethod.POST)
//    @Before(adviceClass = { Activity1A0001CollectValidation.class})
    @StopWatchStartAnno
    @LoginCheckAnno
    @StopWatchEndAnno
    public void collect1A0001(Map<String, Object> dataModel, final HttpServletRequest request) {

        try {
            Activity1A0001CollectValidation.doAdvice(request);
        } catch (RequestProcessAdviceException e) {
            dataModel.put(Keys.MSG, e.getJsonObject().get(Keys.MSG));
            return;
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        final JSONObject ret = activityMgmtService.collect1A0001(userId);

        dataModel.put(Keys.MSG,ret.get(Keys.MSG));
        dataModel.put(Keys.STATUS_CODE,ret.get(Keys.STATUS_CODE));
    }

    /**
     * Shows eating snake.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/activity/eating-snake", method = RequestMethod.GET)
    @StopWatchStartAnno
    @LoginCheckAnno
    @CSRFTokenAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showEatingSnake(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
////        context.setRenderer(renderer);
////        renderer.setTemplateName("/activity/eating-snake.ftl");
////
////        final Map<String, Object> dataModel = renderer.getDataModel();

        String result = "/activity/eating-snake.ftl";

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        final List<JSONObject> maxUsers = activityQueryService.getTopEatingSnakeUsersMax(avatarViewMode, 10);
        dataModel.put("maxUsers", (Object) maxUsers);

        final List<JSONObject> sumUsers
                = activityQueryService.getTopEatingSnakeUsersSum(avatarViewMode, 10);
        dataModel.put("sumUsers", (Object) sumUsers);

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final String userId = user.optString(Keys.OBJECT_ID);
        final int startPoint = activityQueryService.getEatingSnakeAvgPoint(userId);

        String pointActivityEatingSnake = langPropsService.get("activityStartEatingSnakeTipLabel");
        pointActivityEatingSnake = pointActivityEatingSnake.replace("{point}", String.valueOf(startPoint));
        dataModel.put("activityStartEatingSnakeTipLabel", pointActivityEatingSnake);

        return  result;
    }

    /**
     * Starts eating snake.
     *
     * @param request the specified request
     */
    @RequestMapping(value = "/activity/eating-snake/start", method = RequestMethod.POST)
    @StopWatchStartAnno
    @LoginCheckAnno
    @CSRFCheckAnno
    @StopWatchEndAnno
    public void startEatingSnake(Map<String, Object> dataModel, final HttpServletRequest request) {
        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String fromId = currentUser.optString(Keys.OBJECT_ID);

        final JSONObject ret = activityMgmtService.startEatingSnake(fromId);

        dataModel.put(Keys.STATUS_CODE, ret.get(Keys.STATUS_CODE));
        dataModel.put(Keys.MSG, ret.get(Keys.MSG));
//        context.renderJSON(ret);
    }

    /**
     * Collects eating snake.
     *
     * @param request the specified request
     */
    @RequestMapping(value = "/activity/eating-snake/collect", method = RequestMethod.POST)
    @StopWatchStartAnno
    @LoginCheckAnno
    @CSRFTokenAnno
    @StopWatchEndAnno
    public String collectEatingSnake(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/activity/eating-snake.ftl");
        String result = "/activity/eating-snake.ftl";

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
            final int score = requestJSONObject.optInt("score");

            final JSONObject user = (JSONObject) request.getAttribute(User.USER);

            final JSONObject ret = activityMgmtService.collectEatingSnake(user.optString(Keys.OBJECT_ID), score);

//            context.renderJSON(ret);
            dataModel.put(Keys.STATUS_CODE,ret.get(Keys.STATUS_CODE));
            dataModel.put(Keys.MSG,ret.get(Keys.MSG));
        } catch (final Exception e) {
            LOGGER.error( "Collects eating snake game failed", e);

//            context.renderJSON(false).renderMsg("err....");
            dataModel.put(Keys.STATUS_CODE,false);
            dataModel.put(Keys.MSG, e.getMessage());
        }
        return result;
    }

    /**
     * Shows gobang.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/activity/gobang", method = RequestMethod.GET)
    @StopWatchStartAnno
    @LoginCheckAnno
    @CSRFTokenAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showGobang(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/activity/gobang.ftl");
//
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String result = "/activity/gobang.ftl";


        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);

        String pointActivityGobang = langPropsService.get("activityStartGobangTipLabel");
        pointActivityGobang = pointActivityGobang.replace("{point}", String.valueOf(Pointtransfer.TRANSFER_SUM_C_ACTIVITY_GOBANG_START));
        dataModel.put("activityStartGobangTipLabel", pointActivityGobang);

        return result;
    }

    /**
     * Starts gobang.
     *
     * @param request the specified request
     */
    @RequestMapping(value = "/activity/gobang/start", method = RequestMethod.POST)
    @StopWatchStartAnno
    @LoginCheckAnno
    @StopWatchEndAnno
    public void startGobang(Map<String, Object> dataModel, final HttpServletRequest request) {
        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String fromId = currentUser.optString(Keys.OBJECT_ID);

        final JSONObject ret = Results.falseResult();

        final boolean succ = currentUser.optInt(UserExt.USER_POINT) - Pointtransfer.TRANSFER_SUM_C_ACTIVITY_GOBANG_START >= 0;
        ret.put(Keys.STATUS_CODE, succ);

        final String msg = succ ? "started" : langPropsService.get("activityStartGobangFailLabel");
        ret.put(Keys.MSG, msg);

//        context.renderJSON(ret);
        dataModel.put(Keys.MSG, ret.get(Keys.MSG));
        dataModel.put(Keys.STATUS_CODE, ret.get(Keys.STATUS_CODE));
    }
}

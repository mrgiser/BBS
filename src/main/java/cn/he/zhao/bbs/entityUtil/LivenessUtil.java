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
package cn.he.zhao.bbs.entityUtil;

import cn.he.zhao.bbs.util.Symphonys;
import org.json.JSONObject;

/**
 * This class defines all liveness model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jun 12, 2018
 * @since 1.4.0
 */
public final class LivenessUtil {

    /**
     * LivenessUtil.
     */
    public static final String LIVENESS = "liveness";

    /**
     * Key of user id.
     */
    public static final String LIVENESS_USER_ID = "livenessUserId";

    /**
     * Key of liveness date.
     */
    public static final String LIVENESS_DATE = "livenessDate";

    /**
     * Key of liveness point.
     */
    public static final String LIVENESS_POINT = "livenessPoint";

    /**
     * Key of liveness article.
     */
    public static final String LIVENESS_ARTICLE = "livenessArticle";

    /**
     * Key of liveness comment.
     */
    public static final String LIVENESS_COMMENT = "livenessComment";

    /**
     * Key of liveness activity.
     */
    public static final String LIVENESS_ACTIVITY = "livenessActivity";

    /**
     * Key of liveness thank.
     */
    public static final String LIVENESS_THANK = "livenessThank";

    /**
     * Key of liveness vote.
     */
    public static final String LIVENESS_VOTE = "livenessVote";

    /**
     * Key of liveness reward.
     */
    public static final String LIVENESS_REWARD = "livenessReward";

    /**
     * Key of liveness PV.
     */
    public static final String LIVENESS_PV = "livenessPV";

    /**
     * Key of liveness accept answer.
     */
    public static final String LIVENESS_ACCEPT_ANSWER = "livenessAcceptAnswer";

    /**
     * Calculates point of the specified liveness.
     *
     * @param liveness the specified liveness
     * @return point
     */
    public static int calcPoint(final JSONObject liveness) {
        final float activityPer = Symphonys.getFloat("activitYesterdayLivenessReward.activity.perPoint");
        final float articlePer = Symphonys.getFloat("activitYesterdayLivenessReward.article.perPoint");
        final float commentPer = Symphonys.getFloat("activitYesterdayLivenessReward.comment.perPoint");
        final float pvPer = Symphonys.getFloat("activitYesterdayLivenessReward.pv.perPoint");
        final float rewardPer = Symphonys.getFloat("activitYesterdayLivenessReward.reward.perPoint");
        final float thankPer = Symphonys.getFloat("activitYesterdayLivenessReward.thank.perPoint");
        final float votePer = Symphonys.getFloat("activitYesterdayLivenessReward.vote.perPoint");
        final float acceptAnswerPer = Symphonys.getFloat("activitYesterdayLivenessReward.acceptAnswer.perPoint");

        final int activity = liveness.optInt(LivenessUtil.LIVENESS_ACTIVITY);
        final int article = liveness.optInt(LivenessUtil.LIVENESS_ARTICLE);
        final int comment = liveness.optInt(LivenessUtil.LIVENESS_COMMENT);
        int pv = liveness.optInt(LivenessUtil.LIVENESS_PV);
        if (pv > 50) {
            pv = 50;
        }
        final int reward = liveness.optInt(LivenessUtil.LIVENESS_REWARD);
        final int thank = liveness.optInt(LivenessUtil.LIVENESS_THANK);
        int vote = liveness.optInt(LivenessUtil.LIVENESS_VOTE);
        if (vote > 10) {
            vote = 10;
        }
        final int acceptAnswer = liveness.optInt(LivenessUtil.LIVENESS_ACCEPT_ANSWER);

        final int activityPoint = (int) (activity * activityPer);
        final int articlePoint = (int) (article * articlePer);
        final int commentPoint = (int) (comment * commentPer);
        final int pvPoint = (int) (pv * pvPer);
        final int rewardPoint = (int) (reward * rewardPer);
        final int thankPoint = (int) (thank * thankPer);
        final int votePoint = (int) (vote * votePer);
        final int acceptAnswerPoint = (int) (acceptAnswer * acceptAnswerPer);

        int ret = activityPoint + articlePoint + commentPoint + pvPoint + rewardPoint + thankPoint + votePoint + acceptAnswerPoint;

        final int max = Symphonys.getInt("activitYesterdayLivenessReward.maxPoint");
        if (ret > max) {
            ret = max;
        }

        return ret;
    }

    /**
     * Private constructor.
     */
    private LivenessUtil() {
    }
}

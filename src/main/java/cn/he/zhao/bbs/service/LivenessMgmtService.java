package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import cn.he.zhao.bbs.spring.Stopwatchs;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LivenessMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LivenessMgmtService.class);

    /**
     * LivenessUtil Mapper.
     */
    @Autowired
    private LivenessMapper livenessMapper;

    /**
     * Increments a field of the specified liveness.
     *
     * @param userId the specified user id
     * @param field  the specified field
     */
    @Transactional
    public void incLiveness(final String userId, final String field) {
        Stopwatchs.start("Inc liveness");
        final String date = DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd");

        try {
            Liveness liveness = livenessMapper.getByUserAndDate(userId, date);
            if (null == liveness) {
                liveness = new Liveness();

                liveness.setLivenessUserId( userId);
                liveness.setLivenessDate( date);
                liveness.setLivenessPoint( 0);
                liveness.setLivenessActivity( 0);
                liveness.setLivenessArticle( 0);
                liveness.setLivenessComment( 0);
                liveness.setLivenessPV( 0);
                liveness.setLivenessReward( 0);
                liveness.setLivenessThank( 0);
                liveness.setLivenessVote( 0);
//                liveness.put(Liveness.LIVENESS_VOTE, 0);
                liveness.setLivenessAcceptAnswer( 0);

                livenessMapper.add(liveness);
            }

            // TODO: 2018/9/29 根据字符确定更新哪个属性
            liveness.s(field, liveness.optInt(field) + 1);

            livenessMapper.update(liveness);
        } catch (final Exception e) {
            LOGGER.error( "Updates a liveness [" + date + "] field [" + field + "] failed", e);
        } finally {
            Stopwatchs.end();
        }
    }
}

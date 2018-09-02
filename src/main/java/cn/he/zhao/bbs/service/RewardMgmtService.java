package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entity.Reward;
import cn.he.zhao.bbs.mapper.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

/**
 * RewardUtil management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jun 30, 2015
 * @since 1.3.0
 */
@Service
public class RewardMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RewardMgmtService.class);

    /**
     * RewardUtil Mapper.
     */
    @Autowired
    private RewardMapper rewardMapper;

    /**
     * Adds a reward with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "senderId"; "",
     *     "dataId": "",
     *     "type": int
     * }
     * </pre>
     *
     * @return reward id
     * @throws Exception service exception
     */
    @Transactional
    public String addReward(final Reward reward) throws Exception {
        try {
            return rewardMapper.add(reward);
        } catch (final Exception e) {
            final String msg = "Adds reward failed";
            LOGGER.error( msg, e);

            throw new Exception(msg);
        }
    }
}

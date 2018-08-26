package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.mapper.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

/**
 * Reward management service.
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
     * Reward Mapper.
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
     * @throws ServiceException service exception
     */
    @Transactional
    public String addReward(final JSONObject requestJSONObject) throws ServiceException {
        try {
            return rewardMapper.add(requestJSONObject);
        } catch (final MapperException e) {
            final String msg = "Adds reward failed";
            LOGGER.error( msg, e);

            throw new ServiceException(msg);
        }
    }
}

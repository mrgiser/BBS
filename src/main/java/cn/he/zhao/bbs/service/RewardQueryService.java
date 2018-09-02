package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * RewardUtil query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.1, Oct 17, 2015
 * @since 1.3.0
 */
@Service
public class RewardQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RewardQueryService.class);

    /**
     * RewardUtil Mapper.
     */
    @Autowired
    private RewardMapper rewardMapper;

    /**
     * Gets rewarded count.
     *
     * @param dataId the specified data id
     * @param type the specified type
     * @return rewarded count
     */
    public long rewardedCount(final String dataId, final int type) {
        final Query query = new Query();
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Reward.DATA_ID, FilterOperator.EQUAL, dataId));
        filters.add(new PropertyFilter(Reward.TYPE, FilterOperator.EQUAL, type));

        query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            return rewardMapper.count(query);
        } catch (final MapperException e) {
            LOGGER.error( "Rewarded count error", e);

            return 0;
        }
    }

    /**
     * Determines the user specified by the given user id has rewarded the data (article/comment/user) or not.
     *
     * @param userId the specified user id
     * @param dataId the specified data id
     * @param type the specified type
     * @return {@code true} if has rewared
     */
    public boolean isRewarded(final String userId, final String dataId, final int type) {
        final Query query = new Query();
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Reward.SENDER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Reward.DATA_ID, FilterOperator.EQUAL, dataId));
        filters.add(new PropertyFilter(Reward.TYPE, FilterOperator.EQUAL, type));

        query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            return 0 != rewardMapper.count(query);
        } catch (final MapperException e) {
            LOGGER.error( "Determines reward error", e);

            return false;
        }
    }
}

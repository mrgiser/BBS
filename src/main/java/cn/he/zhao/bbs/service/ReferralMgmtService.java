package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entity.my.*;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

/**
 * Referral management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Apr 28, 2016
 * @since 1.4.0
 */
@Service
public class ReferralMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferralMgmtService.class);

    /**
     * Referral Mapper.
     */
    @Autowired
    private ReferralMapper referralMapper;

    /**
     * Adds or updates a referral.
     *
     * @param referral the specified referral
     */
    @Transactional
    public void updateReferral(final JSONObject referral) {
        final String dataId = referral.optString(Referral.REFERRAL_DATA_ID);
        final String ip = referral.optString(Referral.REFERRAL_IP);

        try {
            JSONObject record = referralMapper.getByDataIdAndIP(dataId, ip);
            if (null == record) {
                record = new JSONObject();
                record.put(Referral.REFERRAL_AUTHOR_HAS_POINT, false);
                record.put(Referral.REFERRAL_CLICK, 1);
                record.put(Referral.REFERRAL_DATA_ID, dataId);
                record.put(Referral.REFERRAL_IP, ip);
                record.put(Referral.REFERRAL_TYPE, referral.optInt(Referral.REFERRAL_TYPE));
                record.put(Referral.REFERRAL_USER, referral.optString(Referral.REFERRAL_USER));
                record.put(Referral.REFERRAL_USER_HAS_POINT, false);

                referralMapper.add(record);
            } else {
                final String currentReferralUser = referral.optString(Referral.REFERRAL_USER);
                final String firstReferralUser = record.optString(Referral.REFERRAL_USER);
                if (!currentReferralUser.equals(firstReferralUser)) {
                    return;
                }

                record.put(Referral.REFERRAL_CLICK, record.optInt(Referral.REFERRAL_CLICK) + 1);

                referralMapper.update(record.optString(Keys.OBJECT_ID), record);
            }
        } catch (final MapperException e) {
            LOGGER.error( "Updates a referral failed", e);
        }
    }
}

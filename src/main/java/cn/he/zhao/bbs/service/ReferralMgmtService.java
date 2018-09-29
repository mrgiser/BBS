package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.ReferralUtil;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

/**
 * ReferralUtil management service.
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
     * ReferralUtil Mapper.
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
        final String dataId = referral.optString(ReferralUtil.REFERRAL_DATA_ID);
        final String ip = referral.optString(ReferralUtil.REFERRAL_IP);

        try {
            Referral record = referralMapper.getByDataIdAndIP(dataId, ip);
            if (null == record) {
                record = new Referral();
                record.setReferralAuthorHasPoint(false);
                record.setReferralClick(1);
                record.setReferralDataId( dataId);
                record.setReferralIP( ip);
                record.setReferralType(referral.optInt(ReferralUtil.REFERRAL_TYPE));
                record.setReferralUser(referral.optString(ReferralUtil.REFERRAL_USER));
                record.setReferralUserHasPoint(false);

                referralMapper.add(record);
            } else {
                final String currentReferralUser = referral.optString(ReferralUtil.REFERRAL_USER);
                final String firstReferralUser = record.getReferralUser();
                if (!currentReferralUser.equals(firstReferralUser)) {
                    return;
                }

                record.setReferralClick(record.getReferralClick() + 1);

                referralMapper.update(record);
            }
        } catch (final Exception e) {
            LOGGER.error( "Updates a referral failed", e);
        }
    }
}

package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.InvitecodeUtil;
import cn.he.zhao.bbs.entityUtil.PointtransferUtil;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InvitecodeMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InvitecodeMgmtService.class);

    /**
     * InvitecodeUtil Mapper.
     */
    @Autowired
    private InvitecodeMapper invitecodeMapper;

    /**
     * Expires invitecodes.
     */
    @Transactional
    public void expireInvitecodes() {
        final long now = System.currentTimeMillis();
        final long expired = now - Symphonys.getLong("invitecode.expired");

//        final Query query = new Query().setCurrentPageNum(1).setPageSize(Integer.MAX_VALUE).
//                setFilter(CompositeFilterOperator.and(
//                        new PropertyFilter(InvitecodeUtil.STATUS, FilterOperator.EQUAL, InvitecodeUtil.STATUS_C_UNUSED),
//                        new PropertyFilter(InvitecodeUtil.GENERATOR_ID, FilterOperator.NOT_EQUAL, PointtransferUtil.ID_C_SYS),
//                        new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN_OR_EQUAL, expired)
//                ));

        PageHelper.startPage(1, Integer.MAX_VALUE);
        List<Invitecode> data;
        try {
            data = invitecodeMapper.getExpiredInvitecodes(InvitecodeUtil.STATUS_C_UNUSED,
                    PointtransferUtil.ID_C_SYS, Long.toString(expired));
        } catch (final Exception e) {
            LOGGER.error("Gets invitecodes failed", e);

            return;
        }

//        final JSONArray data = result.optJSONArray(Keys.RESULTS);

        try {
            for (int i = 0; i < data.size(); i++) {
                final Invitecode invitecode = data.get(i);
                final String invitecodeId = invitecode.getOid();

                invitecodeMapper.remove(invitecodeId);
            }
        } catch (final Exception e) {
            LOGGER.error("Expires invitecodes failed", e);
        }
    }

    /**
     * User generates an invitecode.
     *
     * @param userId   the specified user id
     * @param userName the specified user name
     * @return invitecode
     */
    @Transactional
    public String userGenInvitecode(final String userId, final String userName) {
//        final Transaction transaction = invitecodeMapper.beginTransaction();

        try {
            final String ret = RandomStringUtils.randomAlphanumeric(16);
            final Invitecode invitecode = new Invitecode();
            invitecode.setCode(ret);
            invitecode.setMemo("User [" + userName + "," + userId + "] generated");
            invitecode.setStatus(InvitecodeUtil.STATUS_C_UNUSED);
            invitecode.setGeneratorId(userId);
            invitecode.setUserId("");
            invitecode.setUseTime(0L);

            invitecodeMapper.add(invitecode);

//            transaction.commit();

            return ret;
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error("Generates invitecode failed", e);

            return null;
        }
    }

    /**
     * Admin generates invitecodes with the specified quantity and memo.
     *
     * @param quantity the specified quantity
     * @param memo     the specified memo
     * @throws Exception service exception
     */
    @Transactional
    public void adminGenInvitecodes(final int quantity, final String memo) throws Exception {
//        final Transaction transaction = invitecodeMapper.beginTransaction();

        try {
            for (int i = 0; i < quantity; i++) {
                final Invitecode invitecode = new Invitecode();
                invitecode.setCode(RandomStringUtils.randomAlphanumeric(16));
                invitecode.setMemo(memo);
                invitecode.setStatus(InvitecodeUtil.STATUS_C_UNUSED);
                invitecode.setGeneratorId(PointtransferUtil.ID_C_SYS);
                invitecode.setUserId("");
                invitecode.setUseTime(0L);

                invitecodeMapper.add(invitecode);
            }

//            transaction.commit();
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error("Generates invitecodes failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Updates the specified invitecode by the given invitecode id.
     *
     * @param invitecodeId the given invitecode id
     * @param invitecode   the specified invitecode
     * @throws Exception service exception
     */
    @Transactional
    public void updateInvitecode(final String invitecodeId, final Invitecode invitecode) throws Exception {
//        final Transaction transaction = invitecodeMapper.beginTransaction();

        try {
            invitecodeMapper.update(invitecodeId, invitecode);

//            transaction.commit();
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error("Updates an invitecode[id=" + invitecodeId + "] failed", e);
            throw new Exception(e);
        }
    }
}

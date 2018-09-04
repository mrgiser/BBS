package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Verifycode query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.1, Jun 13, 2018
 * @since 1.3.0
 */
@Service
public class VerifycodeQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VerifycodeQueryService.class);

    /**
     * Verifycode Mapper.
     */
    @Autowired
    private VerifycodeMapper verifycodeMapper;

    /**
     * Gets a verifycode with the specified type, biz type and user id.
     *
     * @param type    the specified type
     * @param bizType the specified biz type
     * @param userId  the specified user id
     * @return verifycode, returns {@code null} if not found
     */
    public Verifycode getVerifycodeByUserId(final int type, final int bizType, final String userId) {
//        final Query query = new Query().setFilter(CompositeFilterOperator.and(
//                new PropertyFilter(Verifycode.TYPE, FilterOperator.EQUAL, type),
//                new PropertyFilter(Verifycode.BIZ_TYPE, FilterOperator.EQUAL, bizType),
//                new PropertyFilter(Verifycode.USER_ID, FilterOperator.EQUAL, userId))
//        ).addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final List<Verifycode> results = verifycodeMapper.getByTypeBizTypeUserId(type,bizType,userId);
            if (0 == results.size()) {
                return null;
            }

            return results.get(0);
        } catch (final Exception e) {
            LOGGER.error( "Gets verifycode failed", e);

            return null;
        }
    }

    /**
     * Gets a verifycode with the specified code.
     *
     * @param code the specified code
     * @return verifycode, returns {@code null} if not found
     */
    public Verifycode getVerifycode(final String code) {
//        final Query query = new Query().setFilter(new PropertyFilter(Verifycode.CODE, FilterOperator.EQUAL, code));

        try {
            final List<Verifycode> result = verifycodeMapper.getByCode(code);
            if (0 == result.size()) {
                return null;
            }

            return result.get(0);
        } catch (final Exception e) {
            LOGGER.error( "Gets verifycode error", e);

            return null;
        }
    }
}

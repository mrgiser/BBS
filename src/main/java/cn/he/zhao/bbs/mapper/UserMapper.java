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
package cn.he.zhao.bbs.mapper;

import cn.he.zhao.bbs.entity.UserExt;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper {

    UserExt get(final String oId) ;

    @Select("select * from user WHERE userStatus = #{userStatus}")
    List<UserExt> getUserByValid(final int userStatus);

    List<UserExt> getAll();

    @Select("select * from user WHERE userStatus = #{userStatus}")
    List<UserExt> getByUserStatus(final int userStatus);

    List<UserExt> getByUserJoinPointRank(final int userJoinPointRank);

    List<UserExt> getByUserJoinUsedPointRank(final int userJoinUsedPointRank);

    @Select("select * from user WHERE oId = #{q} OR userName = #{q} OR userEmail = #{q}")
    List<UserExt> getByNameOrEmailOrOId(final String q);

//    @Select("select * from user WHERE oId in #{oIds}")
    @Select("<script>"
            + "SELECT * FROM user WHERE oId in "
            + "<foreach item='item' index='index' collection='oIds' open='(' separator=',' close=')'>"
            + "#{item}"
            + "</foreach>"
            + "</script>")
    List<UserExt> findByOIds(List<String> oIds);

    void update(final String id, final UserExt user) ;

    UserExt getByName(final String name) ;

    UserExt getByEmail(final String email) ;

    @Select("select * from user WHERE userRole = (#{userRole})")
    List<UserExt> getAdmins(final String userRole) ;

    @Select("select * from user WHERE userStatus = #{userStatus} AND userLatestLoginTime >= #{time}")
    List<UserExt> getLatestLoggedByTime(final int userStatus,final long time);

    @Select("select * from user WHERE userCity = #{userCity} AND userStatus = #{userStatus} AND userLatestLoginTime >= #{latestTime}")
    List<UserExt> getByLoginTimeAndCity(final String userCity,final long latestTime,final int userStatus);

    @Select("select count(*) from user WHERE oId >= #{start} AND oId < #{end} AND userStatus = #{userStatus}")
    Integer getUserCountByTime(final long start,final long end,final int userStatus);

    boolean isAdminEmail(final String email) ;

    @Select("select count(*) from user where userRole = #{roleId}")
    Integer countByRoleId(final String roleId);

    @Select("SELECT\n"
            + "	u.*, Sum(sum) AS point\n"
            + "FROM\n"
            + "	" + "pointtransfer" + " AS p,\n"
            + "	" + "user" + " AS u\n"
            + "WHERE\n"
            + "	p.toId = u.oId\n"
            + "AND type = 27\n"
            + "GROUP BY\n"
            + "	toId\n"
            + "ORDER BY\n"
            + "	point DESC\n"
            + "LIMIT #{fetchSize}")
    List<UserExt> getsTopEatingsnakeUsersSum(final int fetchSize);

    @Select("SELECT\n"
            + "	u.*, MAX(sum) AS point\n"
            + "FROM\n"
            + "	" + "pointtransfer" + " AS p,\n"
            + "	" + "user" + " AS u\n"
            + "WHERE\n"
            + "	p.toId = u.oId\n"
            + "AND type = 27\n"
            + "GROUP BY\n"
            + "	toId\n"
            + "ORDER BY\n"
            + "	point DESC\n"
            + "LIMIT #{fetchSize}")
    List<UserExt> getsTopEatingsnakeUsersMax(final int fetchSize);

    @Select("select * from user " +
            "where " +
            "userSubMailSendTime =< #{sevenDaysAgo} " +
            "AND " +
            "userLatestLoginTime =< #{sevenDaysAgo} " +
            "AND " +
            "userSubMailStatus = #{userSubMailStatus} " +
            "AND " +
            "userStatus = #{userStatus} " +
            "AND " +
            "userEmail NOT LIKE #{userEmail}")
    List<UserExt> getMailUser(final long sevenDaysAgo,final int userSubMailStatus,final int userStatus,final String userEmail);
}

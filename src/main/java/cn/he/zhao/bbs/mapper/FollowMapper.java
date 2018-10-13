package cn.he.zhao.bbs.mapper;

import cn.he.zhao.bbs.entity.Follow;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public interface FollowMapper {

    String add(Follow follow);

    void removeByFollowerIdAndFollowingId(final String followerId, final String followingId,
                                                 final int followingType);


    JSONObject getByFollowerIdAndFollowingId(final String followerId, final String followingId,
                                                    final int followingType);

    boolean exists(final String followerId, final String followingId, final int followingType);

    List<Follow> getByFollowerIdAndFollowingType(final String followerId,  final int followingType);
    List<Follow> getFollowingIdAndFollowingType(final String followingId,  final int followingType);

    Long countByFollowingIdAndFollowingType(final String followingId,  final int followingType);

    Long countByFollowerIdAndFollowingType(final String followerId,  final int followingType);
}

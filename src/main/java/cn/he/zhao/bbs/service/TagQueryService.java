package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.cache.TagCache;
import cn.he.zhao.bbs.entityUtil.TagUtil;
import cn.he.zhao.bbs.entityUtil.UserExtUtil;
import cn.he.zhao.bbs.entityUtil.my.CollectionUtils;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.Pagination;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.spring.Common;
import cn.he.zhao.bbs.spring.Paginator;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Tag query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.8.7.0, Feb 22, 2018
 * @since 0.2.0
 */
@Service
public class TagQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TagQueryService.class);

    private static RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(3000)
            .setConnectionRequestTimeout(1000).setSocketTimeout(10000).build();

    /**
     * Tag Mapper.
     */
    @Autowired
    private TagMapper tagMapper;

    /**
     * User-Tag Mapper.
     */
    @Autowired
    private UserTagMapper userTagMapper;

    /**
     * Tag-Tag Mapper.
     */
    @Autowired
    private TagTagMapper tagTagMapper;

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * Domain Mapper.
     */
    @Autowired
    private DomainMapper domainMapper;

    /**
     * Domain tag Mapper.
     */
    @Autowired
    private DomainTagMapper domainTagMapper;

    /**
     * Avatar query service.
     */
    @Autowired
    private AvatarQueryService avatarQueryService;

    /**
     * Short link query service.
     */
    @Autowired
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Tag cache.
     */
    @Autowired
    private TagCache tagCache;

    /**
     * Gets tags by the specified title prefix.
     *
     * @param titlePrefix the specified title prefix
     * @param fetchSize   the specified fetch size
     * @return a list of tags, for example      <pre>
     * [
     *     {
     *         "tagTitle": "",
     *         "tagIconPath": "",
     *     }, ....
     * ]
     * </pre>
     */
    public List<JSONObject> getTagsByPrefix(final String titlePrefix, final int fetchSize) {
        final JSONObject titleToSearch = new JSONObject();
        titleToSearch.put(TagUtil.TAG_T_TITLE_LOWER_CASE, titlePrefix.toLowerCase());

        final List<JSONObject> tags = tagCache.getTags();

        int index = Collections.binarySearch(tags, titleToSearch, (t1, t2) -> {
            String u1Title = t1.optString(TagUtil.TAG_T_TITLE_LOWER_CASE);
            final String inputTitle = t2.optString(TagUtil.TAG_T_TITLE_LOWER_CASE);

            if (u1Title.length() < inputTitle.length()) {
                return u1Title.compareTo(inputTitle);
            }

            u1Title = u1Title.substring(0, inputTitle.length());

            return u1Title.compareTo(inputTitle);
        });

        final List<JSONObject> ret = new ArrayList<>();

        if (index < 0) {
            return ret;
        }

        int start = index;
        int end = index;

        while (start > -1
                && tags.get(start).optString(TagUtil.TAG_T_TITLE_LOWER_CASE).startsWith(titlePrefix.toLowerCase())) {
            start--;
        }

        start++;

        while (end < tags.size()
                && tags.get(end).optString(TagUtil.TAG_T_TITLE_LOWER_CASE).startsWith(titlePrefix.toLowerCase())) {
            end++;
        }

        final List<JSONObject> subList = tags.subList(start, end);
        Collections.sort(subList, (t1, t2) -> t2.optInt(TagUtil.TAG_REFERENCE_CNT) - t1.optInt(TagUtil.TAG_REFERENCE_CNT));

        return subList.subList(0, subList.size() > fetchSize ? fetchSize : subList.size());
    }

    /**
     * Generates tags for the specified content.
     *
     * @param content      the specified content
     * @param tagFetchSize the specified tag fetch size
     * @return tags
     */
    public List<String> generateTags(final String content, final int tagFetchSize) {
        final List<String> ret = new ArrayList<>();

        final String token = Symphonys.get("boson.token");
        if (StringUtils.isBlank(token)) {
            return ret;
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost request = new HttpPost("http://api.bosonnlp.com/keywords/analysis?top_k=" + tagFetchSize);
//            request.setURL(new URL("http://api.bosonnlp.com/keywords/analysis?top_k=" + tagFetchSize));
//            request.setRequestMethod(HTTPRequestMethod.POST);

            request.setHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");
            request.addHeader("X-Token", token);
//            request.setPayload(("\"" + content + "\"").getBytes("UTF-8"));

            request.setConfig(defaultRequestConfig);

            StringEntity strEntity = new StringEntity("\"" + content + "\"","UTF-8");
            request.setEntity(strEntity);

            final CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            StatusLine status = response.getStatusLine();
            int statusCode = status.getStatusCode();
            String str = EntityUtils.toString(entity);
//            final String str = new String(response.getContent(), "UTF-8");

            try {
                final JSONArray data = new JSONArray(str);

                for (int i = 0; i < data.length(); i++) {
                    final JSONArray key = data.getJSONArray(i);
                    ret.add(key.optString(1));
                }
            } catch (final JSONException e) {
                final JSONObject data = new JSONObject(str);

                LOGGER.error( "Boson process failed [" + data.toString(4) + "]");
            }
        } catch (final IOException | JSONException e) {
            LOGGER.error( "Generates tags error: " + content, e);
        }

        return ret;
    }

    /**
     * Determines whether the specified tag title is reserved.
     *
     * @param tagTitle the specified tag title
     * @return {@code true} if it is reserved, otherwise returns {@code false}
     */
    public boolean isReservedTag(final String tagTitle) {
        return ArrayUtils.contains(Symphonys.RESERVED_TAGS, tagTitle);
    }

    /**
     * Gets invalid tags.
     *
     * @return invalid tags, returns an empty list if not found
     */
    public List<String> getInvalidTags() {
        final List<String> ret = new ArrayList<>();

//        final Query query = new Query().setFilter(
//                new PropertyFilter(TagUtil.TAG_STATUS, FilterOperator.NOT_EQUAL, TagUtil.TAG_STATUS_C_VALID));

        try {
            final List<Tag> records = tagMapper.getInvalidTags();

            for (int i = 0; i < records.size(); i++) {
                final String title = records.get(i).getTagTitle();

                ret.add(title);
            }
        } catch (final Exception e) {
            LOGGER.error( "Gets invalid tags error", e);
        }

        return ret;
    }

    /**
     * Gets a tag by the specified tag URI.
     *
     * @param tagURI the specified tag URI
     * @return tag, returns {@code null} if not null
     * @throws Exception service exception
     */
    public Tag getTagByURI(final String tagURI) throws Exception {
        try {
            final Tag ret = tagMapper.getByURI(tagURI);
            if (null == ret) {
                return null;
            }

            if (TagUtil.TAG_STATUS_C_VALID != ret.getTagStatus()) {
                return null;
            }

            TagUtil.fillDescription(ret);

            if (StringUtils.isBlank(ret.getTagSeoTitle())) {
                ret.setTagSeoTitle( tagURI);
            }

            if (StringUtils.isBlank(ret.getTagSeoDesc())) {
                ret.setTagSeoDesc( ret.getTagDescriptionText());
            }

            if (StringUtils.isBlank(ret.getTagSeoKeywords())) {
                ret.setTagSeoKeywords(tagURI);
            }

            final List<Domain> domains = new ArrayList<>();
            ret.setTagDomains((Object) domains);

//            final Query query = new Query().setFilter(
//                    new PropertyFilter(Tag.TAG + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, ret.getOid()));

            final List<DomainTag> relations = domainTagMapper.getByTagOId(ret.getOid());
            for (int i = 0; i < relations.size(); i++) {
                final DomainTag relation = relations.get(i);
                final String domainId = relation.getDomain_oId();
                final Domain domain = domainMapper.getByOId(domainId);
                domains.add(domain);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets tag [title=" + tagURI + "] failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets a tag by the specified tag title.
     *
     * @param tagTitle the specified tag title
     * @return tag, returns {@code null} if not null
     * @throws Exception service exception
     */
    public Tag getTagByTitle(final String tagTitle) throws Exception {
        try {
            final Tag ret = tagMapper.getByTitle(tagTitle);
            if (null == ret) {
                return null;
            }

            if (TagUtil.TAG_STATUS_C_VALID != ret.getTagStatus()) {
                return null;
            }

            TagUtil.fillDescription(ret);

            if (StringUtils.isBlank(ret.getTagSeoTitle())) {
                ret.setTagSeoTitle( tagTitle);
            }

            if (StringUtils.isBlank(ret.getTagSeoDesc())) {
                ret.setTagSeoDesc( ret.getTagDescriptionText());
            }

            if (StringUtils.isBlank(ret.getTagSeoKeywords())) {
                ret.setTagSeoKeywords( tagTitle);
            }

            final List<Domain> domains = new ArrayList<>();
            ret.setTagDomains((Object) domains);

//            final Query query = new Query().setFilter(
//                    new PropertyFilter(Tag.TAG + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, ret.getOid()));
            final List<DomainTag> relations = domainTagMapper.getByTagOId(ret.getOid());
            for (int i = 0; i < relations.size(); i++) {
                final DomainTag relation = relations.get(i);
                final String domainId = relation.getDomain_oId();
                final Domain domain = domainMapper.getByOId(domainId);
                domains.add(domain);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets tag [title=" + tagTitle + "] failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets the trend (sort by reference count descending) tags.
     *
     * @param fetchSize the specified fetch size
     * @return trend tags, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<Tag> getTrendTags(final int fetchSize) throws Exception {
//        final Query query = new Query().addSort(TagUtil.TAG_REFERENCE_CNT, SortDirection.DESCENDING).
//                setCurrentPageNum(1).setPageSize(fetchSize).setPageCount(1);

        PageHelper.startPage(1, fetchSize);
        try {
            final List<Tag> results = tagMapper.getAllDESCByTagReferenceCount();
//            final List<JSONObject> ret = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            for (final Tag tag : results) {
                TagUtil.fillDescription(tag);
            }

            return results;
        } catch (final Exception e) {
            LOGGER.error( "Gets trend tags failed");
            throw new Exception(e);
        }
    }

    /**
     * Gets the new (sort by oId descending) tags.
     *
     * @return trend tags, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getNewTags() throws Exception {
        return tagCache.getNewTags();
    }

    /**
     * Gets the cold (sort by reference count ascending) tags.
     *
     * @param fetchSize the specified fetch size
     * @return trend tags, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<Tag> getColdTags(final int fetchSize) throws Exception {
//        final Query query = new Query().addSort(Tag.TAG_REFERENCE_CNT, SortDirection.ASCENDING).
//                setCurrentPageNum(1).setPageSize(fetchSize).setPageCount(1);

        PageHelper.startPage(1, fetchSize);
        try {
            final List<Tag> result = tagMapper.getAllASCByTagReferenceCount();
//            final List<JSONObject> ret = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            for (final Tag tag : result) {
                TagUtil.fillDescription(tag);
            }

            return result;
        } catch (final Exception e) {
            LOGGER.error( "Gets cold tags failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets the tags the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return tags, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getTags(final int fetchSize) throws Exception {
        return tagCache.getIconTags(fetchSize);
    }

    /**
     * Gets the creator of the specified tag of the given tag id.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param tagId          the given tag id
     * @return tag creator, for example,      <pre>
     * {
     *     "tagCreatorThumbnailURL": "",
     *     "tagCreatorThumbnailUpdateTime": 0,
     *     "tagCreatorName": ""
     * }
     * </pre>, returns {@code null} if not found
     * @throws Exception service exception
     */
    public Tag getCreator(final int avatarViewMode, final String tagId) throws Exception {
//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tagId));

//        final List<Filter> orFilters = new ArrayList<>();
//        orFilters.add(new PropertyFilter(Common.TYPE, FilterOperator.EQUAL, TagUtil.TAG_TYPE_C_CREATOR));
//        orFilters.add(new PropertyFilter(Common.TYPE, FilterOperator.EQUAL, TagUtil.TAG_TYPE_C_USER_SELF));

//        filters.add(new CompositeFilter(CompositeFilterOperator.OR, orFilters));

//        final Query query = new Query().setCurrentPageNum(1).setPageSize(1).setPageCount(1).
//                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
//                addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);

        PageHelper.startPage(1, 1);
        try {
            final Tag ret = new Tag();

            final List<UserTag> results = userTagMapper.getByTagIdAndCretorType(tagId);
//            final JSONArray results = result.optJSONArray(Keys.RESULTS);
            final UserTag creatorTagRelation = results.get(0);
            if (null == creatorTagRelation) {
                LOGGER.warn( "Can't find tag [id=" + tagId + "]'s creator, uses anonymous user instead");
                ret.setTagCreatorThumbnailURL(avatarQueryService.getDefaultAvatarURL("48"));
                ret.setTagCreatorThumbnailUpdateTime(0L);
                ret.setTagCreatorName(UserExtUtil.ANONYMOUS_USER_NAME);

                return ret;
            }

            final String creatorId = creatorTagRelation.getUser_oId();
            if (UserExtUtil.ANONYMOUS_USER_ID.equals(creatorId)) {
                ret.setTagCreatorThumbnailURL( avatarQueryService.getDefaultAvatarURL("48"));
                ret.setTagCreatorThumbnailUpdateTime( 0L);
                ret.setTagCreatorName(UserExtUtil.ANONYMOUS_USER_NAME);

                return ret;
            }

            // TODO: 2018/10/13 增加 entity到 json的工具类 
            final UserExt creator = userMapper.get(creatorId);

            final String thumbnailURL = avatarQueryService.getAvatarURLByUser(avatarViewMode, creator, "48");

            ret.setTagCreatorThumbnailURL( thumbnailURL);
            ret.setTagCreatorThumbnailUpdateTime(creator.getUserUpdateTime());
            ret.setTagCreatorName(creator.getUserName());

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets tag creator failed [tagId=" + tagId + "]", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets the participants (article ref) of the specified tag of the given tag id.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param tagId          the given tag id
     * @param fetchSize      the specified fetch size
     * @return tag participants, for example,      <pre>
     * [
     *     {
     *         "tagParticipantName": "",
     *         "tagParticipantThumbnailURL": "",
     *         "tagParticipantThumbnailUpdateTime": long
     *     }, ....
     * ]
     * </pre>, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<Tag> getParticipants(final int avatarViewMode,
                                            final String tagId, final int fetchSize) throws Exception {
//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tagId));
//        filters.add(new PropertyFilter(Common.TYPE, FilterOperator.EQUAL, 1));
//
//        Query query = new Query().setCurrentPageNum(1).setPageSize(fetchSize).setPageCount(1).
//                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        final List<Tag> ret = new ArrayList<>();

        PageHelper.startPage(1, fetchSize);
        try {
            List<UserTag> userTagRelations = userTagMapper.getByTagIdAndArticleType(tagId);
//            final JSONArray userTagRelations = result.optJSONArray(Keys.RESULTS);

            final List<String> userIds = new ArrayList<>();
            for (int i = 0; i < userTagRelations.size(); i++) {
                userIds.add(userTagRelations.get(i).getUser_oId());
            }

//            query = new Query().setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, userIds));
            List<UserExt> users = userMapper.findByOIds(userIds);

//            final List<JSONObject> users = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            for (final UserExt user : users) {
                final Tag participant = new Tag();

                participant.setTagCreatorName( user.getUserName());

                final String thumbnailURL = avatarQueryService.getAvatarURLByUser(avatarViewMode, user, "48");
                participant.setTagCreatorThumbnailURL( thumbnailURL);
                participant.setTagCreatorThumbnailUpdateTime( user.getUserUpdateTime());

                ret.add(participant);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets tag participants failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets the related tags of the specified tag of the given tag id.
     *
     * @param tagId     the given tag id
     * @param fetchSize the specified fetch size
     * @return related tags, for example,      <pre>
     * [{
     *     "oId": "",
     *     "tagTitle": "",
     *     "tagDescription": "",
     *     ....
     * }, ....]
     * </pre>, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<Tag> getRelatedTags(final String tagId, final int fetchSize) throws Exception {
        final List<Tag> ret = new ArrayList<>();

        final Set<String> tagIds = new HashSet<>();

        PageHelper.startPage(1,fetchSize);
        try {
            List<TagTag> relations = tagTagMapper.getByTag1Id(tagId);

            boolean full = false;

            for (int i = 0; i < relations.size(); i++) {
                tagIds.add(relations.get(i).getTag2_oId());

                if (tagIds.size() >= fetchSize) {
                    full = true;

                    break;
                }
            }

            PageHelper.startPage(1,fetchSize);
            if (!full) {
                relations = tagTagMapper.getByTag2Id(tagId);

                for (int i = 0; i < relations.size(); i++) {
                    tagIds.add(relations.get(i).getTag1_oId());

                    if (tagIds.size() >= fetchSize) {
                        break;
                    }
                }
            }

            for (final String tId : tagIds) {
                final Tag tag = tagMapper.get(tId);
                if (null != tag) {
                    TagUtil.fillDescription(tag);
                    ret.add(tag);
                }
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets related tags failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets tags by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "tagTitle": "", // optional
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10
     *                          , see {@link Pagination} for more details
     * @param tagFields         the specified tag fields to return
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "tags": [{
     *         "oId": "",
     *         "tagTitle": "",
     *         "tagDescription": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws Exception service exception
     * @see Pagination
     */
    public JSONObject getTags(final JSONObject requestJSONObject, final Map<String, Class<?>> tagFields) throws Exception {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);

        PageHelper.startPage(currentPageNum, pageSize, "oid desc");

//        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
//                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
//        for (final Map.Entry<String, Class<?>> tagField : tagFields.entrySet()) {
//            query.addProjection(tagField.getKey(), tagField.getValue());
//        }

        // TODO: 2018/9/28  requestJSONObject.has(Tag.TAG_TITLE)
//        if (requestJSONObject.has(Tag.TAG_TITLE)) {
//            query.setFilter(new PropertyFilter(Tag.TAG_TITLE, FilterOperator.EQUAL, requestJSONObject.optString(Tag.TAG_TITLE)));
//        }

        PageInfo<Tag> result = null;

        try {
            result = new PageInfo<>(tagMapper.getALL());
        } catch (final Exception e) {
            LOGGER.error( "Gets tags failed", e);

            throw new Exception(e);
        }

        final int pageCount = result.getPages();

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final List<Tag> data = result.getList();

        for (final Tag tag : data) {
            tag.setTagCreateTime(Long.parseLong(tag.getOid()));
        }

        ret.put(TagUtil.TAGS, data);

        return ret;
    }

    /**
     * Gets a tag by the specified id.
     *
     * @param tagId the specified id
     * @return tag, return {@code null} if not found
     * @throws Exception service exception
     */
    public Tag getTag(final String tagId) throws Exception {
        try {
            return tagMapper.get(tagId);
        } catch (final Exception e) {
            LOGGER.error( "Gets a tag [tagId=" + tagId + "] failed", e);
            throw new Exception(e);
        }
    }
}

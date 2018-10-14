package cn.he.zhao.bbs.cache;

import cn.he.zhao.bbs.entity.Tag;
import cn.he.zhao.bbs.entityUtil.TagUtil;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.mapper.TagMapper;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.util.JSONs;
import cn.he.zhao.bbs.util.Symphonys;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.management.Query;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TagCache {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TagCache.class);

    /**
     * Icon tags.
     */
    private static final List<Tag> ICON_TAGS = new ArrayList<>();

    /**
     * New tags.
     */
    private static final List<Tag> NEW_TAGS = new ArrayList<>();

    /**
     * All tags.
     */
    private static final List<Tag> TAGS = new ArrayList<>();

    /**
     * &lt;title, URI&gt;
     */
    private static final Map<String, String> TITLE_URIS = new ConcurrentHashMap<>();

    /**
     * &lt;id, tag&gt;
     */
    private static final Map<String, Tag> CACHE = new ConcurrentHashMap<>();

    /**
     * Gets a tag by the specified tag id.
     *
     * @param id the specified tag id
     * @return tag, returns {@code null} if not found
     */
    public Tag getTag(final String id) {
        final Tag tag = CACHE.get(id);
        if (null == tag) {
            return null;
        }

        final Tag ret = JSONs.clone(tag);

        TITLE_URIS.put(ret.getTagTitle(), ret.getTagURI());

        return ret;
    }

    /**
     * Adds or updates the specified tag.
     *
     * @param tag the specified tag
     */
    public void putTag(final Tag tag) {
        CACHE.put(tag.getOid(), JSONs.clone(tag));

        TITLE_URIS.put(tag.getTagTitle(), tag.getTagURI());
    }

    /**
     * Removes a tag by the specified tag id.
     *
     * @param id the specified tag id
     */
    public void removeTag(final String id) {
        final Tag tag = CACHE.get(id);
        if (null == tag) {
            return;
        }

        CACHE.remove(id);

        TITLE_URIS.remove(tag.getTagTitle());
    }

    /**
     * Gets a tag URI with the specified tag title.
     *
     * @param title the specified tag title
     * @return tag URI, returns {@code null} if not found
     */
    public String getURIByTitle(final String title) {
        return TITLE_URIS.get(title);
    }

    /**
     * Gets new tags with the specified fetch size.
     *
     * @return new tags
     */
    public List<Tag> getNewTags() {
        if (NEW_TAGS.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(NEW_TAGS);
    }

    /**
     * Gets icon tags with the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return icon tags
     */
    public List<Tag> getIconTags(final int fetchSize) {
        if (ICON_TAGS.isEmpty()) {
            return Collections.emptyList();
        }

        final int end = fetchSize >= ICON_TAGS.size() ? ICON_TAGS.size() : fetchSize;

        return new ArrayList<>(ICON_TAGS.subList(0, end));
    }

    /**
     * Gets all tags.
     *
     * @return all tags
     */
    public List<Tag> getTags() {
        if (TAGS.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(TAGS);
    }

    /**
     * Loads all tags.
     */
    public void loadTags() {
        loadAllTags();
        loadIconTags();
        loadNewTags();
    }

    /**
     * Loads new tags.
     */
    private void loadNewTags() {
//        final LatkeBeanManager beanManager = LatkeBeanManagerImpl.getInstance();
        final TagMapper tagMapper = SpringUtil.getBean(TagMapper.class);

//        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
//                setCurrentPageNum(1).setPageSize(Symphonys.getInt("newTagsCnt")).setPageCount(1);

        PageHelper.startPage(1,Symphonys.getInt("newTagsCnt"),"oId DESC");
//        query.setFilter(new PropertyFilter(TagUtil.TAG_REFERENCE_CNT, FilterOperator.GREATER_THAN, 0));

        try {
            final List<Tag> result = tagMapper.getNewTags();
            NEW_TAGS.clear();
            NEW_TAGS.addAll(result);
        } catch (final Exception e) {
            LOGGER.error( "Gets new tags failed", e);
        }
    }

    /**
     * Loads icon tags.
     */
    @Transactional
    private void loadIconTags() {
//        final LatkeBeanManager beanManager = LatkeBeanManagerImpl.getInstance();
        final TagMapper tagMapper = SpringUtil.getBean(TagMapper.class);

//        final Query query = new Query().setFilter(
//                CompositeFilterOperator.and(
//                        new PropertyFilter(TagUtil.TAG_ICON_PATH, FilterOperator.NOT_EQUAL, ""),
//                        new PropertyFilter(TagUtil.TAG_STATUS, FilterOperator.EQUAL, TagUtil.TAG_STATUS_C_VALID)))
//                .setCurrentPageNum(1).setPageSize(Integer.MAX_VALUE).setPageCount(1)
//                .addSort(TagUtil.TAG_RANDOM_DOUBLE, SortDirection.ASCENDING);
        PageHelper.startPage(1,Integer.MAX_VALUE,"tagRandomDouble ASC");

        try {
            final List<Tag> tags = tagMapper.getByIconAndStatus();
//            final List<JSONObject> tags = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            final List<Tag> toUpdateTags = new ArrayList<>();
            for (final Tag tag : tags) {
                toUpdateTags.add(JSONs.clone(tag));
            }

            for (final Tag tag : tags) {
                TagUtil.fillDescription(tag);
                tag.setTagTitleLowerCase(tag.getTagTitle().toLowerCase());
            }

            ICON_TAGS.clear();
            ICON_TAGS.addAll(tags);

            // Updates random double
//            final Transaction transaction = tagMapper.beginTransaction();
            for (final Tag tag : toUpdateTags) {
                tag.setTagRandomDouble( Math.random());

                tagMapper.update(tag.getOid(), tag);
            }
//            transaction.commit();
        } catch (final Exception e) {
            LOGGER.error( "Load icon tags failed", e);
        }
    }

    /**
     * Loads all tags.
     */
    public void loadAllTags() {
//        final LatkeBeanManager beanManager = LatkeBeanManagerImpl.getInstance();
        final TagMapper tagMapper = SpringUtil.getBean(TagMapper.class);

//        final Query query = new Query().setFilter(
//                new PropertyFilter(Tag.TAG_STATUS, FilterOperator.EQUAL, TagUtil.TAG_STATUS_C_VALID))
//                .setCurrentPageNum(1).setPageSize(Integer.MAX_VALUE).setPageCount(1);
        PageHelper.startPage(1,Integer.MAX_VALUE);
        try {
            final List<Tag> tags = tagMapper.getByStatus(TagUtil.TAG_STATUS_C_VALID);
//            final List<JSONObject> tags = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            final Iterator<Tag> iterator = tags.iterator();
            while (iterator.hasNext()) {
                final Tag tag = iterator.next();

                String title = tag.getTagTitle();
                if ("".equals(title)
                        || StringUtils.contains(title, " ")
                        || StringUtils.contains(title, "　")) { // filter legacy data
                    iterator.remove();

                    continue;
                }

                if (!TagUtil.containsWhiteListTags(title)) {
                    if (!TagUtil.TAG_TITLE_PATTERN.matcher(title).matches() || title.length() > TagUtil.MAX_TAG_TITLE_LENGTH) {
                        iterator.remove();

                        continue;
                    }
                }

                TagUtil.fillDescription(tag);
                tag.setTagTitleLowerCase(tag.getTagTitle().toLowerCase());
            }

            tags.sort((t1, t2) -> {
                final String u1Title = t1.getTagTitleLowerCase();
                final String u2Title = t2.getTagTitleLowerCase();

                return u1Title.compareTo(u2Title);
            });

            TAGS.clear();
            TAGS.addAll(tags);

            TITLE_URIS.clear();
            for (final Tag tag : tags) {
                TITLE_URIS.put(tag.getTagTitle(), tag.getTagURI());
            }
        } catch (final Exception e) {
            LOGGER.error( "Load all tags failed", e);
        }
    }
}

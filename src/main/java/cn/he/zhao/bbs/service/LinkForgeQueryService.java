package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.cache.TagCache;
import cn.he.zhao.bbs.entityUtil.TagUtil;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class LinkForgeQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkForgeQueryService.class);

    /**
     * Max tag count.
     */
    private static final int TAG_MAX_COUNT = Symphonys.getInt("forge.link.maxTagCnt");

    /**
     * Max link count.
     */
    private static final int LINK_MAX_COUNT = Symphonys.getInt("forge.link.maxCnt");

    /**
     * Tag reference count threshold.
     */
    private static final int TAG_REF_COUNT = Symphonys.getInt("forge.link.tagRefCnt");

    /**
     * Tag-User-LinkUtil Mapper.
     */
    @Autowired
    private TagUserLinkMapper tagUserLinkMapper;

    /**
     * LinkUtil Mapper.
     */
    @Autowired
    private LinkMapper linkMapper;

    /**
     * Tag cache.
     */
    @Autowired
    private TagCache tagCache;

    /**
     * Gets user's nice links.
     *
     * @param userId the specified user id
     * @return a list of tags with links, each of tag like this,      <pre>
     * {
     *     "tagTitle": "",
     *     "tagIconPath": "",
     *     "tagLinks: [{
     *         "linkAddr": "",
     *         "linkTitle": "",
     *         ....
     *     }, ....]
     * }
     * </pre>
     */
    public List<Tag> getUserForgedLinks(final String userId) {
        final List<Tag> ret = new ArrayList<>();

        try {
            List<Tag> cachedTags = tagCache.getTags();
            Collections.sort(cachedTags, (o1, o2) -> o2.getTagLinkCount() - o1.getTagLinkCount());

            for (final Tag cachedTag : cachedTags) {
                cachedTags = cachedTags.size() > TAG_MAX_COUNT ? cachedTags.subList(0, TAG_MAX_COUNT) : cachedTags;

                if (cachedTag.getTagLinkCount() < 1
                        || cachedTag.getTagReferenceCount() < TAG_REF_COUNT) {
                    continue; // XXX: optimize, reduce queries
                }

                final String tagId = cachedTag.getOid();

                final Tag tag = new Tag();
                tag.setTagTitle(cachedTag.getTagTitle());
                tag.setTagURI(cachedTag.getTagURI());
                tag.setTagIconPath(cachedTag.getTagIconPath());

                // query link id
                final List<String> linkIds = tagUserLinkMapper.getByTagIdAndUserId(tagId, userId, LINK_MAX_COUNT);
                if (linkIds.isEmpty()) {
                    continue;
                }

                // get link by id
                final List<Link> links = new ArrayList<>();
                for (final String linkId : linkIds) {
                    links.add(linkMapper.getByOId(linkId));
                }

                tag.setTagLinks((Object) links);
                tag.setTagLinkCount( links.size());

                ret.add(tag);
            }

            Collections.sort(ret, (tag1, tag2) -> tag2.getTagLinkCount() - tag1.getTagLinkCount());
        } catch (final Exception e) {
            LOGGER.error( "Gets forged links failed", e);
        }

        return ret;
    }

    /**
     * Gets nice links.
     *
     * @return a list of tags with links, each of tag like this,      <pre>
     * {
     *     "tagTitle": "",
     *     "tagIconPath": "",
     *     "tagLinks: [{
     *         "linkAddr": "",
     *         "linkTitle": "",
     *         ....
     *     }, ....]
     * }
     * </pre>
     */
    public List<Tag> getForgedLinks() {
        final List<Tag> ret = new ArrayList<>();

        try {
            List<Tag> cachedTags = tagCache.getTags();

            Collections.sort(cachedTags, (o1, o2) -> o2.getTagLinkCount() - o1.getTagLinkCount());

            cachedTags = cachedTags.size() > TAG_MAX_COUNT ? cachedTags.subList(0, TAG_MAX_COUNT) : cachedTags;

            for (final Tag cachedTag : cachedTags) {
                if (cachedTag.getTagLinkCount() < 1
                        || cachedTag.getTagReferenceCount() < TAG_REF_COUNT) {
                    continue; // XXX: optimize, reduce queries
                }

                final String tagId = cachedTag.getOid();

                final Tag tag = new Tag();
                tag.setTagTitle(cachedTag.getTagTitle());
                tag.setTagURI( cachedTag.getTagURI());
                tag.setTagIconPath( cachedTag.getTagIconPath());

                // query link id
                final List<String> linkIds = tagUserLinkMapper.getLinkIdsByTagId(tagId, LINK_MAX_COUNT);
                if (linkIds.isEmpty()) {
                    continue;
                }

                // get link by id
                final List<Link> links = new ArrayList<>();
                for (final String linkId : linkIds) {
                    links.add(linkMapper.getByOId(linkId));
                }

                tag.setTagLinks((Object) links);
                tag.setTagLinkCount(links.size());

                ret.add(tag);
            }

            Collections.sort(ret, (tag1, tag2) -> tag2.getTagLinkCount() - tag1.getTagLinkCount());
        } catch (final Exception e) {
            LOGGER.error( "Gets forged links failed", e);
        }

        return ret;
    }
}

package cn.he.zhao.bbs.mapper;

import cn.he.zhao.bbs.entity.Article;
import org.apache.ibatis.annotations.Select;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

public interface ArticleMapper {

//    @Autowired
//    ArticleCache articleCache;
    Article get(final String oId);

    @Select("select * from article WHERE oId >= #{articleId}")
    List<Article> getGreaterThanID(final String articleId);

    @Select("select * from article WHERE oId <= #{articleId}")
    List<Article> getLessThanID(final String articleId);

    @Select("select * from article WHERE articleStick >= #{articleStick}")
    List<Article> getByArticleStick(final Long articleStick);

    Article getByPrimaryKey(final String oid);

    @Select("select count(*) from article WHERE oId >= #{start} AND oId < #{end} And articleStatus != #{status}")
    Integer countByTimeAndStatus(final long start,final long end,final int status);

    void remove(final String id);

    @Select("select * from article WHERE articleStatus != 1 ORDER BY oId DESC")
    List<Article> findByArticleStatus();

    void update(Article article);

    List<Article> getRandomly(final int fetchSize);

    Article getByTitle(final String articleTitle);

    String add(Article article);

    Article getByOid(final String oid);

    List<Article> getByArticleAuthorId(final String articleAuthorId);

    @Select("select * from article " +
            "where " +
            "articleCreateTime => #{sevenDaysAgo} " +
            "AND " +
            "articleType = 0 " +
            "AND " +
            "articleStatus = 1 " +
            "AND " +
            "articleTags NOT LIKE 'Sandbox%' " +
            "ORDER BY articlePushOrder DESC, articleCommentCount DESC,redditScore DESC"
            )
    List<Article> selectNiceArticles(final long sevenDaysAgo);

    @Select("<script>"
            + "SELECT * FROM article WHERE articleStatus != #{status} AND articleType != #{type} " +
            "AND" +
            " articleAuthorId in "
            + "<foreach item='item' index='index' collection='followingUserIds' open='(' separator=',' close=')'>"
            + "#{item}"
            + "</foreach>"
            + "</script>")
    List<Article> getByStatusTypeID(int status, int type, List<String> followingUserIds);

    @Select("<script>"
            + "SELECT * FROM tagarticle WHERE articleStatus != #{articleStatus} AND oId in "
            + "<foreach item='item' index='index' collection='articleIds' open='(' separator=',' close=')'>"
            + "#{item}"
            + "</foreach>"
            + "</script>")
    List<Article> getByArticleIds(final Set<String> articleIds,final int articleStatus);

    @Select("<script>"
            + "SELECT * FROM tagarticle WHERE oId in "
            + "<foreach item='item' index='index' collection='articleIds' open='(' separator=',' close=')'>"
            + "#{item}"
            + "</foreach>"
            + "</script>")
    List<Article> getByArticleIds2(final Set<String> articleIds);

    @Select("<script>"
            + "SELECT * FROM tagarticle WHERE oId in "
            + "<foreach item='item' index='index' collection='articleIds' open='(' separator=',' close=')'>"
            + "#{item}"
            + "</foreach>"
            + "</script>")
    List<Article> getByArticleIds2(final List<String> articleIds);

    @Select("select * from article WHERE articleCity = #{city} ")
    List<Article> getByCity(final String city);

    @Select("select * from article WHERE clientArticleId = #{clientArticleId} AND articleAuthorId = #{articleAuthorId} ")
    List<Article> getByClientArticleId(final String clientArticleId,final String articleAuthorId);

    @Select("select * from article WHERE articleAuthorId = #{id} " +
            "AND articleAnonymous = #{anonymous} " +
            "AND articleStatus = #{status}")
    List<Article> getByArticleAuthorIdArticleAnonymousStatus(String id,int anonymous, int status);
}

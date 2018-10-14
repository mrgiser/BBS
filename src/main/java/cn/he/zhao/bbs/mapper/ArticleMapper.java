package cn.he.zhao.bbs.mapper;

import cn.he.zhao.bbs.entity.Article;
import org.apache.ibatis.annotations.Select;
import org.json.JSONObject;

import java.util.List;

public interface ArticleMapper {

//    @Autowired
//    ArticleCache articleCache;
    Article get(final String oId);

    @Select("select * from article WHERE articleStick >= #{articleStick}")
    List<Article> getByArticleStick(final Long articleStick);

    Article getByPrimaryKey(final String oid);

    void remove(final String id);

    @Select("select * from article WHERE articleStatus != 1 ORDER BY oId DESC")
    List<Article> findByArticleStatus();

    void update(Article article);

    List<Article> getRandomly(final int fetchSize);

    Article getByTitle(final String articleTitle);

    String add(Article article);

    Article getByOid(final String oid);

    List<Article> getByArticleAuthorId(final String articleAuthorId);
}

package cn.he.zhao.bbs.mapper;

import cn.he.zhao.bbs.entity.Article;
import org.json.JSONObject;

import java.util.List;

public interface ArticleMapper {

//    @Autowired
//    ArticleCache articleCache;

    void remove(final String id);

    Article get(final String id);


    void update(Article article);


    List<Article> getRandomly(final int fetchSize);

    Article getByTitle(final String articleTitle);

    String add(Article article);
}

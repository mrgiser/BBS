package cn.he.zhao.bbs.mapper;

import cn.he.zhao.bbs.entity.Article;
import cn.he.zhao.bbs.util.Ids;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * 描述:
 * ArticleMapperTest
 *
 * @Author HeFeng
 * @Create 2018-09-02 11:34
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ArticleMapperTest {

    public static final Logger logger = LoggerFactory.getLogger(ArticleMapperTest.class);


    @Autowired
    private ArticleMapper articleMapper;
    private static String Title = "我的测试博客";

    @Test
    public void add() throws Exception {
        Article article = new Article();
        final String oid = Ids.genTimeMillisId();
        article.setOid(oid);
        article.setArticleTitle("Title");
        article.setArticleTags("Sym");


    }

    @Test
    public void get() throws Exception {


    }

    @Test
    public void update() throws Exception {


    }

    @Test
    public void getRandomly() throws Exception {


    }

    @Test
    public void getByTitle() throws Exception {


    }

    @Test
    public void remove() throws Exception {


    }
}
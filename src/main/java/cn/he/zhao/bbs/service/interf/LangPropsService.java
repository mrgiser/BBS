package cn.he.zhao.bbs.service.interf;

import java.util.Locale;
import java.util.Map;

/**
 * 描述:
 * Lang配置文件服务
 *
 * @Author HeFeng
 * @Create 2018-06-02 13:11
 */
public interface LangPropsService {
    String get(String var1);

    String get(String var1, Locale var2);

    Map<String, String> getAll(Locale var1);
}
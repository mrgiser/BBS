package cn.he.zhao.bbs.service;

/**
 * 描述:
 * string bool 转换测试
 *
 * @Author HeFeng
 * @Create 2018-10-14 11:51
 */
public class TestStrBool {
    public static void main(String[] args) {
        boolean bool = true;
        String str = "true";

        System.out.println(String.valueOf(bool));
        System.out.println(""+ Boolean.valueOf(str));
    }
}
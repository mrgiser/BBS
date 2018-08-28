package cn.he.zhao.bbs.entity;

public class Emotion {

    private String oid;

    /**
     * Key of emotion user id.
     */
    private String emotionUserId;

    /**
     * Key of emotion content.
     */
    private String emotionContent;

    /**
     * Key of emotion sort.
     */
    private Integer emotionSort;

    /**
     * Key of emotion type.
     */
    private Integer emotionType;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getEmotionUserId() {
        return emotionUserId;
    }

    public void setEmotionUserId(String emotionUserId) {
        this.emotionUserId = emotionUserId;
    }

    public String getEmotionContent() {
        return emotionContent;
    }

    public void setEmotionContent(String emotionContent) {
        this.emotionContent = emotionContent;
    }

    public Integer getEmotionSort() {
        return emotionSort;
    }

    public void setEmotionSort(Integer emotionSort) {
        this.emotionSort = emotionSort;
    }

    public Integer getEmotionType() {
        return emotionType;
    }

    public void setEmotionType(Integer emotionType) {
        this.emotionType = emotionType;
    }
}

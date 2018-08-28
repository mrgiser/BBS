/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.he.zhao.bbs.entity;

/**
 * This class defines all emotion entity relevant keys.
 *
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.0.0.1, Dec 13, 2016
 * @since 1.5.0
 */
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

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

import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Set;

/**
 * This class defines all character entity relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.0, Jul 8, 2016
 * @since 1.4.0
 */
public class Character {

    private String oid;

    /**
     * Key of character user id.
     */
    private String characterUserId;

    /**
     * Key of character image.
     */
    private String characterImg;

    /**
     * Key of character content.
     */
    private String characterContent;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getCharacterUserId() {
        return characterUserId;
    }

    public void setCharacterUserId(String characterUserId) {
        this.characterUserId = characterUserId;
    }

    public String getCharacterImg() {
        return characterImg;
    }

    public void setCharacterImg(String characterImg) {
        this.characterImg = characterImg;
    }

    public String getCharacterContent() {
        return characterContent;
    }

    public void setCharacterContent(String characterContent) {
        this.characterContent = characterContent;
    }
}

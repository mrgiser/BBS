package cn.he.zhao.bbs.entity;

import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Set;

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

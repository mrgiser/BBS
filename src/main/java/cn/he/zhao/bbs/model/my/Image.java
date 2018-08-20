package cn.he.zhao.bbs.model.my;

/**
 * 描述:
 * Image
 *
 * @Author HeFeng
 * @Create 2018-08-20 18:17
 */
public final class Image {
    private String name;
    private byte[] data;

    public Image() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
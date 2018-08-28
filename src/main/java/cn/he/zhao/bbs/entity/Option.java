package cn.he.zhao.bbs.entity;

public class Option {

    private String oid;

    /**
     * Key of option value.
     */
    private String optionValue;

    /**
     * Key of option category.
     */
    private String optionCategory;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    public String getOptionCategory() {
        return optionCategory;
    }

    public void setOptionCategory(String optionCategory) {
        this.optionCategory = optionCategory;
    }
}

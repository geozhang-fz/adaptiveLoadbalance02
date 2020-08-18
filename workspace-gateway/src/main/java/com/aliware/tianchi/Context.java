package com.aliware.tianchi;

public class Context {
    // provider服务器的编码
    private int id;
    // 静态权重
    private Integer weight;
    // 动态权重
    private Integer curWeight;
    private static Integer position = 0;



    /* Constructor */
    public Context(int id, Integer weight, Integer curWeight) {
        this.id = id;
        this.weight = weight;
        this.curWeight = curWeight;
    }

    /* Getter & Setter */
    public static Integer getPosition() {
        return position;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getCurWeight() {
        return curWeight;
    }

    public void setCurWeight(Integer curWeight) {
        this.curWeight = curWeight;
    }

    /* Common Methods */
    private static final Object lockP = new Object();

    public static Integer incrementAndGetPos() {
        synchronized (lockP) {
            position++;
            return position;
        }
    }
}

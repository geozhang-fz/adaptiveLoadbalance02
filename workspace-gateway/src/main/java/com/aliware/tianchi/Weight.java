package com.aliware.tianchi;

public class Weight {
    private int id;
    private Integer weight;
    private Integer curWeight;

    public Weight(int id, Integer weight, Integer curWeight) {
        this.id = id;
        this.weight = weight;
        this.curWeight = curWeight;
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
}

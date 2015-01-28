package com.addressbook.thorrism.addressbook;

/**
 * Created by Lucas Crawford on 1/11/2015.
 */
public class Pair<Left, Right>{
    private Left lVal;
    private Right rVal;

    public Pair(Left left, Right right){
        lVal = left;
        rVal = right;
    }

    public Left getLeft(){
        return lVal;
    }

    public Right getRight(){
        return rVal;
    }

    public void setLeft(Left left){
        this.lVal = left;
    }

    public void setRight(Right right){
        this.rVal = right;
    }


}

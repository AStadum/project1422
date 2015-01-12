package com.addressbook.thorrism.addressbook;

/**
 * Created by Lucas Crawford on 1/11/2015.
 */
public class Pair<Left, Right>{
    private final Left lVal;
    private final Right rVal;

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
}

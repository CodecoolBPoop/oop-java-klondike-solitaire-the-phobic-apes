package com.codecool.klondike;

public enum SuitType{
    HARTS,      //red
    DIAMONDS,   //red
    SPADES,     //black
    CLUBS;      //black

    public static int getIndex(SuitType type){
        int position = 0;
        for (int i = 0; i < SuitType.values().length; i++) {
            if(type.equals(SuitType.values()[i])){
                position = i + 1;
            }
        }
        return position;
    }
}

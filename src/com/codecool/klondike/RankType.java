package com.codecool.klondike;

public enum RankType{
    ACE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    JACK,
    QUEEN,
    KING;

    public static int getIndex(RankType type){
        int position = 0;
        for (int i = 0; i < RankType.values().length; i++) {
            if(type.equals(RankType.values()[i])){
                position = i + 1;
            }
        }
        return position;
    }
}

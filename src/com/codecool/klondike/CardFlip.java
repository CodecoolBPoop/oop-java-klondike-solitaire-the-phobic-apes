package com.codecool.klondike;

import java.util.HashMap;

public class CardFlip extends Action {

    private Card flippedCard;


    CardFlip(Card flippefCard, ActionType actionType){
        super(actionType);
        this.flippedCard = flippefCard;
    }

    @Override
    public HashMap<String,Object> getAffectedResource() {
        HashMap<String, Object> resource = new HashMap<>();
        resource.put("flippedCard", flippedCard);
        return resource;
    }
}

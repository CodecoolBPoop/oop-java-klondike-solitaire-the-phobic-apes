package com.codecool.klondike;

import java.util.HashMap;
import java.util.List;

public class CardDrag extends Action {

    private Pile lastSourcePile;
    private List<Card> lastMovedCards;
    private Pile lastDestPile;


    CardDrag(Pile sourcePile, List<Card> movedCards, Pile destPile, ActionType actionType){
        super(actionType);
        this.lastSourcePile = sourcePile;
        this.lastMovedCards = movedCards;
        this.lastDestPile = destPile;
    }

    @Override
    public HashMap<String,Object> getAffectedResource() {
        HashMap<String,Object> resource = new HashMap<>();
        resource.put("lastSourcePile",lastSourcePile);
        resource.put("lastMovedCards",lastMovedCards);
        resource.put("lastDestPile",lastDestPile);
        return resource;
    }
}

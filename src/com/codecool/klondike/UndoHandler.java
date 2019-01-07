package com.codecool.klondike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UndoHandler {

    private List<Action> actions = new ArrayList<>();


    public void setUndoData(Action undoData) {
        this.actions.add(undoData);
    }

    public void undoMove() {

        if (actions.size() > 0) {
            Action action = actions.get(actions.size() - 1);
            if (action.getActionType().equals(Action.ActionType.CARD_DRAG)) {
                HashMap<String, Object> affectedResource = action.getAffectedResource();
                List<Card> lastMovedCards = (List<Card>) affectedResource.get("lastMovedCards");
                Pile lastDestinationPile = (Pile) affectedResource.get("lastDestPile");
                Pile lastSourcePile = (Pile) affectedResource.get("lastSourcePile");

                for (Card card : lastMovedCards) {
                    lastDestinationPile.getCards().remove(card);
                }
                MouseUtil.slideToDest(lastMovedCards, lastSourcePile);

            } else if (action.getActionType().equals(Action.ActionType.CARD_FLIP)) {
                Card card = (Card) action.getAffectedResource().get("flippedCard");
                card.flip();
            }

            actions.remove(actions.size() - 1);
        }
    }

}

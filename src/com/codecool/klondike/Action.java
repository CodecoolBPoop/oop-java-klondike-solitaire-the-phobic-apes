package com.codecool.klondike;

import java.util.HashMap;

public abstract class Action {

    private ActionType actionType;
    public abstract HashMap<String,Object> getAffectedResource();

    public Action(ActionType actionType){
        this.actionType = actionType;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public enum ActionType{
        CARD_DRAG,
        CARD_FLIP;
    }
}

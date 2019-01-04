package com.codecool.klondike;

import javafx.scene.input.MouseEvent;
import java.util.List;

class CardMover {
    private boolean cardsProcessed = false;
    private List<Card> draggedCards;
    private double dragStartX, dragStartY;

    CardMover(List<Card> draggedCards) {
        this.draggedCards = draggedCards;
        this.dragStartX = dragStartX;
        this.dragStartY = dragStartY;
    }

    void moveDraggedCards(double offsetX, double offsetY) {
        for (Card card : draggedCards) {
//            card.getDropShadow().setRadius(20);
//            card.getDropShadow().setOffsetX(10);
//            card.getDropShadow().setOffsetY(10);

            card.setTranslateX(offsetX);
            card.setTranslateY(offsetY);

        }
    }

    void bringCardsToFront() {
        for (Card card : draggedCards) {
            card.toFront();
        }
    }

    void getDraggedCards(Card card, Pile activePile) {
        List<Card> cards = activePile.getCards();
        int cardIndex = cards.indexOf(card);
        int pileSize = cards.size();

        if (cardIndex != pileSize - 1) {
            for (int i = cardIndex; i < pileSize; i++) {
                draggedCards.add(cards.get(i));
            }
        }else{
            draggedCards.add(card);
        }
    }

    void cardsDropped() {
        cardsProcessed = false;
    }

    void processCards(MouseEvent e) {
        Card card = (Card) e.getSource();

        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        if(!cardsProcessed){
            Pile activePile = card.getContainingPile();
            if (activePile.getPileType() == Pile.PileType.STOCK)
                return;

            draggedCards.clear();
            getDraggedCards(card, activePile);
            bringCardsToFront();
            cardsProcessed = true;
        }
        moveDraggedCards(offsetX, offsetY);
    }

    public void setDragStartX(double dragStartX) {
        this.dragStartX = dragStartX;
    }

    public void setDragStartY(double dragStartY) {
        this.dragStartY = dragStartY;
    }
}

package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.*;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;

    private CardMover cardMover = new CardMover(draggedCards);
    Button restartBtn = new Button("Restart");


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
        if(card.getContainingPile().getPileType() == Pile.PileType.TABLEAU){
            if(card == card.getContainingPile().getTopCard() && card.getContainingPile().getTopCard().isFaceDown()) {
                card.flip();
            }
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
        cardMover.setDragStartX(dragStartX);
        cardMover.setDragStartY(dragStartY);
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        cardMover.processCards(e);
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        cardMover.cardsDropped();
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile pile = getValidIntersectingPile(card, tableauPiles);
        //TODO

        if (pile != null) {
            handleValidMove(card, pile);
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }

    };

    public boolean isGameWon() {
        //TODO
        return false;
    }

    public Game() {
        deck = Card.createNewDeck();
        Collections.shuffle(deck);
        getChildren().add(restartBtn);
        initPiles();
        dealCards();
        addButtonsEventHandlers();
    }

    private void restartGame() {

        for (Card card:stockPile.getCards()) {
            getChildren().remove(card);

        }

        for (Card card:discardPile.getCards()) {
            getChildren().remove(card);
        }

        for (int i = 0; i< tableauPiles.size(); i++) {
            for (Card card: tableauPiles.get(i).getCards()) {
                getChildren().remove(card);
            }
        }

        deck = Card.createNewDeck();
        Collections.shuffle(deck);
        dealCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void addButtonsEventHandlers() {
        restartBtn.setOnAction((event -> restartGame()));
    }

    public void refillStockFromDiscard() {
        //TODO
        System.out.println("Stock refilled from discard pile.");
    }

    
    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        if (destPile.getTopCard() == null){
            if(card.getRank().equals(RankType.KING)){
                return true;
            }else{
                return false;
            }
        }

        return Card.isOppositeColor(card, destPile.getTopCard())
                && Card.isCardOneLowerThanTopCard(card, destPile.getTopCard());

    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void fillTableauPiles() {

        Card card;

        for (int i = 0; i < tableauPiles.size(); i++) {
            for (int j = 0; j < i + 1; j++) {
                card = stockPile.getTopCard();
                card.moveToPile(tableauPiles.get(i));

            }
        }
    }

    public void flipTopCards(){
        for (int i = 0; i < tableauPiles.size(); i++) {
            if (tableauPiles.get(i).getTopCard().isFaceDown()){
                tableauPiles.get(i).getTopCard().flip();
            }
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        stockPile.clear();
        discardPile.clear();
        for (int i = 0; i < tableauPiles.size() ; i++) {
            tableauPiles.get(i).clear();
        }
        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });
        fillTableauPiles();
        flipTopCards();
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}

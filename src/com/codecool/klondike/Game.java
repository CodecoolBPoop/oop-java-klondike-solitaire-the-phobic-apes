package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

public class Game extends Pane {

    private Stage primaryStage;
    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();
    private List<List<Pile>> possiblePiles = new ArrayList<>();
    final Stage dialog = new Stage();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;

    private boolean IS_WIN_CONDITION_TEST = false;
    private String WIN_MESSAGE = "Congratulation you won!";

    private CardMover cardMover = new CardMover(draggedCards);

    private VBox controlBtnsLayout;
    private Button restartBtn;
    private Button undoBtn;

    private UndoHandler undoHandler = new UndoHandler();


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK && card.getContainingPile().getTopCard().equals(card)) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
        if (card.getContainingPile().getPileType() == Pile.PileType.TABLEAU) {
            if (card == card.getContainingPile().getTopCard() && card.getContainingPile().getTopCard().isFaceDown()) {
                card.flip();
                undoHandler.setUndoData(new CardFlip(card, Action.ActionType.CARD_FLIP));
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
        Pile pile = getValidIntersectingPile(card, possiblePiles);

        if (pile != null) {
            handleValidMove(card, pile);
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }
        if(isGameWon()){
            showWinMessage();
        }
    };

    boolean isGameWon() {
        return stockPile.isEmpty() && discardPile.isEmpty() && isFoundationPilesAreFull();
    }

    void showWinMessage() {
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        VBox dialogVbox = new VBox(20);

        Button restartBtnInDialogBox = new Button("Restart");
        restartBtnInDialogBox.setOnAction((e)-> {
            restartGame();
            //dialog.close();
        });

        dialog.setOnCloseRequest((e)->{
            restartGame();
            dialog.close();
        });


        dialogVbox.getChildren().addAll(new Text(WIN_MESSAGE), restartBtnInDialogBox);
        Scene dialogScene = new Scene(dialogVbox, 200, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }


    private boolean isFoundationPilesAreFull() {
        boolean isFoundationPilesAreFull = true;

        for (Pile pile:foundationPiles) {
            if(!pile.getTopCard().getRank().equals(RankType.KING)){
                isFoundationPilesAreFull = false;
            }
        }

        return isFoundationPilesAreFull;
    }

    public Game(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initPossiblePiles();
        deck = Card.createNewDeck();
        if(!IS_WIN_CONDITION_TEST){
            Collections.shuffle(deck);
        }
        initControlLayoutAndBtns();
        initPossiblePiles();
        initPiles();
        dealCards();
    }

    private void initControlLayoutAndBtns(){
        controlBtnsLayout = new VBox(5);
        restartBtn = new Button("Restart");
        undoBtn = new Button("Undo");

        restartBtn.setOnAction((event -> restartGame()));
        undoBtn.setOnAction((event -> undoHandler.undoMove()));

        controlBtnsLayout.getChildren().addAll(restartBtn, undoBtn);
        getChildren().add(controlBtnsLayout);
    }

    private void restartGame() {

        for (Card card : stockPile.getCards()) {
            getChildren().remove(card);

        }

        for (Card card : discardPile.getCards()) {
            getChildren().remove(card);
        }

        for (int i = 0; i < tableauPiles.size(); i++) {
            for (Card card : tableauPiles.get(i).getCards()) {
                getChildren().remove(card);
            }
        }

        resetFoundationPiles();

        deck = Card.createNewDeck();
        Collections.shuffle(deck);
        dealCards();
    }

    private void initPossiblePiles() {
        possiblePiles.add(foundationPiles);
        possiblePiles.add(tableauPiles);
    }

    private void resetFoundationPiles() {
        for (Pile pile : foundationPiles) {
            for (Card card : pile.getCards()) {
                getChildren().remove(card);
            }
        }

        for (Pile pile : foundationPiles) {
            pile.getCards().clear();
        }
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        for (int i = discardPile.getCards().size() - 1; i >= 0; i--) {
            Card card = discardPile.getCards().get(i);
            card.flip();
            card.moveToPile(stockPile);
        }
        System.out.println("Stock refilled from discard pile.");
    }


    private Pile getValidIntersectingPile(Card card, List<List<Pile>> piles) {
        Pile result = null;
        for (List<Pile> list : piles) {
            for (Pile pile : list) {
                if (!pile.equals(card.getContainingPile()) &&
                        isOverPile(card, pile) &&
                        isMoveValid(card, pile))
                    result = pile;
            }
        }
        return result;
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        if (destPile.getPileType().equals(Pile.PileType.TABLEAU)) {
            if (destPile.getTopCard() == null) {
                return card.getRank().equals(RankType.KING);
            }

            return Card.isOppositeSuit(card, destPile.getTopCard())
                    && Card.isCardRankBelowToTopCard(card, destPile.getTopCard());
        } else {
            if (destPile.getTopCard() == null) {
                return card.getRank().equals(RankType.ACE);

            } else {
                return Card.isCardRankHigherThanTopCard(card, destPile.getTopCard())
                        && Card.isSameSuit(card, destPile.getTopCard());
            }
        }
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

        List<Card> movedCards = new ArrayList<>();
        movedCards.addAll(draggedCards);

        undoHandler.setUndoData(new CardDrag(
                card.getContainingPile(), movedCards , destPile, Action.ActionType.CARD_DRAG)
        );

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
                if (stockPile.getTopCard() != null) {
                    card = stockPile.getTopCard();
                    card.moveToPile(tableauPiles.get(i));
                }

            }
        }
    }

    public void flipTopCards() {
        for (int i = 0; i < tableauPiles.size(); i++) {
            if(tableauPiles.get(i).getTopCard() != null){
                if (tableauPiles.get(i).getTopCard().isFaceDown()) {
                    tableauPiles.get(i).getTopCard().flip();
                }
            }
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        stockPile.clear();
        discardPile.clear();
        for (int i = 0; i < tableauPiles.size(); i++) {
            tableauPiles.get(i).clear();
        }
        if (IS_WIN_CONDITION_TEST) {
            buildTestState(deckIterator);
        }else{
            deckIterator.forEachRemaining(card -> {
                stockPile.addCard(card);
                addMouseEventHandlers(card);
                getChildren().add(card);
            });
        }
        fillTableauPiles();
        flipTopCards();
    }

    private void buildTestState(Iterator<Card> deckIterator) {
        for (int i = 0; i < 52; i++) {
            Card card = deckIterator.next();
            card.flip();
            if(i <= 12){
                foundationPiles.get(0).addCard(card);

            }else if(i > 12 && i <= 25){
                foundationPiles.get(1).addCard(card);

            }else if(i > 25 && i <= 38){
                foundationPiles.get(2).addCard(card);

            }else{
                foundationPiles.get(3).addCard(card);
            }

            addMouseEventHandlers(card);
            getChildren().add(card);
        }
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}

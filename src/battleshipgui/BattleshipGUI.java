package battleshipgui;

import javafx.application.Application;
import javafx.scene.layout.GridPane;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import java.io.File;
import java.io.FileNotFoundException;
import javafx.stage.FileChooser;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.util.Scanner;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Alert.AlertType;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Line;


public class BattleshipGUI extends Application {

    //Loads the player and CPU text files when called on
    public static void loadFile(Button[][] playerBoard, Button[][] cpuBoard, String player, Label playerText, Label cpuText, Button[][] cpuCopy) {
        try {
            Scanner file = new Scanner(new File(player));
            int i = 0;
            while (file.hasNextLine()) {
                String[] chars = file.nextLine().split(" ");
                for (int j = 0; j < playerBoard[i].length; j++) {
                    //The game board loads whichever file the user chooses
                    if (player.equals("PLAYER.txt")) {
                        playerText.setText("File Loaded!");
                        playerBoard[j][i].setText(chars[j]);
                        playerBoard[i][j].setStyle(
                                "-fx-text-fill: BLUE;"
                                + "-fx-font-size: 16;"
                                + "-fx-font-weight: BOLD;"
                                + "-fx-opacity: 1");
                    } else if (player.equals("CPU.txt")) {
                        cpuText.setText("File Loaded!");
                        cpuCopy[j][i].setText(chars[j]);
                        cpuBoard[j][i].setText("*");
                        cpuBoard[i][j].setStyle(
                                "-fx-text-fill: RED;"
                                + "-fx-font-size: 16;"
                                + "-fx-font-weight: BOLD;"
                                + "-fx-opacity: 1");
                    }

                    //Once both files are loaded in, the game can now start and the buttons become interactive
                    if (("File Loaded!".equals(playerText.getText())) && ("File Loaded!".equals(cpuText.getText()))) {
                        cpuBoard[i][j].setDisable(false);
                    }
                }
                i++;
            }
        } catch (FileNotFoundException exception) {
            playerText.setText("Error opening file");
        }
    }

    //When a button on the CPU board is clicked, it changes the button to either a hit or miss photo depending on whether they hit a ship or not
    public static void makeMove(Button[][] cpuBoard, Button[][] cpuCopy, Label playerText, Button[][] playerBoard, Label cpuText, int[][] hp, Label playerSunk, Label cpuSunk, int[] hits) {
        String winnerPlayer = "The player has won!";
        int boardType = 0;

        for (int i = 0; i < cpuCopy.length; i++) {
            for (int j = 0; j < cpuCopy[i].length; j++) {
                final Button coordinateCopy = cpuCopy[i][j];
                final Button coordinate = cpuBoard[i][j];
                cpuBoard[i][j].setOnAction((ActionEvent e) -> {
                    playerSunk.setText("");

                    if ("*".equals(coordinateCopy.getText())) {
                        coordinate.setGraphic(new ImageView("M.png"));
                        playerText.setText("You have missed sir!");
                        coordinate.setStyle("-fx-background-color: GRAY;"
                                + "-fx-opacity: 1");
                    } else {
                        //If the user hits a ship, the shipSinker and winner method is called on
                        shipSinker(hp, boardType, coordinateCopy, playerSunk, cpuSunk);
                        coordinate.setGraphic(new ImageView("H.png"));
                        playerText.setText("Direct hit, nice shot sir!");
                        coordinate.setStyle("-fx-background-color: DIMGRAY;"
                                + "-fx-opacity: 1");
                        winner(boardType, hits, winnerPlayer, playerBoard, cpuBoard, playerText, cpuText);
                    }
                    //The text on the button attacked is removed showing only the image, and the button becomes non-interactive
                    coordinate.setText("");
                    coordinate.setDisable(true);
                    //The randomMove method is called if the user hasn't won yet
                    if (hits[0] != 0) {
                        randomMove(playerBoard, cpuText, hp, playerSunk, cpuSunk, hits, cpuBoard, playerText);
                    }
                });
            }
        }
    }

    //This method triggers a random button on the player board that has not been touched to be attacked
    public static void randomMove(Button[][] playerBoard, Label cpuText, int hp[][], Label playerSunk, Label cpuSunk, int[] hits, Button[][] cpuBoard, Label playerText) {
        String winnerPlayer = "The CPU has won!";
        int boardType = 1;
        String letter = "ABCDEFGHIJ";
        String[][] shipType = {{"Battleship", "Aircraft Carrier", "Destroyer", "Submarine", "Patrol Boat"}, {"BCDSP"}};
        cpuSunk.setText("");

        int i = (int) (Math.round((Math.random() * 9)));
        int j = (int) (Math.round((Math.random() * 9)));
        do {
            i = (int) (Math.round((Math.random() * 9)));
            j = (int) (Math.round((Math.random() * 9)));
        } while (playerBoard[i][j].getGraphic() != null);
        final Button coordinateCopy = playerBoard[i][j];

        //The button image changes to either a hit or miss photo depending on whether it hit a ship or not and lets the user know whether the computer missed or hit
        if ("*".equals(coordinateCopy.getText())) {
            playerBoard[i][j].setGraphic(new ImageView("M.png"));
            playerBoard[i][j].setStyle("-fx-background-color: GRAY;"
                    + "-fx-opacity: 1");
            cpuText.setText("The computer has attacked " + letter.charAt(j) + Integer.toString(i) + " and missed!");
        } else {
            //If the computer hits one of the users ships, it goes through the shipSinker and winner method
            shipSinker(hp, boardType, coordinateCopy, playerSunk, cpuSunk);
            playerBoard[i][j].setGraphic(new ImageView("H.png"));
            playerBoard[i][j].setStyle("-fx-background-color: DIMGRAY;"
                    + "-fx-opacity: 1");
            cpuText.setText("The computer has attacked " + letter.charAt(j) + Integer.toString(i) + " and hit your " + shipType[0][shipType[1][0].indexOf(coordinateCopy.getText())] + "!");
            winner(boardType, hits, winnerPlayer, playerBoard, cpuBoard, playerText, cpuText);
        }
        //Removes the original text, leaving only the image on the button
        playerBoard[i][j].setText("");
    }

    //This method is called after a ship has been hit and checks whether it has sunken or not 
    public static void shipSinker(int[][] hp, int boardType, Button coordinateCopy, Label playerSunk, Label cpuSunk) {
        String[][] shipType = {{"Battleship", "Aircraft Carrier", "Destroyer", "Submarine", "Patrol Boat"}, {"BCDSP"}};
        //Int ship corresponds to the type of ship, 0 = Battleship, 1 = Aircraft Carrier, etc
        int ship = shipType[1][0].indexOf(coordinateCopy.getText());

        //Each time a ship is hit, that specific ship loses a HP and once it hits 0, the user is notified
        hp[boardType][ship]--;
        if (hp[boardType][ship] == 0) {
            if (boardType == 0) {
                playerSunk.setText("You have sunk the enemy's " + shipType[0][ship] + " captain!");
            } else {
                cpuSunk.setText("The CPU has sunk your " + shipType[0][ship] + " captain!");
            }
        }
        //The texts goes away once you make a move again
    }

    //This method is called on when either the CPU or user hits a ship and depending on which board was hit, that board loses a HP
    public static void winner(int boardType, int[] hits, String winnerPlayer, Button[][] playerBoard, Button[][] cpuBoard, Label playerText, Label cpuText) {
        Alert winner = new Alert(AlertType.INFORMATION, winnerPlayer);
        hits[boardType]--;
        //Once its HP count hits 0, a message pops up declaring the winner and ending the game
        if (hits[boardType] == 0) {
            //The boards will then have their images and texts removed, displaying a shade of gray. The shade of gray is assigned within the makeMove and randomMove method
            restart(playerBoard, cpuBoard, playerText, cpuText, hits);
            winner.show();
        }
    }

    //This method causes the game to become non-interactive either by winning or restarting the game
    public static void restart(Button[][] playerBoard, Button[][] cpuBoard, Label playerText, Label cpuText, int[] hits) {
        for (int i = 0; i < playerBoard.length; i++) {
            for (int j = 0; j < playerBoard[i].length; j++) {
                playerBoard[i][j].setText("");
                cpuBoard[i][j].setText("");
                playerBoard[i][j].setGraphic(null);
                cpuBoard[i][j].setGraphic(null);
                playerBoard[i][j].setDisable(true);
                cpuBoard[i][j].setDisable(true);

                //If the user decides to restart the game, the buttons and text go back to their original values
                if ((hits[0] != 0) && (hits[1] != 0)) {
                    playerBoard[i][j].setStyle("-fx-opacity: 1");
                    cpuBoard[i][j].setStyle("-fx-opacity: 1");
                    playerText.setText("Please open the Player.txt file!");
                    cpuText.setText("Please open the CPU.txt file!");
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        //Creates the layout panes and sets their properties
        GridPane mainPane = new GridPane();
        BorderPane root = new BorderPane();
        GridPane boards = new GridPane();
        GridPane boardOne = new GridPane();
        GridPane boardTwo = new GridPane();
        GridPane playerBox = new GridPane();
        GridPane cpuBox = new GridPane();

        boards.setHgap(10);
        mainPane.setVgap(10);
        mainPane.setHgap(72);
        mainPane.setPadding(new Insets(10, 10, 10, 10));
        boards.setPadding(new Insets(5, 10, 10, 10));
        playerBox.setPadding(new Insets(0, 0, 40, 10));
        cpuBox.setPadding(new Insets(0, 0, 40, 10));

        //Allows the user to access the game board files
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("/Users/tiffany/NetBeansProjects/BattleshipGUI 5.14.50 PM"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        //Creates a menu bar to open the text files, restart the game or to exit the GUI		   
        Menu menu = new Menu("File");
        MenuItem m1 = new MenuItem("Open");
        MenuItem m2 = new MenuItem("Restart Game");
        MenuItem m3 = new MenuItem("Exit");
        menu.getItems().addAll(m1, m2, m3);
        MenuBar menuBar = new MenuBar(menu);

        //Creates keyboard shortcuts for the menu items and assigns them to their menu items
        KeyCombination open = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
        KeyCombination restart = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
        KeyCombination exit = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
        m1.setAccelerator(open);
        m2.setAccelerator(restart);
        m3.setAccelerator(exit);

        //Creates the game boards, and sets the dimensions of each button
        Button[][] playerBoard = new Button[10][10];
        Button[][] cpuBoard = new Button[10][10];
        Button[][] cpuCopy = new Button[10][10];
        for (int i = 0; i < playerBoard.length; i++) {
            for (int j = 0; j < playerBoard[i].length; j++) {
                //The cpuCopy board will hold the actual CPU text file and will not be displayed, whereas the cpuBoard holds "*" characters and is displayed
                cpuCopy[i][j] = new Button();
                playerBoard[i][j] = new Button();
                cpuBoard[i][j] = new Button();
                playerBoard[i][j].setMinHeight(50);
                playerBoard[i][j].setMaxHeight(50);
                playerBoard[i][j].setMinWidth(50);
                playerBoard[i][j].setMaxWidth(50);
                cpuBoard[i][j].setMinHeight(50);
                cpuBoard[i][j].setMinWidth(50);
                cpuBoard[i][j].setMaxHeight(50);
                cpuBoard[i][j].setMaxWidth(50);
                boardOne.add(playerBoard[i][j], i, j);
                boardTwo.add(cpuBoard[i][j], i, j);
            }
        }
        //Creates the labels, images and arrays used in this game
        int[][] hp = {{4, 5, 3, 3, 2}, {4, 5, 3, 3, 2}};
        int[] hits = {17, 17};
        ImageView rowPlayer = new ImageView(new Image(getClass().getResourceAsStream("rows.png")));
        ImageView colPlayer = new ImageView(new Image(getClass().getResourceAsStream("cols.png")));
        ImageView rowCPU = new ImageView(new Image(getClass().getResourceAsStream("rows.png")));
        ImageView colCPU = new ImageView(new Image(getClass().getResourceAsStream("cols.png")));
        Label playerMessages = new Label("Player Messages");
        Label cpuMessages = new Label("CPU Messages");
        Label playerText = new Label("");
        Label cpuText = new Label("");
        Label playerSunk = new Label("");
        Label cpuSunk = new Label("");
        Line verticalLine = new Line(0, 0, 0, 500);

        //Changes the font style, size, colour, and adds the borders
        playerMessages.setFont(Font.font("Times New Roman", FontWeight.BOLD, 20));
        cpuMessages.setFont(Font.font("Times New Roman", FontWeight.BOLD, 20));
        playerText.setFont(Font.font("Times New Roman", FontWeight.BOLD, 25));
        cpuText.setFont(Font.font("Times New Roman", FontWeight.BOLD, 25));
        playerSunk.setFont(Font.font("Times New Roman", FontWeight.BOLD, 25));
        cpuSunk.setFont(Font.font("Times New Roman", FontWeight.BOLD, 25));
        playerMessages.setStyle("-fx-text-fill: BLUE");
        cpuMessages.setStyle("-fx-text-fill: RED");
        playerText.setStyle("-fx-text-fill: BLUE");
        cpuText.setStyle("-fx-text-fill: RED");
        playerSunk.setStyle("-fx-text-fill: BLUE");
        cpuSunk.setStyle("-fx-text-fill: RED");
        playerBox.setStyle("-fx-border-color: BLACK");
        cpuBox.setStyle("-fx-border-color: BLACK");
        boards.setStyle("-fx-border-color: BLACK");

        //Starts the game and calls on the makeMove method
        restart(playerBoard, cpuBoard, playerText, cpuText, hits);
        makeMove(cpuBoard, cpuCopy, playerText, playerBoard, cpuText, hp, playerSunk, cpuSunk, hits);

        //Assigns each menu item with a specific task when clicked
        m1.setOnAction((ActionEvent e) -> {
            String player = fileChooser.showOpenDialog(primaryStage).getName();
            loadFile(playerBoard, cpuBoard, player, playerText, cpuText, cpuCopy);
        });

        m2.setOnAction((ActionEvent e) -> {
            //The text and hit points goes back to their original values and the game restarts
            playerSunk.setText("");
            cpuSunk.setText("");
            for (int i = 0; i < 2; i++) {
                hp[i][0] = 4;
                hp[i][1] = 5;
                hp[i][2] = 3;
                hp[i][3] = 3;
                hp[i][4] = 2;
                hits[i] = 17;
            }
            restart(playerBoard, cpuBoard, playerText, cpuText, hits);
        });

        m3.setOnAction((ActionEvent e) -> {
            primaryStage.close();
        });

        //Adds the variables onto the scene
        root.setTop(menuBar);
        root.setCenter(mainPane);
        boards.add(boardOne, 1, 1);
        boards.add(boardTwo, 4, 1);
        boards.add(rowPlayer, 0, 1);
        boards.add(rowCPU, 3, 1);
        boards.add(verticalLine, 2, 1);
        mainPane.add(colPlayer, 1, 0);
        mainPane.add(colCPU, 2, 0);
        playerBox.add(playerMessages, 0, 0);
        playerBox.add(playerText, 0, 1);
        playerBox.add(playerSunk, 0, 2);
        cpuBox.add(cpuMessages, 0, 0);
        cpuBox.add(cpuText, 0, 1);
        cpuBox.add(cpuSunk, 0, 2);
        mainPane.add(boards, 0, 1, 3, 1);
        mainPane.add(playerBox, 0, 2, 3, 3);
        mainPane.add(cpuBox, 0, 5, 3, 6);

        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setTitle("Battleship GUI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

//class Game : container class for all actions and states of a game, separate from the game board. Gateway between the user and the board, but uses the main class to get input.
class Game {
  Board board;
  boolean running; //false if paused or the game has been terminated, won or lost
  long pauseTime; //used to rest time if the user chooses to pause in between

  //Method Game () : Constructor for this class. Prompts the user for the size of the board and initializes the board.
  //Pre : N/A
  //Post : Initializes board object with the dimensions provided by the user.
  public Game () {
    int width = 5, height = 5;
    running = true;
    
    outerloop:
    while (true) {
      Main.clearScreen ();

      switch (Main.getChoice (new String [] {"1 - 9 x 9", "2 - 16 x 16", "3 - 30 x 16", "4 - Custom Board", "5 - Cancel"}, Main.green + "Choose board size:" + Main.reset)) {

        case '1':
          width = height = 9;
          break outerloop;

        case '2':
          width = height = 16;
          break outerloop;

        case '3':
          width = 30;
          height = 16;
          break outerloop;

        case '4':
          width = Main.getNumber (5, 40, "width: ");
          if (width == -1) {
            continue outerloop;
          }

          height = Main.getNumber (5, 20, "height: ");
          if (height == -1) {
            continue outerloop;
          }

          break outerloop;

        case '5':
          running = false;
          return;
      }
    }

    board = new Board (width, height);
  }

  //Method resume () : Resumes a game already created by the user
  //Pre : The Game object must already have been created and initialized
  //Post : The boolean running is set to true, to communicate with class Main that the game is in progress, and the start time is reset to discount the time that passed while the game was paused. The board is then printed to the screen.
  public void resume () {
    running = true;
    board.startTime = board.startTime + System.currentTimeMillis () - pauseTime;
    board.printDisplay ();
  }

  //Method run () : Prmpts the user for a game action, one of: 1) Make a move, 2) Pause the game, 3) Discard the game, 4) Flag a mine and 5) Remove a flag. Action is taken based on the user's choice.
  //Pre : The Game object has been declared and initialized
  //Post : The user's choice of action has been implemented (Eg. moving a number of spaces, flagging/unflagging a square, pausing/discarding the game). Also implements a series of actions if the user has won or lotst the game as a result of a move.
  public void run () {
    int time, count, moves, moveX, moveY, posX, posY;
    double score;
    int [] tile;
    String options [] = {"1 - Make a move", "2 - Pause and return to menu", "3 - Discard game", "4 - Flag a mine", "5 - Remove a flag"};
 
    if (board.flags < 1) {
      options = removeArrayElement (options, 4);
    }

    switch (Main.getChoice (options, Main.green + "Choose an action: " + Main.reset)) {

      case '1':
        count = 0;
        moveX = 0;
        moveY = 0;
        posX = board.posX;
        posY = board.posY;
        options = new String [] {"R - Up Left", "T - Up", "Y - Up Right", "F - Left", "H - Right", "V - Down Left", "B - Down", "N - Down Right", "G - Cancel"};

        for (int row = board.posY - 1; row <= board.posY + 1; row++) {
          for (int column = board.posX - 1; column <= board.posX + 1; column++) {
            if (row == board.posY && column == board.posX) {
              continue;
            }
            
            if (!isValidMove (column, row)) {
              options = removeArrayElement (options, count);
            } else {
              count++;
            }
          }
        }

        switch (Main.getChoice (options, Main.green + "Choose your direction: " + Main.reset)) {
          case 'r':
            moveX = -1;
            moveY = -1;
            break;
          case 't':
            moveX = 0;
            moveY = -1;
            break;
          case 'y':
            moveX = 1;
            moveY = -1;
            break;
          case 'f':
            moveX = -1;
            moveY = 0;
            break;
          case 'h':
            moveX = 1;
            moveY = 0;
            break;
          case 'v':
            moveX = -1;
            moveY = 1;
            break;
          case 'b':
            moveX = 0;
            moveY = 1;
            break;
          case 'n':
            moveX = 1;
            moveY = 1;
            break;
          case 'g':
            return;
        }

        count = 0;
        
        while (true) {
          posX += moveX;
          posY += moveY;

          if (!isValidMove (posX, posY)) {
            break;
          }

          count++;
        }

        moves = Main.getNumber (1, count, "number of spaces to move: ");

        if (moves == -1) {
          return;
        }

        posX = board.posX;
        posY = board.posY;

        for (int k = 0; k < moves; k++) {
          posX += moveX;
          posY += moveY;

          try {
            board.goToSquare (posX, posY);

          } catch (Exception e) {
            if (board.state == -1) { //If the user has lost the game
              System.out.println ("You lost!");
              Main.getButtonPress ("continue");

              Main.clearScreen ();

              switch (Main.getChoice (new String [] {"1 - Return to menu", "2 - Play same board"}, "What would you like to do now?")) {
                case '1':
                  running = false;
                  break;
                case '2':
                  board.setUp ();
              }

            } else if (board.state == 1) { //If the user has won the game
              running = false;
              time = (int) ((System.currentTimeMillis () - board.startTime) / 1000);

              System.out.print ("You win!");
              Main.getButtonPress ("continue");
              Main.clearScreen ();

              System.out.print ("\nYour time is ");
              board.printTime ();
              System.out.print ("\n");

              score = board.layout.length * board.layout [0].length;
              score /= Math.log (time);
              score *= Math.log (10);
              score *= 1 + (board.lives * 0.25);

              for (int row = 0; row < board.layout.length; row++) {
                for (int column = 0; column < board.layout [0].length; column++) {
                  if (board.display [row] [column].equals ("\uD83D\uDEA9   ")) {
                    if (board.layout [row] [column] == -1) {
                      score += 3;
                    } else {
                      score -= 3;
                    }
                  }
                }
              }

              System.out.println ("Your score is " + score);

              for (int i = 0; i < 3; i++) {
                if (score > Main.highScores [i]) {
                  Main.hasHighScores++;

                  for (int j = 2; j > i; j--) {
                    Main.highScores [j] = Main.highScores [j - 1];
                  }

                  Main.highScores [i] = score;

                  System.out.println ("\nYou got a new high score! You are now at place " + (i + 1) + ".\n");

                  break;
                }
              }

              Main.printHighScores ();
            }

            return;
          }
        }
        
        return;

      case '2':
        running = false;
        pauseTime = System.currentTimeMillis ();
        return;

      case '3':
        running = false;
        board.state = -1;
        return;

      case '4':
        try {
          tile = getTile (board.grass);
          board.flags++;
          board.mineCount--;
          board.display [tile [0]] [tile [1]] = board.flag;
        } catch (Exception ignored) {}

        board.printDisplay ();
        return;

      case '5':
        try {
          tile = getTile (board.flag);
          board.mineCount++;
          board.flags--;
          board.display [tile [0]] [tile [1]] = board.grass;
        } catch (Exception ignored) {}

        board.printDisplay ();
   }
  }

  //Method getTile () : Returns the coordinates of a tile that the user wants to flag or unflag
  //Pre : The String representation of the tile to look for (in the case of flagging, only a grass tile can be flagged, in the case of unflagging, only a flagged tile can be unflagged)
  //Post : Return the coordinates of the tile in the form of an int array : {row, column}
  private int [] getTile (String check) throws Exception {
    int column, row;

    board.printDisplay (-1, board.posY);
    System.out.print (Main.saveCursor);

    outerloop:
    while (true) {
      column = Main.getNumber (1, board.layout [0].length, "column: ") - 1;

      if (column == -1) {
        throw new Exception ();
      }

      for (int i = 0; i < board.layout.length; i++) {
        if (board.display [i] [column].equals (check)) {
          break outerloop;
        }
      }

      System.out.println ("There are no valid moves on this column.");
      Main.getButtonPress ("type a new value");
      System.out.print (Main.restoreCursor + Main.clear);
    }

    board.printDisplay (column, -1);

    while (true) {
      System.out.print (Main.restoreCursor + Main.clear);

      row = Main.getNumber (1, board.layout [0].length, "row: ") - 1;

      if (row == -1) {
        throw new Exception ();
      }

      if (board.display [row] [column] == check) {
        break;
      }

      System.out.println ("Invalid tile.");
      Main.getButtonPress ("type a new value");
    }

    return new int [] {row, column};
  }

  //Method isValidMove () : Check whether a move is valid
  //Pre : The coordinates of the tile to move to
  //Post : Returns false if the tile doesn't exist, or if the tile is flagged or crossed out (from previously stepping on a mine), and true otherwise
  private boolean isValidMove (int posX, int posY) {
    try {
      if (board.display [posY] [posX].equals ("\uD83D\uDEA9   ") || board.display [posY] [posX].equals ("\u274C   ")) {
        return false;
      }
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  //Method removeArrayElement () : Removes the required element from the provided array
  //Pre : The array from which to remove, and the index of the element to be removed, such that the index exists for the array provided
  //Post : A new array is returned, which contains all the elements of the provided array in order, minus the one to be removed
  private String [] removeArrayElement (String [] array, int toRemove) {
    String removed [] = new String [array.length - 1];
    int count = 0;

    for (int i = 0; i < array.length; i++) {
      if (i == toRemove) {
        continue;
      }

      removed [count] = array [i];
      count++;
    }

    return removed;
  }
}
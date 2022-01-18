//class Board : container class for all the variables and methods pertaining to the layout, display and various states of the board and elements on the board.
class Board {
  int [] [] layout; //Represents the values on the board when they are uncovered
  String display [] []; //Holds the strings representing the actual tiles

  int mineCount;
  int lives; //Represents the number of lives collected
  int state; //Game lost: -1, Game won: 1, Game running: 0
  int posX, posY; //Represents current position on board
  int availableLives = 0; //Represents the number of lives not yet collected
  int flags = 0; //Represents the number of flags placed : to determine whether or not the user can remove a flag
  
  long startTime;

  //Ascii codes of the emojis used with enough spaces to make a 4-character string
  String mud = "\033[33m\u2592\u2592\033[0m  ";
  String grass = "\033[92m\u2592\u2592\033[0m  ";
  String mine = "\uD83D\uDCA3   ";
  String flag = "\uD83D\uDEA9   ";
  String life = "\uD83D\uDC9A   ";
  String key = "\uD83D\uDDDD   ";
  String [] emojis = {"\u2620   ", "\uD83D\uDE36   ", "\uD83D\uDE04   "};
  //Emojis: dead, neutral and happy

  //Method Board () : Constructor for this class. Initializes layout, display and minecount
  //Pre : Width, height such that 5 < width < 40, 5 < height < 20
  //Post : Creates a layout for the board with the dimensions provided, and sets the initial display and other states of the board, helped partly by method setUp ()
  public Board (int width, int height) {
    int count = 1;
    boolean keepGoing = true;
    boolean [] [] connected;

    display = new String [height] [width];
    mineCount = width * height / 5;

    while (true) {
      layout = new int [height] [width];
      layout [height - 1] [width - 1] = -3; //Sets key at bottom right tile

      for (int i = 0; i < mineCount; i++) {
        setLandmark (-1); //Sets mines at random
      }

      connected = new boolean [height] [width];
      connected [0] [0] = true;
      
      outerloop: 
      while (keepGoing) {
        keepGoing = false;

        for (int row = 0; row < layout.length; row++) {
          for (int column = 0; column < layout [0].length; column++) {
            if (connected [row] [column]) {

              for (int i = row - 1; i <= row + 1; i++) {
                for (int j = column - 1; j <= column + 1; j++) {
                  try {
                    if (layout [i] [j] != -1 && !connected [i] [j]) {
                      count++;
                      connected [i] [j] = true;
                      keepGoing = true;
                    }
                  } catch (Exception ignored) {}
                }
              }
            }
          }
        }
      }

      if (count == layout.length * layout [0].length - mineCount) {
        break;
      }
    }

    for (int row = 0; row < layout.length; row++) {
      for (int column = 0; column < layout [0].length; column++) {
        if (layout [row] [column] == 0) {
          count = 0;

          for (int i = row - 1; i <= row + 1; i++) {
            for (int j = column - 1; j <= column + 1; j++) {
              try {
                if (layout [i] [j] == -1) {
                  count++;
                }
              } catch (Exception ignored) {}
            }
          }

          layout [row] [column] = count;     
        }
      }
    }

    setUp ();
  }

  
  //Method setUp () : Sets the initial display of the board, with grass on the uncovered tiles and the emoji at starting position. Used for restarting a game with the same  layout as well as initially setting things up.
  //Pre : layout must alreay have been initialized
  //Post : Many variables set (or reset), and the display set to initial conformation and emoji placed on square (0, 0)
  public void setUp () {
    //Resets:
    flags = 0;
    lives = 0;
    posX = 0;
    posY = 0;
    mineCount = layout.length * layout [0].length / 5;
    state = 0;

    //Sets/resets lives
    for (; availableLives < (layout [0].length * layout.length / 80); availableLives++) {
      setLandmark (-2); //lives
    }

    for (int row = 0; row < layout.length; row++) {
      for (int column = 0; column < layout [0].length; column++) {
        display [row] [column] = grass;
      }
    }

    display [layout.length - 1] [layout [0].length - 1] = key;

    startTime = System.currentTimeMillis(); //Sets/resets start time

    try {
      goToSquare (0, 0);
    } catch (Exception ignored) {}
  }

  //Method gotoSquare () : Handles the movement of the user to a new tile on the board, and takes various actions based on the tile the user uncovers
  //Pre : The coordinates of the user's new position, such that such a position exists on the board
  //Post : Display is manipulated and printed to the board to simulate the movement of the emoji to a new tile. An exception is thrown if the user's move causes them to win or lose, communicating to class Board that the game has ended. Further action is taken based on the specific tile the user uncovers: a zero tile, a life a mine, etc.
  public void goToSquare (int posX, int posY) throws Exception {
    String fire = "\uD83D\uDD25   ", sound = "\007";
    boolean keepGoing = true;

    display [this.posY] [this.posX] = getDisplay (layout [this.posY] [this.posX]);

    this.posX = posX;
    this.posY = posY;

    display [this.posY] [this.posX] = emojis [(int) Math.signum (lives) + 1];

    printDisplay ();
    System.out.printf (sound);
    delay (750);

    if (layout [posY] [posX] == -1) {
      lives--;

      display [posY] [posX] = mine;
      printDisplay ();
      delay (750);

      display [this.posY] [this.posX] = fire;
      printDisplay ();
      System.out.printf (sound); //Makes a sound
      delay (750);

      display [posY] [posX] = emojis [(int) Math.signum (lives) + 1];
      printDisplay ();

      if (lives > -1) {
        System.out.println ("You lost a life.\n");

      } else {
        state = -1;
        throw new Exception ();
      }
    } else if (layout [posY] [posX] == -3) {
      state = 1;
      throw new Exception ();

    } else if (layout [posY] [posX] == -2) {
      layout [this.posY] [this.posX] = 0;
      lives++;
      availableLives--;

      display [this.posY] [this.posX] = life;
      printDisplay ();
      delay (1000);

      display [this.posY] [this.posX] = emojis [(int) Math.signum (lives) + 1];
      printDisplay ();

    } else if (layout [this.posY] [this.posX] == 0) {
      while (keepGoing) {
        keepGoing = false;

        for (int row = 0; row < layout.length; row++) {
          for (int column = 0; column < layout [0].length; column++) {
            if (display [row] [column].equals (mud) || display [row] [column].equals (life) || display [row] [column].equals (emojis [(int) Math.signum (lives) + 1])) {

              for (int i = row - 1; i <= row + 1; i++) {
                for (int j = column - 1; j <= column + 1; j ++) {
                  try {
                    if (display [i] [j].equals (grass)) {
                      display [i] [j] = getDisplay (layout [i] [j]);
                      keepGoing = true;
                    }
                  } catch (Exception ignored) {} 
                }
              }
            }
          }
        }
      }
      printDisplay ();
    }
  }

  //Method setLandmark () : Sets landmarks (mines/lives) on the board at random
  //Pre : The coded int value of the landmark (-1 for bombs, -2 for lives) and the presence of at least one zero tile on which to place the landmark
  //Post : Layout contains a value coding for a landmark at a random location in the array
  private void setLandmark (int value) {
    int row, column;

    while (true) {
      row = (int) (Math.random () * layout.length);
      column = (int) (Math.random () * layout [0].length);

      if (layout [row] [column] == 0 && !((row == 0 || row == 1) && (column == 0 || column == 1))) {
        layout [row] [column] = value;
        return;
      }
    }      
  }
  
  //Method printDisplay () : Prints the display to the screen, including the top info bar and row/column numbers as necessary
  //Pre : Index of column and row to print numbers for (-1 if unnecessary)
  //Post : The top info bar, the board and a message to resize the browser as necesary is printed to the screen.
  public void printDisplay (int columnNum, int rowNum) {
    String clock =  "\u23F0";
    
    Main.clearScreen ();

    System.out.println (Main.green + "Resize the screen if the display is wonky.\n" + Main.reset);

    //Top info bar : time, minecount, etc.
    System.out.print (life + lives + "  Available: " + availableLives + "  |  ");
    System.out.print (mine + mineCount + "  |  ");
    System.out.printf ("You are standing on: " + getDisplay (layout [this.posY] [this.posX]) + "|  ");
    System.out.print (clock + "   ");
    printTime ();

    System.out.print ("\n\n\n");
  
    for (int row = 0; row < layout.length; row++) {
      if (row == rowNum) {
        for (int column = 0; column < layout [0].length; column++) {
          System.out.print (Main.green + formatNumber (column + 1, 4) + Main.reset);
        }

        System.out.print ("\n\n");
      }

      for (int column = 0; column < layout [0].length; column++) {
        if (column == columnNum) {
          System.out.print (Main.green + formatNumber (row + 1, 3) + Main.reset);
        }

        System.out.print (display [row] [column]);
      }

      System.out.print ("\n\n");
    }
  }

  //Method printDisplay () : overloaded method. Passes -1, -1 as parameters to printDisplay (row, column), signifying that row and column numbers are unnecessary
  public void printDisplay () {
    printDisplay (-1, -1);
  }

  //Method delay () : Delays the program as necessary
  //Pre : Time to delay in milliseconds > 0
  //Post : Delays specified time and adds that time to the start time, so that it isn't counted in the final score calculation
  public void delay (int delay) {
    try {
      Thread.sleep (delay);
      startTime = startTime + 750;
    } catch (Exception ignored) {}
  }

  
  //Method printTime () : Prints the time since starting in a format that is useful to the user
  //Pre : startTime has been initialized and the cursor is located where the time should be printed
  //Post : The time is printed to the screen in the format Hours:Minutes:Seconds
  public void printTime () {
    int time = (int) ((System.currentTimeMillis () - startTime) / 1000);
    String formatted = ":";

    if (time % 60 < 10) {
      formatted += "0";
    }

    formatted += (time % 60);
    time = time / 60;

    formatted = (time % 60) + formatted;

    if (time % 60 < 10) {
      formatted = "0" + formatted;
    }

    formatted = ":" + formatted;

    time = time / 60;
    formatted = time + formatted;

    if (time < 10) {
      formatted = "0" + formatted;
    }

    System.out.print (formatted);
  }

  //Method getDisplay () : Return the string representation of an uncovered tile based on the corresponding layout value
  //Pre : The value stored in the corresponding square in the array layout [] []
  //Post : Returns the corresponding emoji or the number formatted with the appropriate number of spaces
  private String getDisplay (int tileValue) {
    String cross = "\u274C   ";
    
    if (tileValue == -1) {
      return cross;
    } else if (tileValue == -2) {
      return life;
    } else if (tileValue == 0) {
      return mud;
    } else if (tileValue == -3) {
      return key;
    } else {
      return formatNumber (tileValue, 4);
    }
  }

  //Method formatNumber () : Used to format a number as a String with a certain number of characters
  //Pre : The number to format and the length of the final String
  //Post : Returns the number as a String with the desired length
  private static String formatNumber (int num, int length) {
    String formatted = "" + num;

    for (int i = 0; i < length; i++) {
      formatted += " ";
    }

    return formatted.substring (0, length);
  }
}
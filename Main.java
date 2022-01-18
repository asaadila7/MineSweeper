/*
Name: Aadila Ali Sabry

Date: 25/05/2021

Description: This is an adventure version of minesweeper. Make your way across the board to the key, and don't trip the mines. The numbers will help you figure out which squares are safe to move to.

A more detailed explanation of the game is provided in the "MineSweeperRules" text file, which can be accessed from the menu.

Additional features are the timer, the coloured text, the menu and the emojis used throughout the program.
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

//class Main : container class for the main method, and several miscellaneous methods,including all the methods in this program that interact with the user directly.
class Main {
  static Game game;
  static Scanner scanner = new Scanner (System.in);
  static double [] highScores = new double [3];
  static int hasHighScores = 0;

  static String green = "\033[92m";
  static String reset = "\033[0m";
  static String saveCursor = "\033[s";
  static String restoreCursor = "\033[u";
  static String clear = "\033[J";
  
  //Method main () : Loops through the main menu until the user chooses to quit
  //Pre : N/A
  //Post : Implements action based on user's choice repeatedly: one of 1) New Game, 2) Resume Game, 3) View High Scores, 4) How to Play and 5) Quit Game
  public static void main(String[] args) {
    FileReader fReader;
    BufferedReader bReader;
    String line;

    while (true) {
      clearScreen ();

      outerloop:
      switch (getChoice (new String [] {"1 - New Game", "2 - Resume Game", "3 - View High Scores", "4 - How to Play", "5 - Quit Game"},  "**************************************\n" + green + "WELCOME TO MINESWEEPER ADVENTURE MODE!" + reset + "\n**************************************")) {

        case '1':
          game = new Game ();
          break;

        case '2':
          try {
            game.resume ();
          } catch (Exception e) {
            System.out.print ("\nYou don't have a game saved.");
            getButtonPress ("return to menu");
          }
          break;
         
        case '3':
          clearScreen ();
          printHighScores ();
          break;

        case '4':
            try {
              fReader = new FileReader ("MineSweeperRules.txt");
            } catch (Exception e) {
              System.out.println ("Could not find file.");
              break outerloop;
            }

            bReader = new BufferedReader (fReader);

            clearScreen ();

            loop:
            while (true) {
              try {
                line = bReader.readLine ();

                if (line == null) {
                  getButtonPress ("return to menu");
                  break loop;
                }

                System.out.println (line);
              } catch (Exception e) {
                break;
              }
            }

          break;

        case '5':
          scanner.close ();
          return;
      }

      try {
        while (game.running) {
          game.run ();
        }

        if (game.board.state != 0) {
          game = null;
        }

      } catch (Exception ignored) {}
    }
  }

  //Method printHighScores () : Prints the stored high score values
  //Pre : N/A
  //Post : High scores are printed to screen. The program then waits for the user to hit enter to continue.
  public static void printHighScores () {
    String [] ordinals = {"First", "Second", "Third"};

    if (hasHighScores == 0) {
      System.out.println ("\nYou haven't won any games yet!");
      getButtonPress ("return to menu");
      return;
    }

    System.out.println (green + "High Scores:" + reset);

    for (int i = 0; i < hasHighScores; i++) {
      System.out.println ("\n\t" + green + ordinals [i] + ": " + reset + highScores [i]);
    }

    getButtonPress ("return to menu");
  }

  //Method getNumber () : Used for getting user input in the form of a number within a range
  //Pre : maximum and minimum values (max > min, validation not done in the method) and a prompt
  //Post : a number within the range, including the bounds, or -1 if the user decides they no longer want to enter a number and instead wants to return to the previous menu
  public static int getNumber (int min, int max, String prompt) {
    int input;

    System.out.print (saveCursor); //save cursor position

    while (true) {
      System.out.print (restoreCursor + clear); //clears text from previous attempts to get input

      System.out.print ("Type -1 to cancel or enter " + prompt);

      try {
        input = (int) Double.parseDouble (scanner.nextLine ()); //can cause error if input is not a number
        //why double instead of int? so that if they enter a double, i can round it down and use it, instead of discarding

        if ((input >= min && input <= max) || input == -1) { //valid input
          System.out.print (restoreCursor + clear); //clears text before returning
          return input;

        } else {
          System.out.print ("\nNumber must be greater than or equal to " + min + " and less than or equal to " + max + ".");
          //error message for out of bounds value
        }

      } catch (Exception e) { //error message for input that isnt a number
        System.out.print ("\nThat wasn't a number!");
      }

      getButtonPress ("enter a different value"); //waits for a button press from the user before continuing prorgram
    }
  }

  //Method getChoice () : Prompts the user for a (valid) choice. The user may type out the option or just the character
  //Pre : a string array of the options, and a prompt for the user
  //Post : returns a character corresponding to one of the options passed as a parameter
  public static char getChoice (String [] options, String title) {    
    String choice;

    System.out.print (saveCursor); //save cursor position

    outerloop:
    while (true) {

      System.out.print (restoreCursor + clear);
      
      System.out.println (title + "\n");

      for (int i = 0; i < options.length; i++) {
        System.out.println ("\t" + options [i]);
      }

      System.out.print ("\nChoice: ");
      choice = scanner.nextLine ().toLowerCase ();
              
      for (int i = 0; i < options.length; i++) {
        if ((choice.length () == 1 && choice.charAt (0) == options [i].toLowerCase ().charAt (0)) || choice.contains (options [i].toLowerCase ().substring (4))) {
          choice = options [i].toLowerCase ();
          break outerloop;
        }
      }

      System.out.print ("\nThat wasn't a choice!");
      getButtonPress ("enter a different value");
    }

    System.out.print (restoreCursor + clear);
    return choice.charAt (0);
  }
  
  //Method getButtonPress () : Waits for the user to press enter before allowing the program to continue
  //Pre : String reason (the reason for the user to press enter)
  //Post : A prompt is printed to the screen and the program execution is paused until the user presses enter
  public static void getButtonPress (String reason) {
    System.out.print ("\n\nPress enter to " + reason + ": ");
    scanner.nextLine ();
  }

  //Method clearScreen () : Clears the screen
  //Pre : N/A
  //Post : Screen (along with scrollback buffer) is cleared.
  public static void clearScreen () {
    System.out.print("\033[H\033[2J");  
    System.out.flush ();
    System.out.print("\033[3J");
  }
}
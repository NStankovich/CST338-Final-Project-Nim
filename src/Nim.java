/******************************************************************************* 
Name: Nicholas Stankovichm
Group: Group 2 - InnovaTree
Assignment Name: 
(F)Complete and upload the Final Project (10 hrs)
Due Date: Dec 18, 2020

Description:
This program is a fully functional game of Multi-Pile Nim. The game is designed 
to be nearly impossible. Only if the player makes a perfect sequence of choices 
will they find victory.
*******************************************************************************/
import java.util.*;
import java.util.stream.IntStream;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.lang.*;


public class Nim
{
   static final int NUM_PILES = 4;
   static final int MAX_GEMS = 10;
   
   public static void main(String[] args)
   {
      NimController myController = new NimController(NUM_PILES,MAX_GEMS);
   }
}

/**
 * Controller for the Nim game. It works between the model and the view and 
 * handles all the game logic.
 * @author Nicholas Stankovich
 *
 */
class NimController implements ActionListener
{
   private ActionListener buttonEar;
   private NimModel myModel;
   private NimView myView;
   
   private int currentPile; //makes sure player can only take from one pile
   private boolean playerTurn, anyOdd, turnHasStarted;
   private boolean[] isOdd; //used for the wizard's strategy
   /**
    * Constructor that creates the table and starts the game
    * @param numPiles number of piles of gems
    * @param numGems maximum number of gems per pile
    */
   NimController(int numPiles, int numGems)
   {
      buttonEar = this;
      myModel = new NimModel(numPiles,numGems);
      myView = new NimView(this,numPiles,numGems);
      isOdd = new boolean[myModel.getBinarySize()];
      anyOdd=false;
      if (!myView.describeGame())
      {
         System.exit(0);
      }
      else
      {
         Thread timer = new UpdateTimerLabel();
         timer.start();
         newGame();
      }
   }
   /**
    * Listener for all the button interactions.
    */
   @Override
   public void actionPerformed(ActionEvent e)
   {
      if (e.getActionCommand()=="Timer")
      {
         myModel.resetTimer();
      }
      else if (e.getActionCommand()=="End")
      {
         if(myModel.getTotalGems()>0)//Checks if game should be over
         {
            if(playerTurn&&turnHasStarted)
            {
               playerTurn=false;
               myView.updateTurn(playerTurn);
               wizardTakesTurn();
            }
         }
         else
         {
            //Game is over, player wins
            if (myView.youWin())
            {
               newGame();
            }
            else
            {
               myView.thankYou(); 
            }
         }
      }
      else
      {
         int numCommand = Integer.parseInt(e.getActionCommand());
         if (playerTurn)
         {
            if(!turnHasStarted&&myModel.getPile(numCommand)>0)
            {
               turnHasStarted=true;
               currentPile = numCommand;
               takeGem(numCommand);
            }
            else if (turnHasStarted&&numCommand==currentPile
                  &&myModel.getPile(numCommand)>0)
            {
               takeGem(numCommand);
            }
         }
      }
      
   }
   /**
    * Sets up for a new game. Generates a random number of gems (between 4 and 
    * 10) for each pile. Displays the new gems. Asks if the player wants to go 
    * first. If so, does nothing, if not, the wizard plays.
    */
   private void newGame()
   {
      Random rand = new Random();
      for (int i = 0; i<myModel.getNumPiles(); i++)
      {
         myModel.setPile(i, rand.nextInt(myModel.getMaxGems()-3)+4);
      }
      for (int j = 1; j<=myModel.getPile(myModel.getBiggest());j++)
      {
         for (int i = 0; i<myModel.getNumPiles(); i++)
         {
            if (j<=myModel.getPile(i))
            {
               try
               {
                  Thread.sleep(100);
               }
               catch (InterruptedException e)
               {
                  System.out.print("Error: Interrupted 2");
               }
               myView.updatePile(i, j);
            }
         }
      }
      turnHasStarted=false;
      currentPile=-1;
      playerTurn = myView.whoFirst();
      myView.updateTurn(playerTurn);
      if (!playerTurn)
      {
         wizardTakesTurn();
      }
   }
   /**
    * Takes a gem from the given pile in both the model and the view.
    * @param index Index of pile and button to be updated
    */
   private void takeGem(int index)
   {
      myModel.setPile(index, myModel.getPile(index)-1);
      myView.updatePile(index, myModel.getPile(index));
   }
   /**
    * Executes the wizard's play. Uses the mathematic principles described by 
    * Charles Bouton, who also gave the game its modern name, to determine the 
    * optimal move. Unless the player plays a perfect game, including choosing 
    * who goes first, the wizard will win.
    */
   private void wizardTakesTurn()
   {
      turnHasStarted=false;
      currentPile=-1;
      anyOdd=false;
      for (int i=0; i<isOdd.length; i++)
      {
         isOdd[i]=((myModel.getTotal(i)%2)==1);
         if(isOdd[i])
         {
            anyOdd=true;
         }
      }
      if (anyOdd)
      {
         int firstColumn;
         findColLoop: for (firstColumn = isOdd.length-1; firstColumn>=0;
               firstColumn--)
         {
            if (isOdd[firstColumn])
            {
               break findColLoop;
            }
         }
         int pile;
         findPileLoop: for (pile = 0; pile < myModel.getNumPiles();pile++)
         {
            if (myModel.getBinary(pile, firstColumn)==1)
            {
               break findPileLoop;
            }
         }
         int gemsToLeave = 0;
         for (int i = 0; i<isOdd.length; i++)
         {
            if ((isOdd[i]&&myModel.getBinary(pile, i)==0)
                  ||(!isOdd[i]&&myModel.getBinary(pile, i)==1))
            {
               gemsToLeave+=Math.pow(2, i);
            }
            
         }
         int gemsToTake = myModel.getPile(pile) - gemsToLeave;
         for (int i=1; i<=gemsToTake;i++)
         {
            try
            {
               Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
               System.out.print("Error: Interrupted 3");
            }
            takeGem(pile);
         }
      }
      else
      {
         takeGem(myModel.getBiggest());
      }
      if (myModel.getTotalGems()>0)//Checks if game should be over
      {
         playerTurn=true;
         myView.updateTurn(playerTurn);
      }
      else
      {
         if (myView.iWin())//Game over, wizard wins
         {
            newGame();
         }
         else
         {
            myView.thankYou();
         }
      }
   }
   /**
    * Updates the timer label every second. Runs in its own thread.
    * @author Nicholas Stankovich
    *
    */
   private class UpdateTimerLabel extends Thread
   {
      /**
       * Runs a never ending loop that updates the timer button with current
       * run time.
       */
      public void run()
      {
         while(true)
         {
            myView.updateTimer(myModel.getTimerLabel());
            try
            {
               Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
               System.out.print("Error: Interrupted 4");
            }
         }
      }

   }
}

/**
 * The model that holds the information for the Nim game. It holds all the 
 * values for the piles, as well as the binary array that the wizard uses 
 * to determine the optimal move.
 * @author Nicholas Stankovich
 *
 */
class NimModel
{
   private int piles[];
   private int binaryArray[][];
   private int columnTotals[];
   private ImageIcon pileIcons[];
   private int maxGems, totalGems, timerCounter, biggestPileIndex, binarySize;
   private Timer gameTimer;
   
   /**
    * Constructor for the model. Creates and starts the timer, then initializes
    * other members as needed.
    * @param numPiles Number of piles
    * @param numGems Maximum gems per pile
    */
   NimModel(int numPiles, int numGems)
   {
      gameTimer = new Timer();
      gameTimer.start();
      timerCounter=totalGems=biggestPileIndex=binarySize=0;
      maxGems = numGems;
      piles = new int[numPiles];
      binaryArray = new int[numPiles][(int)Math.floor(
            Math.log(numGems)/Math.log(2))+1];
      columnTotals = new int [(int)Math.floor(Math.log(numGems)/Math.log(2))+1];
      pileIcons = new ImageIcon[numGems+1];
      binarySize = (int)Math.floor(Math.log(numGems)/Math.log(2))+1;
      for (int i = 0; i < piles.length; i++)
      {
         piles[i]=0;
      }
      calcBinary();
   }
   /**
    * Mutator for the value in a pile. Also automatically adjusts binaryArray.
    * @param pile Index of pile to be updated
    * @param num Number of gems that should be in that pile
    * @return returns true if successful
    */
   public boolean setPile(int pile, int num)
   {
      if (pile<piles.length&&num<=maxGems)
      {
         piles[pile]=num;
         calcBinary();
         totalGems = IntStream.of(piles).sum();
         biggestPileIndex=findBiggest();
         return true;
      }
      else
      {
         System.out.print("Error: Invalid gem value");
         return false;
      }
      
   }
   /**
    * Accessor for value in a pile
    * @param pile Index of desired pile
    * @return Number of gems in the pile
    */
   public int getPile(int pile)
   {
      return piles[pile];
   }
   /**
    * Private helper to fill out the binary array according to the values in 
    * the piles.
    */
   private void calcBinary()
   {
      
      for (int i = 0; i < columnTotals.length; i++)
      {
         columnTotals[i]=0;
      }
      for (int i = 0; i < binaryArray.length; i++)
      {
         for (int j = 0; j < binaryArray[i].length; j++)
         {
            binaryArray[i][j]=0;
         }
      }
      
      for (int i = 0; i < piles.length; i++)
      {
         String binary = Integer.toBinaryString(piles[i]);
         for (int j = binary.length()-1; j >= 0; j--)
         {
            binaryArray[i][j] = Character.getNumericValue(
                  binary.charAt(binary.length()-1-j));
            columnTotals[j]+=binaryArray[i][j];
         }
      }
      /*for (int i = 0; i < binaryArray.length; i++) //For debugging purposes
      {
         for (int j = 0; j < binaryArray[i].length; j++)
         {
            System.out.print(binaryArray[i][j]);
            
         }
         System.out.print('\n');
      }
      System.out.print('\n');*/
   }
   /**
    * Private helper to find the pile with the most gems
    * @return Index of biggest pile
    */
   private int findBiggest()
   {
      int result = 0;
      for (int i = 1; i<piles.length; i++)
      {
         if (piles[i]>piles[result])
            result=i;
      }
      return result;
   }
   /**
    * Accessor for values in the binary array
    * @param pile Desired pile
    * @param index Which digit in the binary number
    * @return Value of digit
    */
   public int getBinary(int pile, int index)
   {
      return binaryArray[pile][index];
   }
   /**
    * Accessor for the column totals
    * @param index Which digit total
    * @return Value of column total
    */
   public int getTotal(int index)
   {
      return columnTotals[index];
   }
   /**
    * Accessor for the index of the biggest pile
    * @return Index of biggest pile
    */
   public int getBiggest()
   {
      return biggestPileIndex;
   }
   /**
    * Accessor for the number of piles
    * @return Length of pile array
    */
   public int getNumPiles()
   {
      return piles.length;
   }
   /**
    * Accesor for the maximum number of gems per pile
    * @return Value of maxGems
    */
   public int getMaxGems()
   {
      return maxGems;
   }
   /**
    * Accessor for size of the binary numbers
    * @return Number of digits in binary numbers
    */
   public int getBinarySize()
   {
      return binarySize;
   }
   /**
    * Accessor for total number of gems in play
    * @return Value of totalGems
    */
   public int getTotalGems()
   {
      return totalGems;
   }
   /**
    * Accessor for the gem icons
    * @param Index Number of gems in the pile
    * @return Icon with that many gems
    */
   public ImageIcon getIcon(int index)
   {
      if (index<pileIcons.length)
      {
         return pileIcons[index];
      }
      else
      {
         return new ImageIcon("GemIcons/Error.png");
      }
   }
   /**
    * Mutator to set the icons in the icon array
    * @param Icon Icon to be placed into the array
    * @param Index Number of gems in that icon
    * @return True if successful
    */
   public boolean setIcon(ImageIcon icon, int index)
   {
      if (index<pileIcons.length)
      {
         pileIcons[index]=icon;
         return true;
      }
      else
      {
         return false;
      }
   }
   /**
    * Accessor for the number of seconds in the timer.
    * @return The number of seconds that have elapsed
    */
   public int getTimerCounter()
   {
      return gameTimer.getSeconds();
   }
   
   /**
    * If the timer is running, it stops it. If its stopped, it runs it.
    */
   @SuppressWarnings("deprecation")
   public void resetTimer()
   {
      if(gameTimer.getTimerOn())
      {
         timerCounter = gameTimer.getSeconds();
         gameTimer.setTimerOn(false);
         gameTimer.stop();
      }
      else
      {
         gameTimer = new Timer(timerCounter);
         gameTimer.start(); 
      }
   }
   /**
    * Accessor for timer label
    * @return String with properly formatted timer label
    */
   public String getTimerLabel()
   {
      return gameTimer.getTimerLabel();
   }
}
/**
 * View class for the Nim game. It handles all of the GUI elements.
 * @author Nicholas Stankovich
 *
 */
class NimView extends JFrame
{
   private JButton[] pileButtons;
   private JButton endTurn, timerButton;
   private Icon[] gemIcons;
   private ImageIcon wizardIcon;
   private JLabel turnLabel;
   
   private JPanel pnlTimer, pnlPiles, pnlEnd, pnlTurn;
   /**
    * Constructor that creates the table and sets up everything.
    * @param buttonEar Listener for the buttons on the interface
    * @param numPiles Number of piles
    * @param numGems Max number of gems per pile
    */
   NimView(ActionListener buttonEar, int numPiles, int numGems)
   {
      this.setTitle("Nim - The (Nearly) Impossible Game");
      pileButtons = new JButton[numPiles];
      for (int i = 0; i<pileButtons.length; i++)
      {
         pileButtons[i] = new JButton();//Initializes the buttons
      }
      gemIcons = new ImageIcon[numGems+1];
      wizardIcon = new ImageIcon("GemIcons/Wizard.png");
      String file;
      for (int i=0; i<gemIcons.length;i++)
      {
         file = "GemIcons/" + String.format("%02d", i) + ".png";
         gemIcons[i] = new ImageIcon(file);//Initializes the icons
      }
      setupTable(buttonEar, numPiles);
   }
   /**
    * Sets up the table with all the needed visual elements
    * @param buttonEar Listener for all the buttons
    * @param numPiles Number of piles
    */
   private void setupTable(ActionListener buttonEar, int numPiles)
   {
      
      setSize(810,685);
      setLocationRelativeTo(null);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setVisible(true);
      setLayout(new BorderLayout());
      
      pnlTimer = new JPanel();
      pnlPiles = new JPanel();
      pnlEnd = new JPanel();
      pnlTurn = new JPanel();
      turnLabel = new JLabel("Your Turn");
      
      pnlTimer.setLayout(new FlowLayout());
      pnlPiles.setLayout(new GridLayout(2,2));
      pnlEnd.setLayout(new FlowLayout());
      pnlTurn.setLayout(new FlowLayout());
      
      pnlTimer.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),"Timer"));
      pnlPiles.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),"Choose Carefully"));
      pnlEnd.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),"Are You Done?"));
      
      add(pnlTimer,BorderLayout.WEST);
      add(pnlPiles,BorderLayout.CENTER);
      add(pnlEnd,BorderLayout.EAST);
      add(pnlTurn, BorderLayout.NORTH);
      for(int i=0; i<pileButtons.length;i++)
      {
         pileButtons[i].setActionCommand(Integer.toString(i));
         pileButtons[i].setIcon(gemIcons[0]);
         pileButtons[i].addActionListener(buttonEar);
         pnlPiles.add(pileButtons[i]);
      }
      timerButton = new JButton("0:00");
      timerButton.addActionListener(buttonEar);
      timerButton.setActionCommand("Timer");
      pnlTimer.add(timerButton);
      endTurn = new JButton("End Turn");
      endTurn.addActionListener(buttonEar);
      endTurn.setActionCommand("End");
      pnlEnd.add(endTurn);
      pnlTurn.add(turnLabel);
      revalidate();
      repaint();
   }
   /**
    * Changes the text displayed on the timer button
    * @param label String to be displayed on the button
    */
   public void updateTimer(String label)
   {
      timerButton.setText(label);
      pnlTimer.revalidate();
      pnlTimer.repaint();
   }
   /**
    * Updates a pile button with an icon containing a desired number of gems.
    * @param pileIndex Index of the button to be updated
    * @param numGems Number of gems that pile should contain
    */
   public void updatePile(int pileIndex, int numGems)
   {
      pileButtons[pileIndex].setIcon(gemIcons[numGems]);
      pnlPiles.revalidate();
      pnlPiles.repaint();
   }
   /**
    * Updates the text at the top indicating whose turn it is
    * @param turn Boolean indicating turn, true for player, false for wizard.
    */
   public void updateTurn (boolean turn)
   {
      if (turn)
      {
         turnLabel.setText("Your Turn");
      }
      else
      {
         turnLabel.setText("My Turn");
      }
   }
   /**
    * Dispalys dialog box introducing the game and asking the user if they 
    * want to play.
    * @return Boolean value indicated selected choice, true for yes, 
    * false for no
    */
   public boolean describeGame()
   {
      String message = "<html><body><p style='width: 250px;'>" + 
            "Welcome, traveller! I am the great wizard Nim! Shall I"
            + " interest you in a game? The rules are deceptively simple. " +
            "I will place a random number of gems in " + pileButtons.length +
            " bowls. On each of our turns, we will take anywhere between one " +
            "and all of the gems from a single bowl. Once you have taken a gem"+
            " from a bowl, you can't take any from any other bowl until your " +
            "next turn. Whoever takes the final gem wins. But be warned. I am "+
            "an expert at this game, and if you make a single mistake, even " +
            "when deciding who goes first, it will be impossible to beat me! " +
            "Do you dare?" + "</p></body></html>";
      int input = JOptionPane.showConfirmDialog(null,message, "Meet the Wizard",
            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
            wizardIcon);
      if (input==JOptionPane.YES_OPTION)
      {
         return true;
      }
      else
      {
         return false;
      }
      
   }
   /**
    * Displays dialog box asking the user if they want to go first.
    * @return Boolean value indicating user choice.
    */
   public boolean whoFirst()
   {
      String message = "Would you care to go first?";
      int input = JOptionPane.showConfirmDialog(null,message,"Choose Carefully",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,wizardIcon);
      if (input==JOptionPane.YES_OPTION)
      {
         return true;
      }
      else
      {
         return false;
      }
   }
   /**
    * Displays dialog box stating that the wizard has won and asking if the 
    * user wants to play again.
    * @return Boolean value indicating choice.
    */
   public boolean iWin()
   {
      String message = "<html><body><p style='width: 250px;'>" + 
            "It appears I have won. Hardly a surprise, really. " +
            "Care to play again?" + "</p></body></html>";
      int input = JOptionPane.showConfirmDialog(null,message,"Sorry...",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,wizardIcon);
      if (input==JOptionPane.YES_OPTION)
      {
         return true;
      }
      else
      {
         return false;
      }
   }
   /**
    * Displays dialog box stating the player has won and asks if they want to 
    * play again.
    * @return Boolean value indicating response
    */
   public boolean youWin()
   {
      String message = "<html><body><p style='width: 250px;'>" + 
            "What's this? You've won! Dear me, this is a surprise! "+
            "Care to play again?" + "</p></body></html>";
      int input = JOptionPane.showConfirmDialog(null,message,"Congratulations!",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,wizardIcon);
      if (input==JOptionPane.YES_OPTION)
      {
         return true;
      }
      else
      {
         return false;
      }
   }
   /**
    * Displays thank you message and ends the game.
    */
   public void thankYou()
   {
      String message = "Thank you for playing! Come again soon!";
      JOptionPane.showMessageDialog(null, message, "Game Over!", 
            JOptionPane.INFORMATION_MESSAGE, wizardIcon);
      System.exit(0);
   }
}
/**
 * Timer that contains a second counter that iterates once each second. Also 
 * has boolean that indicates if the timer is currently running and a label 
 * containing current time display or "paused" message.
 * @author Nicholas Stankovich
 *
 */
class Timer extends Thread
{
   private int seconds;
   private boolean timerOn;
   private String timerLabel;

   /**
    * Constructor that initializes time to 0 seconds and turns on timer
    */
   Timer()
   {
      super();
      seconds = 0;
      timerOn = true;
      int m = (int)seconds/60;
      int s = seconds % 60;
      timerLabel = String.format("%d:%02d", m, s);
   }

   /**
    * Constructor that starts timer at a specified time
    * @param Number of seconds at which timer starts
    */
   Timer(int sec)
   {
      super();
      seconds = sec;
      timerOn = true;
      int m = (int)seconds/60;
      int s = seconds % 60;
      timerLabel = String.format("%d:%02d", m, s);
   }

   /**
    * Starts the timer if it's stopped and vice versa
    * @return The current time in seconds
    */
   public int resetTimer()
   {
      if (!timerOn)
      {
         timerOn = true;
         start();
      }
      else if(timerOn)
      {
         timerOn = false;
      }

      return seconds;
   }

   /**
    * Creates a label indicating current time in mm:ss format, or "*PAUSED*" 
    * if timer is stopped.
    * @return String that has been created
    */
   public String getTimerLabel()
   {
      if (timerOn)
      {
         int min = (int)seconds/60;
         int sec = seconds % 60;
         timerLabel = String.format("%d:%02d", min, sec);
         return timerLabel;
      }
      else
      {
         timerLabel = "*PAUSED*";
         return timerLabel;
      }
   }

   /**
    * Accessor for current on/off state of timer
    * @return Value of timerOn
    */
   public boolean getTimerOn()
   {
      return timerOn;
   }

   /**
    * Accessor for current number of seconds
    * @return Value of seconds
    */
   public int getSeconds()
   {
      return seconds;
   }

   /**
    * Mutator for timerOn value
    * @param Desired boolean state for timerOn
    */
   public void setTimerOn(boolean state)
   {
      timerOn = state;
   }

   /**
    * The thread that iterates seconds by one each second if timerOn is true
    */
   public void run()
   {
      while(getTimerOn())
      {
         try
         {
            Thread.sleep(1000);
            seconds++;
         }
         catch (InterruptedException e)
         {
            System.out.print("Error: Interrupted Exception");
            System.exit(1);
         }
      }
   }
}
/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import java.util.Arrays;

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	
	//public static void main(String[] args) {
		//new Yahtzee().start(args);
	//}
	
	
	public void run() {	
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		//System.out.println("nPlayers is " + Integer.toString(nPlayers));
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);	
		playGame();
	}

	private void playGame() {
		scorecard = new int[N_CATEGORIES][nPlayers];
		//System.out.println(Arrays.toString(scorecard));
		for (int i = 0; i < N_CATEGORIES; i++) {
			for (int j = 0; j < nPlayers; j++) {
				scorecard[i][j] = -1;
				//System.out.println(scorecard[i][j]);
			}
		}for (int i = 0; i < N_SCORING_CATEGORIES; i++) {
			for (int player = 1; player <= nPlayers; player++) {
				playRound(player);
			}
		}for (int player = 1; player <= nPlayers; player++) {
				finaliseScorecard(player);
		}congratulateWinner();
	}
	
	private void finaliseScorecard(int player) {
		int upperscore = calculateScore(player, ONES, SIXES);
		scorecard[UPPER_SCORE -1][player-1] = upperscore;
		display.updateScorecard(UPPER_SCORE, player, upperscore);
		if (upperscore > 63) {
			scorecard[UPPER_BONUS -1][player-1] = 35;
		}else {
			scorecard[UPPER_BONUS -1][player-1] = 0;
		}display.updateScorecard(UPPER_BONUS, player, 35);
		int lowerscore = calculateScore(player, THREE_OF_A_KIND, CHANCE);
		scorecard[LOWER_SCORE -1][player-1] = lowerscore;
		display.updateScorecard(LOWER_SCORE, player, lowerscore);
		int total = scorecard[UPPER_SCORE -1][player-1]+
					scorecard[UPPER_BONUS -1][player-1]+
					scorecard[LOWER_SCORE -1][player-1];
		scorecard[TOTAL-1][player-1] = total;
		display.updateScorecard(TOTAL, player, total);
	}
	
	
	private void congratulateWinner() {
		String winningPlayer = "";
		int winningScore = 0;
		for (int player = 0; player < nPlayers; player++) {
			if (scorecard[TOTAL -1][player]>winningScore) {
				winningScore = scorecard[TOTAL -1][player];
				winningPlayer = playerNames[player];
			}
		}display.printMessage("Congratulations "+ winningPlayer + 
								", you won with a total of " + Integer.toString(winningScore) + "points!");
	}
	
	private int calculateScore(int player, int lower_cat, int upper_cat) {
		int score = 0;
		for (int i = lower_cat-1; i < upper_cat; i++) {
			score += scorecard[i][player-1];		
		}return score;
	}	
	
	private void playRound(int player) {	
		display.printMessage(playerNames[player-1] + "'s turn. Click 'Roll dice' to roll the dice.");
		display.waitForPlayerToClickRoll(player);
		for (int i = 0; i < N_DICE; i++) {
			rollDice(i);
		} for (int i = 0; i < 2; i++) {
			display.displayDice(dice);
			display.printMessage(playerNames[player-1] + ", please select die you wish to reroll (or select none) and click 'Roll dice'");
			display.waitForPlayerToSelectDice();
			for (int j = 0; j < N_DICE; j++) {
				if (display.isDieSelected(j)) {
					rollDice(j);
				}
			}
		} display.displayDice(dice);
		
		while (true) {		
			display.printMessage(playerNames[player-1] + ", please select a category.");
			int category = display.waitForPlayerToSelectCategory();
			System.out.println("Category is " + Integer.toString(category));
			System.out.println("Dice are " + Arrays.toString(dice));
			if (scorecard[category - 1][player -1] == -1) {
				scorecard[category - 1][player -1] = checkCategory(dice, category);
				display.updateScorecard(category, player, scorecard[category - 1][player -1]);
				break;
			}
		}
	}
	

	public static int checkCategory(int[] dice, int category) {
		int[] sortedDie = Arrays.copyOf(dice, 5);
		Arrays.sort(sortedDie);
		
		int sum = 0;
		if (category < 7) {
			for (int i = 0; i < 5; i++) {
				if(sortedDie[i] == category) {
					sum += sortedDie[i];
				}
			}return sum;
		}else if (category == THREE_OF_A_KIND) { 
			return sumOfAKind(sortedDie, 3);
		}else if (category == FOUR_OF_A_KIND) { 
			return sumOfAKind(sortedDie, 4);
		}else if (category == YAHTZEE) { 
			return sumOfAKind(sortedDie, 5);
		}else if (category == CHANCE) {
			for (int i = 0; i < 5; i++) {
				sum += sortedDie[i];
			}return sum;
		}else if (category == FULL_HOUSE) {
			if (((sortedDie[0] == sortedDie[1])&&
				 (sortedDie[1] == sortedDie[2])&&
				 (sortedDie[3] == sortedDie[4]))||
			    ((sortedDie[0] == sortedDie[1])&&
			     (sortedDie[2] == sortedDie[3])&&
				 (sortedDie[3] == sortedDie[4]))) {
				return 25;
			}
		}else if ((category == SMALL_STRAIGHT)||(category == LARGE_STRAIGHT)) {
			int count = 0;
			for (int i = 1; i < 5; i++) {
				int difference = sortedDie[i]-sortedDie[i-1];
				if (difference == 1) {
					count += 1;
				}
			}if ((category == SMALL_STRAIGHT)&&(count>2)) {
				return 30;
			}else if ((category == LARGE_STRAIGHT)&&(count>3)) {
				return 40; 	
			}		
		}return 0;
	}
	
	
	private static int sumDice(int [] dice) { 
		int sum = 0;
		for (int i = 0; i < 5; i++) {
			sum += dice[i];
		}return sum;
	}
	
	
	private static int sumOfAKind(int [] dice, int x) { 	
		for (int die_score = 1; die_score < 7; die_score++) { // die score
			int count = 0;
			for (int die = 0; die < 5; die++) { //die
				if (die_score == dice[die]) {
					count += 1;
					if (count == x) { 
						if (x > 4) {
							return 50;
						}return  x*die_score;
					}
				}
			}		
		}return 0;
	}

	private void rollDice(int index) {
			dice[index] = rgen.nextInt(1,6);
	}

	public void setTestArrays() {
		for (int i = 0; i<4; i++) {
			int result = checkCategory(testArrayChance[i], CHANCE);
			System.out.println("result is "+ result); 
		}
	}
	
/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	private int[] dice = new int[N_DICE];
	private int[][]scorecard = new int[N_CATEGORIES][nPlayers];
	private int remainingRounds = N_SCORING_CATEGORIES; 

	/* Private instance variables, for testing */	
	private int[][]testArrayOnes = {{1,1,1,1,1},{5,6,4,2,3},{1,1,2,3,2},{1,2,2,2,2}};//5, 0, 2, 1	
	private int[][]testArrayTwos = {{2,2,2,2,2},{5,6,4,6,3},{1,1,2,3,2},{1,1,1,2,1}};//10,0,4,2
	private int[][]testArrayThrees = {{3,3,3,3,3},{5,6,4,2,1},{1,3,2,3,2},{1,3,2,2,2}};//15,0,6,3
	private int[][]testArrayFours = {{4,4,4,4,4},{5,6,1,2,3},{1,4,2,4,2},{1,2,4,2,2}};//20,0,8,4
	private int[][]testArrayFives = {{5,5,5,5,5},{1,6,4,2,3},{1,5,2,5,2},{1,2,5,2,2}};//25,0,10,5
	private int[][]testArraySixes = {{6,6,6,6,6},{5,1,4,2,3},{1,6,2,6,2},{1,2,6,2,2}};//30,0,12,6
	private int[][]testArrayTOAK = {{1,1,1,6,5},{5,6,4,2,3},{4,1,4,3,4},{6,6,2,2,6}};//3, 0, 12, 18
	private int[][]testArraySStraight = {{2,3,4,5,5},{2,3,4,5,6},{2,3,2,5,4},{6,6,2,2,6}};//30, 30, 30, 0
	private int[][]testArrayLStraight = {{2,3,4,5,6},{1,3,2,5,4},{2,3,2,5,4},{5,4,3,2,1}};//40, 40, 0, 40
	private int[][]testArrayYahtzeeBang = {{2,2,2,2,2},{1,1,1,1,1},{2,3,2,5,4},{6,6,6,6,1}};//50, 50, 0, 0
	private int[][]testArrayChance = {{2,2,2,2,2},{1,1,1,1,1},{2,3,2,5,4},{6,6,6,6,1}};//10, 5, 16, 25
}

package byow.Core;

import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;
import java.util.Random;
import java.awt.Color;
import java.awt.Font;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class DisplayGame {

    /** The width of the window of this game. */
    private int width;
    /** The height of the window of this game. */
    private int height;
    /** The Random object used to randomly generate Strings. */
    private Random rand;
    /** Whether or not the game is over. */
    private boolean gameOver;


    public static void main(String[] args) {
        //long seed = Long.parseLong(args[0]);
        long seed = 5197880843569031643L;
        int width = 60;
        int height = 40;
        char c;
        DisplayGame game = new DisplayGame(width, height, seed);
        game.startGame();
        while (true) {//while loop isn't working. only the initial 'n' input works.
            if (StdDraw.hasNextKeyTyped()) {
                c = StdDraw.nextKeyTyped();
                if (c == 'n') {
                    game.startDungeon(width, height, seed);
                } else if (c == 'l') {

                } else if (c == 'q') {
                    //save and quit
                    break;
                } else {
                    continue;
                }
            }
        }
    }

    public DisplayGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
        this.rand = new Random(seed);
    }

    public void startGame() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 50);
        StdDraw.setFont(fontBig);
        StdDraw.text(width/2, height*3/4, "CS61BL: Dungeon");
        Font fontSmall = new Font("Monaco", Font.PLAIN, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.text(width/2, height/2 - 1, "[n]ew Game");
        StdDraw.text(width/2, height/2 - 3, "[l]oad Game");
        StdDraw.text(width/2, height/2 - 5, "[q]uit");
        StdDraw.show();
    }

    public void startDungeon(int width, int height, long seed) {
        BSPTree bsp = new BSPTree(width, height, seed);
        BSPTree.Node<Area> curr = bsp.getRoot();
        bsp.createRandomTree(curr, 0, width, 0, height, 4);

        TERenderer ter = new TERenderer();
        ter.initialize(width, height);

        TETile[][] dungeonTile = new TETile[width][height];
        TETile[][] displayTile = new TETile[width][height];

        bsp.fillNothing(dungeonTile, width, height);
        bsp.fillNothing(displayTile, width, height);

        bsp.createDungeon(curr, dungeonTile);
        ter.renderFrame(dungeonTile);
    }

}

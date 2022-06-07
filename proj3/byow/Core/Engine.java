package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;
import org.junit.Test;

import java.util.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map.Entry;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 60;
    public static final int HEIGHT = 40;
    private final boolean DEBUG = false; // change to `false` before submitting to gradescope
    private BSPTree finalWorld;
    boolean lightOn = false;
    boolean quit = false;
    private int[] target;
    private LinkedList<Point> path = new LinkedList<>();
    private long seed;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        ter.initialize(WIDTH, HEIGHT);
        createMenu();
    }

    private BSPTree initWorld(long seed) {
        BSPTree bsp = new BSPTree(WIDTH,HEIGHT,seed);
        BSPTree.Node<Area> root = bsp.getRoot();
        int num = bsp.chooseNumOfRooms();
        bsp.createRandomTree(root, 0, WIDTH, 0, HEIGHT, num);

        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] dungeonTile = new TETile[WIDTH][HEIGHT];
        TETile[][] displayTile = new TETile[WIDTH][HEIGHT];

        bsp.fillNothing(dungeonTile, WIDTH, HEIGHT);
        bsp.fillNothing(displayTile, WIDTH, HEIGHT);

        bsp.createDungeon(root, dungeonTile);
        bsp.displayAroundAvatar(dungeonTile, displayTile);
        return bsp;
    }

    private BSPTree loadGame(long seed) {
        File bspFile = new File("game.txt");
        if (bspFile.exists()) {
            return Utils.readObject(bspFile,BSPTree.class);
        }
        return initWorld(seed);
    }

    private BSPTree loadGame() {
        File bspFile = new File("game.txt");
        if (bspFile.exists()) {
            return Utils.readObject(bspFile,BSPTree.class);
        }
        return null;
    }

    private String getSeed() {
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                Character nextKey = StdDraw.nextKeyTyped();
                if (nextKey.equals('S') || nextKey.equals('s')) {
                    return sb.toString();
                } else {
                    if (nextKey > '9' || nextKey < '0') {
                        throw new IllegalArgumentException();
                    }
                    sb.append(nextKey);
                    drawFrame(sb.toString());
                }
            }
        }
    }

    public void drawFrame(String s) {
        /* Take the input string S and display it at the center of the screen,
         * with the pen settings given below. */
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, s);
        StdDraw.show();
    }

    private void createMenu() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontSmall = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(fontSmall);
        StdDraw.text(WIDTH / 2,HEIGHT / 2,"New Game(N)");
        StdDraw.text(WIDTH / 2,HEIGHT / 2 - 2,"Load Game(L)");
        StdDraw.text(WIDTH / 2,HEIGHT / 2 - 4,"Quit Game(:Q)");
        StdDraw.show();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                System.out.println("Next Key received.");
                Character nextKey = StdDraw.nextKeyTyped();
                System.out.println("next key is " + nextKey);
                if (nextKey.equals('n') || nextKey.equals('N')) {
                    System.out.println("enter condition N");
                    String s = getSeed();
                    System.out.println("get seed completed.");
                    seed = Long.parseLong(s);
                    System.out.println("seed has been assigned.");
                    BSPTree newWorld = initWorld(seed);
                    finalWorld = newWorld;
                    System.out.println("get init world.");
                    ter.initialize(WIDTH,HEIGHT);
                    ter.renderFrame(newWorld.displayTile);
                    System.out.println("world has been rendered.");
                    playGame(finalWorld,false);
                    File bspFile = new File("game.txt");
                    Utils.writeObject(bspFile,newWorld);
                    return;
                } else if (nextKey.equals('l') || nextKey.equals('L')) {
                    BSPTree newWorld = loadGame();
                    if (newWorld == null) {
                        String s = getSeed();
                        seed = Long.parseLong(s);
                        newWorld = loadGame(seed);
                    }
                    finalWorld = newWorld;
                    ter.initialize(WIDTH,HEIGHT);
                    this.lightOn = newWorld.lightOn;
                    if (!this.lightOn) {
                        ter.renderFrame(newWorld.displayTile);
                    } else {
                        ter.renderFrame(newWorld.finalTile);
                    }
                    playGame(finalWorld,true);
                    return;
                } else if (nextKey.equals(':')) {
                    while (true) {
                        if (StdDraw.hasNextKeyTyped()) {
                            Character nextC = StdDraw.nextKeyTyped();
                            if (nextC.equals('q') || nextC.equals('Q')) {
                                System.exit(0);
                            }
                        }
                    }
                }
            }
        }
    }

    private void playGame(BSPTree world,boolean isLoad) {
        if (isLoad) {
            target = new int[]{world.targetPoint.getX(),world.targetPoint.getY()};
        } else {
            target = randomTarget(world);
        }
        while (true) {
            moveMouse(world);
            if (StdDraw.hasNextKeyTyped()) {
                Character nextMove = StdDraw.nextKeyTyped();
                if (nextMove.equals(':')) {
                    quit = true;
                    continue;
                }
                if (quit && (nextMove.equals('q') || nextMove.equals('Q'))) {
                    world.lightOn = this.lightOn;
                    File bspFile = new File("game.txt");
                    Utils.writeObject(bspFile,world);
                    System.exit(0);
                }
                moveAvatar(world,nextMove);
                quit = false;
            }
        }
    }

    private LinkedList<Point> pathFinding(TETile[][] world,Point avatar,Point target) {
        System.out.println("targetX is: " + target.getX() + " targetY is: " + target.getY());
        LinkedList<Point> path = new LinkedList<>();
        PriorityQueue<Cell> minPq = new PriorityQueue<>();
        boolean[][] marked = new boolean[WIDTH][HEIGHT];
        boolean[][] added = new boolean[WIDTH][HEIGHT];
        HashMap<Point,Point> comesTo = new HashMap<>();
        //double[][] distTo = new double[WIDTH][HEIGHT];
        for (int i = 0;i < WIDTH;i++) {
            for (int j = 0; j < HEIGHT; j++) {
                marked[i][j] = false;
                added[i][j] = false;
            }
        }
        minPq.add(new Cell(avatar,0));
        System.out.println("The size of pq before loops: " + minPq.size());
        while (!minPq.isEmpty()) {
            System.out.println("The program stuck here");
            System.out.println("The size of pq before poll() : " + minPq.size());
            Cell curr = minPq.poll();
            System.out.println("The size of pq after poll() : " + minPq.size());
            if (!marked[curr.pos.getX()][curr.pos.getY()]) {
                marked[curr.pos.getX()][curr.pos.getY()] = true;
            }

            if (curr.pos.getX() == target.getX()
                    && curr.pos.getY() == target.getY()) {
                System.out.println("Path has been found!");
                //Point currPos = comesTo.get(curr.pos);
                Point currPos = curr.pos;
                while (comesTo.containsKey(currPos)) {
                    path.add(currPos);
                    currPos = comesTo.get(currPos);
                }
                return path;
            }
            findPathCore(world,curr,target,minPq,added,comesTo);
        }
        System.out.println("No path found out!");
        return new LinkedList<>();
    }

    private void findPathCore(TETile[][] world,Cell curr,Point target,PriorityQueue<Cell> minPq,boolean[][] added,HashMap<Point,Point> comesTo) {
        int[][] delta = new int[][]{{0,1},{0,-1},{-1,0},{1,0}};
        for (int i = 0;i < 4;i++) {
            Point newPoint = new Point(curr.pos.getX() + delta[i][0],curr.pos.getY() + delta[i][1]);
            double newW = curr.weight + 1 + getDistance(newPoint,target);
            System.out.println("currX: " + curr.pos.getX() + " currY: " + curr.pos.getY());
            if (newPoint.getX() < WIDTH && newPoint.getY() < HEIGHT
                    && newPoint.getX() >= 0 && newPoint.getY() >= 0
                    && (world[newPoint.getX()][newPoint.getY()].description().equals("floor")
                    || world[newPoint.getX()][newPoint.getY()].description().equals("mountain")
                    || world[newPoint.getX()][newPoint.getY()].description().equals("grass"))
                    && !added[newPoint.getX()][newPoint.getY()]) { //check for validation
                minPq.add(new Cell(newPoint,newW));
                comesTo.put(newPoint,curr.pos);
                added[newPoint.getX()][newPoint.getY()] = true;
                System.out.println("newX: " + newPoint.getX() + " , newY " + newPoint.getY() + " has been added");
            }
        }
    }

    private double getDistance(Point p1,Point p2) {
        return Math.abs(p2.getX() - p1.getX()) + Math.abs(p2.getY() - p1.getY());
                //Math.sqrt(Math.pow((double)(p1.getX() - p2.getX()),2.0) + Math.pow((double)(p1.getY() - p2.getY()),2.0));
    }

    private int[] randomTarget(BSPTree world) {
        TETile[][] tiles = world.finalTile;
        Random rand = new Random(seed++);
        int x = rand.nextInt(WIDTH);
        int y = rand.nextInt(HEIGHT);
        while (true) {
            if (tiles[x][y].description().equals("floor")) {
                break;
            }
            x = rand.nextInt(WIDTH);
            y = rand.nextInt(HEIGHT);
        }
        tiles[x][y] = Tileset.MOUNTAIN;
        world.targetPoint = new Point(x,y);
        finalWorld.finalTile = tiles;
        world.displayAroundAvatar(world.finalTile, world.displayTile);
        finalWorld.displayTile = world.displayTile;
        ter.renderFrame(finalWorld.displayTile);
        int[] res = new int[2];
        res[0] = x;
        res[1] = y;
        return res;
    }

    private void moveAvatar(BSPTree world,Character nextMove) {
        int x = world.avatarPoint.getX();
        System.out.println("avatar x is " + x);
        int y = world.avatarPoint.getY();
        System.out.println("avatar y is " + y);
        LinkedList<Point> newPath = pathFinding(world.finalTile,world.avatarPoint,new Point(target[0],target[1]));
        Collections.reverse(newPath);
        if (path.isEmpty()) {
            path = newPath;
        } else {
            path.remove();
            /*
            if (path.isEmpty()) {
                path.add(new Point(target[0],target[1]));
            }
             */
        }
        for (Point p : path) {
            System.out.println("x is " + x + " y is: " + y);
            System.out.println("x in path is: " + p.getX() + " y in path is: " + p.getY() + " taregt is: " + target[0] + " ," + target[1]);
            if (p.getX() != target[0] && p.getY() != target[1]) {
                world.finalTile[p.getX()][p.getY()] = Tileset.GRASS;
            }
        }

        switch(nextMove) {
            case 'W':
            case 'w':
                if (!lightOn) {
                    if (!world.finalTile[x][y + 1].description().equals("wall")
                            && !world.finalTile[x][y + 1].description().equals("nothing")) {
                        world.finalTile[x][y + 1] = Tileset.AVATAR;
                        world.finalTile[x][y] = Tileset.FLOOR;
                        if (x == target[0] && y + 1 == target[1]) {
                            target = randomTarget(world);
                            finalWorld.finalTile[target[0]][target[1]] = Tileset.MOUNTAIN;
                            System.out.println("target has changed.");
                        }
                        finalWorld.finalTile = world.finalTile;
                        finalWorld.avatarPoint = new Point(x,y + 1);
                        world.displayAroundAvatar(world.finalTile, world.displayTile);
                        finalWorld.displayTile = world.displayTile;
                    } else {
                        System.out.println("Moved Failed!");
                    }
                    ter.renderFrame(world.displayTile);
                } else {
                    if (!world.finalTile[x][y + 1].description().equals("wall")
                            && !world.finalTile[x][y + 1].description().equals("nothing")) {
                        world.finalTile[x][y + 1] = Tileset.AVATAR;
                        world.finalTile[x][y] = Tileset.FLOOR;
                        if (x == target[0] && y + 1 == target[1]) {
                            target = randomTarget(world);
                            finalWorld.finalTile[target[0]][target[1]] = Tileset.MOUNTAIN;
                            System.out.println("target has changed.");
                        }
                        finalWorld.finalTile = world.finalTile;
                        finalWorld.avatarPoint = new Point(x,y + 1);
                    } else {
                        System.out.println("Moved Failed!");
                    }
                    ter.renderFrame(world.finalTile);
                }
                break;
            case 'A':
            case 'a':
                if (!lightOn) {
                    if (!world.finalTile[x - 1][y].description().equals("wall")
                            && !world.finalTile[x - 1][y].description().equals("nothing")) {
                        world.finalTile[x - 1][y] = Tileset.AVATAR;
                        world.finalTile[x][y] = Tileset.FLOOR;
                        if (x - 1 == target[0] && y == target[1]) {
                            target = randomTarget(world);
                            finalWorld.finalTile[target[0]][target[1]] = Tileset.MOUNTAIN;
                            System.out.println("target has changed.");
                        }
                        finalWorld.finalTile = world.finalTile;
                        finalWorld.avatarPoint = new Point(x - 1,y);
                        world.displayAroundAvatar(world.finalTile, world.displayTile);
                        finalWorld.displayTile = world.displayTile;
                    } else {
                        System.out.println("Moved Failed!");
                    }
                    ter.renderFrame(world.displayTile);
                } else {
                    if (!world.finalTile[x - 1][y].description().equals("wall")
                            && !world.finalTile[x - 1][y].description().equals("nothing")) {
                        world.finalTile[x - 1][y] = Tileset.AVATAR;
                        world.finalTile[x][y] = Tileset.FLOOR;
                        if (x - 1 == target[0] && y == target[1]) {
                            target = randomTarget(world);
                            finalWorld.finalTile[target[0]][target[1]] = Tileset.MOUNTAIN;
                            System.out.println("target has changed.");
                        }
                        finalWorld.finalTile = world.finalTile;
                        finalWorld.avatarPoint = new Point(x - 1,y);
                    } else {
                        System.out.println("Moved Failed!");
                    }
                    ter.renderFrame(world.finalTile);
                }
                break;
            case 'S':
            case 's':
                if (!lightOn) {
                    if (!world.finalTile[x][y - 1].description().equals("wall")
                            && !world.finalTile[x][y - 1].description().equals("nothing")) {
                        world.finalTile[x][y - 1] = Tileset.AVATAR;
                        world.finalTile[x][y] = Tileset.FLOOR;
                        if (x == target[0] && y - 1 == target[1]) {
                            target = randomTarget(world);
                            finalWorld.finalTile[target[0]][target[1]] = Tileset.MOUNTAIN;
                            System.out.println("target has changed.");
                        }
                        finalWorld.finalTile = world.finalTile;
                        finalWorld.avatarPoint = new Point(x,y - 1);
                        world.displayAroundAvatar(world.finalTile, world.displayTile);
                        finalWorld.displayTile = world.displayTile;
                    } else {
                        System.out.println("Moved Failed!");
                    }
                    ter.renderFrame(world.displayTile);
                } else {
                    if (!world.finalTile[x][y - 1].description().equals("wall")
                            && !world.finalTile[x][y - 1].description().equals("nothing")) {
                        world.finalTile[x][y - 1] = Tileset.AVATAR;
                        world.finalTile[x][y] = Tileset.FLOOR;
                        if (x == target[0] && y - 1 == target[1]) {
                            target = randomTarget(world);
                            finalWorld.finalTile[target[0]][target[1]] = Tileset.MOUNTAIN;
                            System.out.println("target has changed.");
                        }
                        finalWorld.finalTile = world.finalTile;
                        finalWorld.avatarPoint = new Point(x,y - 1);
                    } else {
                        System.out.println("Moved Failed!");
                    }
                    ter.renderFrame(world.finalTile);
                }
                break;
            case 'D':
            case 'd':
                if (!lightOn) {
                    if (!world.finalTile[x + 1][y].description().equals("wall")
                            && !world.finalTile[x + 1][y].description().equals("nothing")) {
                        world.finalTile[x + 1][y] = Tileset.AVATAR;
                        world.finalTile[x][y] = Tileset.FLOOR;
                        if (x + 1 == target[0] && y == target[1]) {
                            target = randomTarget(world);
                            finalWorld.finalTile[target[0]][target[1]] = Tileset.MOUNTAIN;
                            System.out.println("target has changed.");
                        }
                        finalWorld.finalTile = world.finalTile;
                        finalWorld.avatarPoint = new Point(x + 1,y);
                        world.displayAroundAvatar(world.finalTile, world.displayTile);
                        finalWorld.displayTile = world.displayTile;
                    } else {
                        System.out.println("Moved Failed!");
                    }
                    ter.renderFrame(world.displayTile);
                } else {
                    if (!world.finalTile[x + 1][y].description().equals("wall")
                            && !world.finalTile[x + 1][y].description().equals("nothing")) {
                        world.finalTile[x + 1][y] = Tileset.AVATAR;
                        world.finalTile[x][y] = Tileset.FLOOR;
                        if (x + 1 == target[0] && y == target[1]) {
                            target = randomTarget(world);
                            finalWorld.finalTile[target[0]][target[1]] = Tileset.MOUNTAIN;
                            System.out.println("target has changed.");
                        }
                        finalWorld.finalTile = world.finalTile;
                        finalWorld.avatarPoint = new Point(x + 1,y);
                    } else {
                        System.out.println("Moved Failed!");
                    }
                    ter.renderFrame(world.finalTile);
                }
                break;
            case 'e':
            case 'E':
                if (!lightOn) {
                    lightOn = true;
                    ter.renderFrame(world.finalTile);
                } else {
                    lightOn = false;
                    world.displayAroundAvatar(world.finalTile, world.displayTile);
                    finalWorld.displayTile = world.displayTile;
                    ter.renderFrame(world.displayTile);
                }
            default:
                break;
        }
        //System.out.println("targetX is " + target[0] + " targetY is " + target[1]);
    }

    private void moveMouse(BSPTree world) {
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.text(WIDTH / 2,HEIGHT - 1,"FLOOR");
        StdDraw.text(WIDTH / 2,HEIGHT - 1,"AVATAR");
        StdDraw.text(WIDTH / 2,HEIGHT - 1,"NOTHING");
        StdDraw.text(WIDTH / 2,HEIGHT - 1,"WALL");
        StdDraw.text(WIDTH / 2,HEIGHT - 1,"HIDDEN");
        StdDraw.text(WIDTH / 2,HEIGHT - 1,"TARGET");
        StdDraw.text(WIDTH/2, HEIGHT - 1, "PATH");
        int x = (int)StdDraw.mouseX();
        int y = (int)StdDraw.mouseY();
        StdDraw.setPenColor(Color.WHITE);
        if (x < WIDTH && y < HEIGHT && x >= 0 && y >= 0) {
            if (!lightOn) {
                if (world.displayTile[x][y].description().equals("floor")) {
                    StdDraw.text(WIDTH / 2,HEIGHT - 1,"FLOOR ");
                } else if (world.displayTile[x][y].description().equals("you")) {
                    StdDraw.text(WIDTH / 2,HEIGHT - 1,"AVATAR");
                } else if (world.displayTile[x][y].description().equals("nothing")) {
                    StdDraw.text(WIDTH / 2,HEIGHT - 1,"NOTHING");
                } else if (world.displayTile[x][y].description().equals("wall")) {
                    StdDraw.text(WIDTH / 2,HEIGHT - 1,"WALL");
                } else if (world.displayTile[x][y].description().equals("hidden")) {
                    StdDraw.text(WIDTH / 2,HEIGHT - 1,"HIDDEN");
                } else if (world.displayTile[x][y].description().equals("mountain")) {
                    StdDraw.text(WIDTH / 2,HEIGHT - 1,"TARGET");
                } else if (world.displayTile[x][y].description().equals("grass")) {
                    StdDraw.text(WIDTH/2, HEIGHT - 1, "PATH");
                }
            } else {
                if (world.finalTile[x][y].description().equals("floor")) {
                    StdDraw.text(WIDTH / 2,HEIGHT - 1,"FLOOR");
                } else if (world.finalTile[x][y].description().equals("you")) {
                    StdDraw.text(WIDTH / 2,HEIGHT - 1,"AVATAR");
                } else if (world.finalTile[x][y].description().equals("nothing")) {
                    StdDraw.text(WIDTH / 2,HEIGHT - 1,"NOTHING");
                } else if (world.finalTile[x][y].description().equals("wall")) {
                    StdDraw.text(WIDTH / 2,HEIGHT - 1,"WALL");
                } else if (world.finalTile[x][y].description().equals("hidden")) {
                    StdDraw.text(WIDTH / 2,HEIGHT - 1,"HIDDEN");
                } else if (world.finalTile[x][y].description().equals("mountain")) {
                    StdDraw.text(WIDTH / 2,HEIGHT - 1,"TARGET");
                } else if (world.finalTile[x][y].description().equals("grass")) {
                    StdDraw.text(WIDTH / 2,HEIGHT - 1,"PATH");
                }
            }
        }
        StdDraw.show();
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        boolean load = false;
        long seedInt = 0;
        String movement = "";
        BSPTree loadBsp = null;

        char[] c = input.toCharArray();
        if (c[0] == 'n' || c[0] == 'N') {
            //start a new game
        } else if (c[0] == 'l' || c[0] == 'L') {
            //load a game
            File bspFile = new File("game.txt");
            if (bspFile.exists()) {
                loadBsp = Utils.readObject(bspFile,BSPTree.class);
                load = true;
            } else {
                System.out.println("error");
            }
        } else if (c[0] == 'q' || c[0] == 'Q') {
            //nothing to do when the initial input is 'q'
            return null;
        } else {
            return null;
        }
        if (!load) {
            String seed = "";
            int i = 1;
            while (true) {// this is not working
                if (c[i] == 's' || c[i] == 'S') {
                    break;
                }
                seed += c[i];
                i += 1;
            }
            seedInt = Long.valueOf(seed);
            for (i += 1; i < c.length; i++) {
                movement += c[i];
            }
        }
        if (load) {
            for (int i = 1; i < c.length; i++) {
                movement += c[i];
            }
            loadBsp.moveAvatar(loadBsp.finalTile, movement);

            //quit and save the game
            File bspFile = new File("game.txt");
            Utils.writeObject(bspFile, loadBsp);

            return loadBsp.finalTile;
        }

        BSPTree bsp = new BSPTree(WIDTH, HEIGHT, seedInt);
        BSPTree.Node<Area> curr = bsp.getRoot();
        bsp.createRandomTree(curr, 0, WIDTH, 0, HEIGHT, 4);

        TETile[][] finalWorldFrame;

        if (DEBUG) {
            TERenderer ter = new TERenderer();
        }

        if (DEBUG) {
            ter.initialize(WIDTH, HEIGHT);
        }

        finalWorldFrame = new TETile[WIDTH][HEIGHT];
        //fill the area with Tileset.NOTHING
        bsp.fillNothing(finalWorldFrame, WIDTH, HEIGHT);

        bsp.createDungeon(curr, finalWorldFrame);

        bsp.moveAvatar(finalWorldFrame, movement);

        //quit and save the game
        File bspFile = new File("game.txt");
        Utils.writeObject(bspFile, bsp);

        return finalWorldFrame;
    }
}

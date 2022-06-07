package byow.Core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

/** @Source https://eskerda.com/bsp-dungeon-generation/
 * We used this website to get an idea of connecting
 * random sized rooms by creating hallways.
 *
 */

public class BSPTree implements Serializable {
    //create a Random object
    private final Random rand;
    //the root(whole area of this world) of the BSPTree
    private final Node<Area> root;
    //create an avatar in the left most room
    boolean avatar = true;
    //remember where the avatar is placing
    Point avatarPoint;
    Point targetPoint;
    //if true, split the area by x-coordinate, false then split by y-coordinate
    boolean positionX = false;
    //save the result of TETile[][]
    TETile[][] finalTile = null;
    TETile[][] displayTile = null;
    boolean lightOn;

    public BSPTree(int width, int height, long seed) {
        root = new Node<Area>(new Area(0, width, 0,  height));
        this.rand = new Random(seed);
    }

    public Node<Area> getRoot() {
        return this.root;
    }

    // split the area by 1/4 to 3/4 of |value2 - value1|
    public int generateRandomValue(int value1, int value2) {
        int bound = Math.abs(this.rand.nextInt((value2 - value1) / 2));
        return bound + (value1 * 3 + value2) / 4;
    }

    public int chooseNumOfRooms() {
        int num = this.rand.nextInt();
        if (num % 2 == 0) {
            return 3;
        }
        return 4;
    }

    //recursively create several sizes of rooms. 2^num numbers of rooms are created
    public void createRandomTree(Node<Area> curr, int x1, int x2, int y1, int y2, int num) {
        if (num <= 0) {
            return;
        }
        //randomly choosing the coordinate to split. x or y
        int bound = this.rand.nextInt(Integer.MAX_VALUE - 1);
        if (x2 - x1 < 3 || y2 - y1 < 3) {
            return;
        }
        if (x2 - x1 < 12 && y2 - y1 < 12) {
            return;
        }
        if (x2 - x1 < 12) {
            bound = 1;
        }
        if (y2 - y1 < 12) {
            bound = 2;
        }
        if (bound % 2 == 0) {
            positionX = true;
        } else {
            positionX = false;
        }
        curr.orientation = positionX;//remember current node is splitting by x or y
        if (positionX) {// split by x-coordinate
            bound = generateRandomValue(x1, x2);
            curr.left = new Node<Area>(new Area(x1, bound, y1, y2));
            curr.right = new Node<Area>(new Area(bound + 1, x2, y1, y2));
            createRandomTree(curr.left, x1, bound, y1, y2, num - 1);
            createRandomTree(curr.right, bound + 1, x2, y1, y2, num - 1);
        } else {// split by y-coordinate
            bound = generateRandomValue(y1, y2);
            curr.left = new Node<Area>(new Area(x1, x2, y1, bound));
            curr.right = new Node<Area>(new Area(x1, x2, bound + 1, y2));
            createRandomTree(curr.left, x1, x2, y1, bound, num - 1);
            createRandomTree(curr.right, x1, x2, bound + 1, y2, num - 1);
        }

    }

    //using the created BSPTree, go to each leaf and create areas by using Tileset
    public void createDungeon(Node<Area> curr, TETile[][] tiles) {
        if (curr.left == null && curr.right == null) {
            int x1 = curr.area.getX1();
            int x2 = curr.area.getX2();
            int y1 = curr.area.getY1();
            int y2 = curr.area.getY2();
            //resize each rooms randomly
            createRandomRoom(tiles, curr, x1, x2, y1, y2);
            if (avatar) {
                createAvatar(tiles, curr);
                avatar = false;
            }
            return;
        }

        if (curr.left != null) {
            createDungeon(curr.left, tiles);
        }
        if (curr.right != null) {
            createDungeon(curr.right, tiles);
        }

        if (curr.left != null && curr.right != null) {
            //connect by looking at the center of each rooms
            connectRooms(curr, tiles);
        }
        finalTile = tiles;
    }

    private void connectRooms(Node<Area> curr,TETile[][] tiles) {
        boolean corridor = false;
        boolean before = true;
        boolean after = false;
        int hall;
        int start;
        int stop;
        if (curr.orientation) {//split by x
            hall = curr.area.getCenterY();
            if (curr.left.left == null && curr.left.right == null) {
                start = curr.left.area.getLeafCenterX();
            } else {
                start = curr.left.area.getCenterX();
            }
            if (curr.right.right == null && curr.right.left == null) {
                stop = curr.right.area.getLeafCenterX();
            } else {
                stop = curr.right.area.getCenterX();
            }
            for (int i = start; i < stop; i++) {
                if (i == curr.left.area.getX2()) {
                    after = true;
                }
                if (tiles[i][hall] == Tileset.WALL && tiles[i+1][hall] != Tileset.FLOOR && before) {//starting condition
                    before = false;
                    corridor = true;
                }
                if (tiles[i][hall] == Tileset.WALL && tiles[i+1][hall] != Tileset.WALL && tiles[i+1][hall] != Tileset.NOTHING && after) {//ending condition
                    tiles[i][hall] = Tileset.FLOOR;
                    tiles[i][hall + 1] = Tileset.WALL;
                    tiles[i][hall - 1] = Tileset.WALL;
                    break;
                }
                if (corridor) {
                    tiles[i][hall] = Tileset.FLOOR;
                    tiles[i][hall + 1] = Tileset.WALL;
                    tiles[i][hall - 1] = Tileset.WALL;
                }
            }
        } else {
            hall = curr.area.getCenterX();
            if (curr.left.left == null && curr.left.right == null) {
                start = curr.left.area.getLeafCenterY();
            } else {
                start = curr.left.area.getCenterY();
            }
            if (curr.right.right == null && curr.right.left == null) {
                stop = curr.right.area.getLeafCenterY();
            } else {
                stop = curr.right.area.getCenterY();
            }
            for (int i = start; i < stop; i++) {
                if (i == curr.left.area.getY2()) {
                    after = true;
                }
                if (tiles[hall][i] == Tileset.WALL && tiles[hall][i+1] != Tileset.FLOOR && before) {//starting condition
                    before = false;
                    corridor = true;
                }
                if (tiles[hall][i] == Tileset.WALL && tiles[hall][i+1] != Tileset.WALL && tiles[hall][i+1] != Tileset.NOTHING && after) {//ending condition
                    tiles[hall][i] = Tileset.FLOOR;
                    tiles[hall + 1][i] = Tileset.WALL;
                    tiles[hall - 1][i] = Tileset.WALL;
                    break;
                }
                if (corridor) {
                    tiles[hall][i] = Tileset.FLOOR;
                    tiles[hall + 1][i] = Tileset.WALL;
                    tiles[hall - 1][i] = Tileset.WALL;
                }
            }
        }
    }

    //create a random sized room inside the given area to create randomness.
    public void createRandomRoom(TETile[][] tiles, Node<Area> curr, int x1, int x2, int y1, int y2) {
        //areas will be updated to randomly small areas. Still can access to centers and connect rooms
        curr.area.setX1(x1 + this.rand.nextInt((x2 - x1)/3));
        curr.area.setY1(y1 + this.rand.nextInt((y2 - y1)/3));
        curr.area.setX2(x2 - this.rand.nextInt((x2 - x1)/3));
        curr.area.setY2(y2 - this.rand.nextInt((y2 - y1)/3));

        int newX1 = curr.area.getX1();
        int newX2 = curr.area.getX2();
        int newY1 = curr.area.getY1();
        int newY2 = curr.area.getY2();
        curr.area.setLeafCenterX((newX1 + newX2)/2);
        curr.area.setLeafCenterY((newY1 + newY2)/2);
        //print a random room
        for (int i = newX1; i < newX2; i++) {
            for (int j = newY1; j < newY2; j++) {
                if (i == newX1 || j == newY1
                        || i == newX2 - 1 || j == newY2 - 1) {//surrounding area should be a wall
                    //System.out.println("wall");
                    tiles[i][j] = Tileset.WALL;
                } else {
                    //System.out.println("floor");
                    tiles[i][j] = Tileset.FLOOR;
                }
            }
        }
    }

    public void fillNothing(TETile[][] tiles, int width, int height) {
        for (int i = 0; i < width; i += 1) {
            for (int j = 0; j < height; j += 1) {
                tiles[i][j] = Tileset.NOTHING;
            }
        }
    }

    public void createAvatar(TETile[][] tiles, Node<Area> curr) {
        int x = curr.area.getCenterX();
        int y = curr.area.getCenterY();
        avatarPoint = new Point(x,y);
        tiles[x][y] = Tileset.AVATAR;
    }

    public void displayAroundAvatar(TETile[][] tiles, TETile[][] display) {
        for (int i = 0; i < tiles.length; i += 1) {
            for (int j = 0; j < tiles[0].length; j += 1) {
                display[i][j] = Tileset.HIDDEN;
            }
        }
        for (int i = avatarPoint.getX() - 3; i <= avatarPoint.getX() + 3; i++) {
            for (int j = avatarPoint.getY() - 3; j <= avatarPoint.getY() + 3; j++) {
                if (i < 0 || i >= tiles.length || j < 0 || j >= tiles[0].length
                        || (i == avatarPoint.getX()-3 && j == avatarPoint.getY()-3)
                        || (i == avatarPoint.getX()-3 && j == avatarPoint.getY()+3)
                        || (i == avatarPoint.getX()-3 && j == avatarPoint.getY()-2)
                        || (i == avatarPoint.getX()-2 && j == avatarPoint.getY()-3)
                        || (i == avatarPoint.getX()-3 && j == avatarPoint.getY()+2)
                        || (i == avatarPoint.getX()-2 && j == avatarPoint.getY()+3)
                        || (i == avatarPoint.getX()+3 && j == avatarPoint.getY()+3)
                        || (i == avatarPoint.getX()+3 && j == avatarPoint.getY()+2)
                        || (i == avatarPoint.getX()+2 && j == avatarPoint.getY()+3)
                        || (i == avatarPoint.getX()+3 && j == avatarPoint.getY()-3)
                        || (i == avatarPoint.getX()+3 && j == avatarPoint.getY()-2)
                        || (i == avatarPoint.getX()+2 && j == avatarPoint.getY()-3)){
                    continue;
                }
                display[i][j] = tiles[i][j];
            }
        }
        displayTile = display;
    }

    public void moveAvatar(TETile[][] tiles, String movement) {
        char[] eachMove = movement.toCharArray();
        Point prev = avatarPoint;
        Point curr = new Point(0, 0);
        for (int i = 0; i < eachMove.length; i++) {
            if (eachMove[i] == ':' && i+1 < eachMove.length && eachMove[i] == 'Q') {
                //save and quit
                break;
            }
            if (eachMove[i] == 'w' || eachMove[i] == 'W') {
                curr.setX(prev.getX());
                curr.setY(prev.getY() + 1);
            }
            if (eachMove[i] == 'a' || eachMove[i] == 'A') {
                curr.setX(prev.getX() - 1);
                curr.setY(prev.getY());
            }
            if (eachMove[i] == 's' || eachMove[i] == 'S') {
                curr.setX(prev.getX());
                curr.setY(prev.getY() - 1);
            }
            if (eachMove[i] == 'd' || eachMove[i] == 'D') {
                curr.setX(prev.getX() + 1);
                curr.setY(prev.getY());
            }
            if (tiles[curr.getX()][curr.getY()] == Tileset.WALL ||
                    tiles[curr.getX()][curr.getY()] == Tileset.NOTHING) {
                finalTile = tiles;
                return;
            }
            tiles[prev.getX()][prev.getY()] = Tileset.FLOOR;
            tiles[curr.getX()][curr.getY()] = Tileset.AVATAR;
        }
        finalTile = tiles;
    }

    class Node<Area> implements Serializable{
        Node<Area> left;
        Node<Area> right;
        Area area;
        boolean orientation; // true for x; false for y;
        boolean leftChild;// looking from the parent, if leftChild then true, if not false
        HashMap<String, Integer> leftPos;
        HashMap<String, Integer> rightPos;

        Node(Area area) {
            this.area = area;
            this.left = null;
            this.right = null;
            this.orientation = false;
            this.leftChild = true;
            leftPos = new HashMap<>();
            rightPos = new HashMap<>();
        }
    }

    public static void main(String[] args) {
        int width = 60;
        int height = 40;
        long seed = 880843569031643L;
        BSPTree bsp = new BSPTree(width, height, seed);
        Node<Area> curr = bsp.getRoot();
        int num = bsp.chooseNumOfRooms();
        bsp.createRandomTree(curr, 0, width, 0, height, num);

        TERenderer ter = new TERenderer();
        ter.initialize(width, height);

        TETile[][] dungeonTile = new TETile[width][height];
        TETile[][] displayTile = new TETile[width][height];

        bsp.fillNothing(dungeonTile, width, height);
        bsp.fillNothing(displayTile, width, height);

        bsp.createDungeon(curr, dungeonTile);
        ter.renderFrame(dungeonTile);

        //bsp.displayAroundAvatar(dungeonTile, displayTile);

        //ter.renderFrame(displayTile);
    }
}

package byow.Core;

import java.io.Serializable;

public class Area implements Serializable {

    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private boolean tooSmall;
    private int centerX;
    private int centerY;
    private int leafCenterX;
    private int LeafCenterY;

    public Area(int x1, int width, int y1, int height) {
        this.x1 = x1;
        this.x2 = width;
        this.y1 = y1;
        this.y2 = height;
        this.tooSmall = false;
        this.centerX = (this.x1 + this.x2) / 2;
        this.centerY = (this.y1 + this.y2) / 2;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getLeafCenterX() {
        return leafCenterX;
    }

    public int getLeafCenterY() {
        return LeafCenterY;
    }

    public void setLeafCenterX(int leafCenterX) {
        this.leafCenterX = leafCenterX;
    }

    public void setLeafCenterY(int leafCenterY) {
        LeafCenterY = leafCenterY;
    }

    public int getX1() {return this.x1;}

    public int getX2() {return this.x2;}

    public int getY1() {return this.y1;}

    public int getY2() {return this.y2;}

    public void setX1(int x1) {this.x1 = x1;}

    public void setX2(int x2) {this.x2 = x2;}

    public void setY1(int y1) {this.y1 = y1;}

    public void setY2(int y2) {this.y2 = y2;}

    public boolean getTooSmall() {return this.tooSmall;}

    public void setTooSmall(boolean small) {this.tooSmall = small;}

    public int area() {
        return (x2 - x1) * (y2 - y1);
    }
}

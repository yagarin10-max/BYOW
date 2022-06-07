# Build Your Own World Design Document

**Partner 1:**

**Partner 2:**

## Classes and Data Structures
> Area Class //this is the node of BSPTree. Represents the area with 4 values. 
 Instance variables/ methods
 - int x1
 - int x2
 - int y1
 - int y2
 - public int area(int x1, int x2, int y1, int y2)//returns the area of this area
 - boolean tooSmall //could use to delete too small areas and create the dungeon more randomly

> BSPTree Class //creates the base of random area dungeon
 Instance variables/ methods
 - Area root // x1=0, x2=WIDTH, y1=0, y2=HEIGHT
 - boolean positionx
 - boolean positiony
 - randomXorY(String seed) // choose x or y from the seed
 - void splitX(String seed) //split the area horizontically, and randomly according to the seed
 - void splitY(String seed) //split the area vertically, and randomly according to the seed
 - void createRandomTree(String seed) //recursively create a BSPTree by spliting the whole area using splitX and splitY
 - void createCorridor(root) //recursively connect the siblings of BSPTree, and create a corridor

## Algorithms

## Persistence

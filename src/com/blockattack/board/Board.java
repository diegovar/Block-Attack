package com.blockattack.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.input.touch.TouchEvent;

import android.util.Log;
import android.util.Pair;

import com.blockattack.BlockAttackActivity;
import com.blockattack.board.controller.IBoardController;
import com.blockattack.objects.Block;
import com.blockattack.objects.CellPosition;

public class Board extends Rectangle implements IEntity, IUpdateHandler {

	
	private int initialX;
	private int initialY;
	
	private IBoardController controller;
	private BoardState state;
	private BoardState initialState;
	
	private static int emptyProbability = 60; //percentage X%
	private static int dropSpeed = 32; //ticks per block's height drop
	//private static int aeroSwapTicks = 2; //players can swap blocks in mid-air while they're falling. 
	
	/**
	 * Constructors
	 */
	
	public Board(IBoardController controller, int speed, int blocksPerRow, int maximumRows, int pX, int pY, BoardState state)
	{
		super(pX,pY,blocksPerRow*Block.width,maximumRows*Block.height);
		this.state = state;
		setupBoard(controller, speed, blocksPerRow, state, pX, pY);
	}
	
	public Board(IBoardController controller, int speed, int blocksPerRow, int maximumRows, int pX, int pY)
	{
		super(pX,pY,blocksPerRow*Block.width,maximumRows*Block.height);
		BoardState state = generateInitialConfiguration(speed, blocksPerRow, maximumRows);
		setupBoard(controller, speed, blocksPerRow, state, pX, pY);		
	}
	
	private void setupBoard(IBoardController controller, int speed, int blocksPerRow, BoardState state, int pX, int pY) {
		this.controller = controller;
		this.attachChild(controller);
		this.setPosition(pX, pY);
		this.initialX = pX;
		this.initialY = pY;
		this.setAlpha(0.5f);
		this.state = state;
		
		initialState = state.clone();
		
		for (ArrayList<Block> row : state.blocks) {
			attachChildBlocks(row);
		}
		attachChildBlocks(state.nextRow);
	}
	
	
	/**
	 * Getters and setters
	 */
	public BoardState getState() {
		return state;
	}
	
	
	/**
	 * Public methods
	 */

	@Override
	public void onManagedUpdate(float pSecondsElapsed) {
		super.onManagedUpdate(pSecondsElapsed);
		advanceBlocks(state.speed);
		if(!state.disappearingBlocks.isEmpty())
			updateDisappearingBlocks();
		if(!state.fallingBlocks.isEmpty())
			updateFallingBlocks();
		
		if(newRowNeeded(state.speed)) {
			promoteAllRows();
			ArrayList<Block> row = generateNewRow(state, -1, false);
			attachChildBlocks(row);
			state.nextRow = row;
		}
	}

	public void reset() {
		
		final BoardState stateCache = state;
		final Board boardCache = this;
		//detach all existing blocks
		BlockAttackActivity.getInstance().runOnUpdateThread(new Runnable() {
            public void run() {
            	for (ArrayList<Block> row : stateCache.blocks) {
					for (Block block : row) {
						boardCache.detachChild(block);
					}
				}
            }
        });
		

		//Reset the board to its initial state.
		state = generateInitialConfiguration(state.speed, state.blocksPerRow, state.maximumRows);
		this.setPosition(initialX, initialY);
		
		for (ArrayList<Block> row : state.blocks) {
			attachChildBlocks(row);
		}
		attachChildBlocks(state.nextRow);
	}
	
	public void setBlockSelected(Block b) {
		if(!state.selectedBlocks.contains(b)) {
			b.setSelected(true);
			state.selectedBlocks.add(b);
		}
	}
	
	public void swapBlocks(CellPosition pos1, CellPosition pos2) {
		
		Block b1 = state.blocks.get(pos1.row).get(pos1.col);
		Block b2 = state.blocks.get(pos2.row).get(pos2.col);
		//if one of the two is an empty space, make sure it's b2
		if(b1 == null) {
			b1 = b2;
			b2 = null;
			CellPosition swapCellTemp = pos1;
			pos1 = pos2;
			pos2 = swapCellTemp;
		}
		
		//swap blocks in block list
		state.blocks.get(pos1.row).set(pos1.col,b2);
		state.blocks.get(pos2.row).set(pos2.col,b1);
		
		//swap physically (sprite positions). No animation for now.
		float fBX = b1.getX();
		float fBY = b1.getY();
		b1.setPosition(getColPosition(pos2.col),getRowPosition(pos2.row));
		if(b2 != null) {
			b2.setPosition(fBX, fBY);
		}
		
		//swap columns and rows in the blocks themselves
		b1.position.row = pos2.row;
		b1.position.col = pos2.col;
		if(b2 != null) {
			b2.position.row = pos2.row;
			b2.position.col = pos1.col;
		}
		
		//if b2 is null, set the upper blocks as falling. Also if b1 doesn't have a floor, set it to fall as well.
		//Only the upper block of each tower is added to the falling list (more efficient)
		if(b2 == null && pos1.row + 1 < state.blocks.size()) {
			Block upper = state.blocks.get(pos1.row+1).get(pos1.col);
			if(upper != null) {
				setAsFalling(upper, true);
			}
		}
		if(b1.position.row > 0) {
			Block lower = state.blocks.get(pos2.row-1).get(pos2.col);
			if(lower == null || lower.isFalling()) {
				setAsFalling(b1, true);
			}
		}
		
		ArrayList<Pair<CellPosition, CellPosition>> matches = checkForMatches();
		
		//for now just remove all blocks from the grid.
		if(matches.size() > 0) {
			makeDisappear(matches);
		}
		
	}
	
	public int getRowIndexFromPosition(float y) {
		return (int)Math.floor((this.getHeight() - y)/Block.height);
	}
	
	/**
	 * Events
	 */
	
	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
		return controller.onTouch(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY, this);
	}
	

	
	/**
	 * Private methods
	 */
	
	private void makeDisappear(ArrayList<Pair<CellPosition, CellPosition>> matches) {
		final ArrayList<Pair<CellPosition, CellPosition>> matchesRef = matches;
		for (Pair<CellPosition, CellPosition> pair : matches) {
			for (int j = pair.first.col; j <= pair.second.col; j++) {
				for (int i = pair.first.row; i <= pair.second.row; i++) {
					state.blocks.get(i).set(j, null);
				}
				if(pair.second.row < state.blocks.size() - 1) {
					setAsFalling(state.blocks.get(pair.second.row + 1).get(j), true);
				}
			}
		}
		BlockAttackActivity.getInstance().runOnUpdateThread(new Runnable() {
            public void run() {
            	for (Pair<CellPosition, CellPosition> pair : matchesRef) {
        			for (int j = pair.first.col; j <= pair.second.col; j++) {
        				for (int i = pair.first.row; i <= pair.second.row; i++) {
        					Block b = state.blocks.get(i).get(j);
        					detachChild(b);
        				}
        			}
        		}
            }
        });
	}
	
	/**
	 * Attaches a group of blocks to the scene to be rendered.
	 * @param bs
	 */
	private void attachChildBlocks(Collection<Block> bs) {
		if(bs != null) {
			for (Iterator<Block> iterator = bs.iterator(); iterator.hasNext();) {
				Block block = iterator.next();
				if(block != null) {
					attachChild(block);
				}
			}
		}
	}
	
	
	private void setAsFalling(Block b, boolean falling) {
		b.setFalling(falling);
		b.setSwappable(!falling);
		state.fallingBlocks.add(b);
		for (int i = b.position.row + 1; i < state.blocks.size(); i++) {
			Block fall = state.blocks.get(i).get(b.position.col);
			if(fall != null) {
				fall.setFalling(falling);
				fall.setSwappable(!falling);
			}
		}
	}
	
	
	private void updateDisappearingBlocks() {
		
	}
	
	
	/**
	 * Updates the position of all the falling blocks. While a block is falling, the position marks the nearest (floor) whole grid location. The Y is updated
	 * according to the block drop speed. When the block gets to a whole grid position, its position is updated. 
	 */
	private void updateFallingBlocks() {
		ArrayList<Block> toRemoveFromFalling = new ArrayList<Block>();
		for (int i = 0; i < state.fallingBlocks.size(); i++) {

			Block block = state.fallingBlocks.get(i);
			
			//see if we've reached the bottom
			boolean decreaseRow = false;
			boolean removeFromFalling = false;
			boolean moveFall = true;
			if(block.getY() > getRowPosition(block.position.row-1)) { //got to a cell boundary
				decreaseRow = true;
				//Block lower = state.blocks.get(block.position.row-1).get(block.position.col); //should be empty or falling
				boolean bottom = block.position.row - 1 == 0;
				Block underLower = bottom ? null : state.blocks.get(block.position.row-2).get(block.position.col);
				if(bottom || underLower != null && !underLower.isFalling()) {
					removeFromFalling = true;
				}
			}
			
			if(removeFromFalling) {
				toRemoveFromFalling.add(block);
			}
			for(int j = block.position.row; j < state.blocks.size(); j++) { //note that j includes block's index
				Block upper = state.blocks.get(j).get(block.position.col);
				if(upper != null) {
					if(decreaseRow) {
						state.blocks.get(upper.position.row-1).set(upper.position.col, upper);
						state.blocks.get(upper.position.row).set(upper.position.col, null);
						upper.position.row--;
						upper.setPosition(getColPosition(upper.position.col), getRowPosition(upper.position.row));
					}
					if(removeFromFalling) {
						upper.setFalling(false);
						upper.setSwappable(true);
					} else {
						if(moveFall) {
							upper.setPosition(upper.getX(), upper.getY() + Block.height / dropSpeed);
						}
					}
				}
			}
		}
		for (int i = 0; i < toRemoveFromFalling.size(); i++) {
			Block b = toRemoveFromFalling.get(i);
			b.setPosition(b.getX(), getRowPosition(b.position.row));
			state.fallingBlocks.remove(b);
		}
	}
	
	/**
	 * Checks for matches in the entire board and returns those matches.
	 * @return
	 */
	private ArrayList<Pair<CellPosition, CellPosition>> checkForMatches() {
		ArrayList<Pair<CellPosition, CellPosition>> res = new ArrayList<Pair<CellPosition,CellPosition>>();
		
		//check horizontally
		for (int i = 0; i < state.blocks.size(); i++) {
			checkForMatchesRow(i, res);
		}
		
		//check vertically
		for (int i = 0; i < state.blocksPerRow; i++) {
			checkForMatchesCol(i, res);
		}
		
		return res;
	}
	
	/**
	 * Finds matches in the supplied row
	 * @param rowN The row
	 * @param matchesRes The array where matches are gathered
	 */
	private void checkForMatchesRow(int rowN, ArrayList<Pair<CellPosition, CellPosition>> matchesRes) {
		int type = -1;
		int matches = 0;
		for (int i = 0; i < state.blocksPerRow; i++) { //for each element that can disappear
			ArrayList<Block> row = state.blocks.get(rowN);
			Block current = row.get(i);
			if(current != null) {
				if(type == -1 || current.type != type) { //if it's the first, it's the first after an empty space or the blocks don't match.
					if(matches >= 3) {
						matchesRes.add(new Pair<CellPosition, CellPosition>(new CellPosition(rowN, i - 1 - matches - 1), new CellPosition(rowN, i - 1)));
					}
					type = current.type;
					matches = 1;
				} else if(current.type == type && !current.isFalling() && !current.isDisappearing()) { //one more match
					matches++;
				}
			} else { //as it's empty, reset everything
				type = -1;
				matches = 0;
			}
		}
		if(matches >= 3) {
			matchesRes.add(new Pair<CellPosition, CellPosition>(new CellPosition(rowN, state.blocksPerRow - matches - 1), new CellPosition(rowN, state.blocksPerRow - 1)));
		}
	}
	
	/**
	 * Finds matches in the supplied column
	 * @param colN The column
	 * @param matchesRes The array where matches are gathered
	 */
	private void checkForMatchesCol(int colN, ArrayList<Pair<CellPosition, CellPosition>> matchesRes) {
		int type = -1;
		int matches = 0;
		for (int i = 0; i < state.blocks.size(); i++) { //for each element that can disappear
			Block current = state.blocks.get(i).get(colN);
			if(current != null) {
				if(type == -1 || current.type != type) { //if it's the first, it's the first after an empty space or the blocks don't match.
					if(matches >= 3) {
						matchesRes.add(new Pair<CellPosition, CellPosition>(new CellPosition(i - 1 - matches - 1, colN), new CellPosition(i - 1, colN)));
					}
					type = current.type;
					matches = 1;
				} else if(current.type == type && !current.isFalling() && !current.isDisappearing()) { //one more match
					matches++;
				}
			} else { //as it's empty, reset everything
				type = -1;
				matches = 0;
			}
		}
		if(matches >= 3) {
			matchesRes.add(new Pair<CellPosition, CellPosition>(new CellPosition(state.blocks.size() - 1 - matches - 1, colN), new CellPosition(state.blocks.size() - 1, colN)));
		}
	}

	
	private int getRowPosition(int row) {
		return ((int)this.getHeight()) - (row + 1) * Block.height;
	}
	
	private int getColPosition(int col) {
		return col * Block.width;
	}
	
	/**
	 * Called when a new row is needed. Moves the entire board 1 block's height down 
	 * and all blocks one block's height up.
	 * @param newNextRow
	 */
	private void promoteAllRows() {
		if(state.nextRow != null) {
			state.blocks.add(0, state.nextRow);
		}
		for (Iterator<ArrayList<Block>> iterator = state.blocks.iterator(); iterator.hasNext();) {
			ArrayList<Block> row = iterator.next();
			for (Iterator<Block> iterator2 = row.iterator(); iterator2.hasNext();) {
				Block block = iterator2.next();
				if(block != null) {
					block.setPosition(block.getX(), block.getY() - Block.height);
					block.position.row++;
				}
			}
		}
		
		if(state.blocks.size() >= 10) {
			final Board boardCache = this;
			BlockAttackActivity.getInstance().runOnUpdateThread(new Runnable() {
                public void run() {
                	ArrayList<Block> row = state.blocks.get(9);
        			for (Block block : row) {
        				boardCache.detachChild(block);
        			}
        			state.blocks.remove(9);
                }
	        });
		}
		
		setPosition(getX(), getY() + Block.height);
		
		state.ticks = 0;
	}
	
	
	/**
	 * Advances the blocks in the board upwards. Called once per tick.
	 * @param speed The speed at which the blocks must move. Given in ticks per block
	 */
	private void advanceBlocks(int speed) {
		if(!state.blocks.isEmpty()) {
			float moveYTo = speed > 0 ? (float)Math.floor(state.ticks * Block.height / speed) : 0;
			if(moveYTo - getY() >= 1) {
				setPosition(getX(), - moveYTo);
			}
		}
		state.ticks++;
	}
	
	/**
	 * Returns true if a new row is needed because all the blocks have advanced a blocks's height.
	 * @param speed
	 * @return
	 */
	private boolean newRowNeeded(int speed) {
		return this.getY() <= -Block.height ;
	}
	
	
	private BoardState generateInitialConfiguration(int speed, int blocksPerRow, int maximumRows) {
		BoardState state = new BoardState(new ArrayList<ArrayList<Block>>(), null, speed, blocksPerRow, maximumRows);
		//we fill to half the maximum amount of rows.
		for (int i = maximumRows/2-1; i >= 0; i--) {
			state.blocks.add(0, generateNewRow(state, i, true));
		}
		//add the nextRow
		state.nextRow = generateNewRow(state, -1, false);
		return state;
	}
	
	

	/**
	 * Generates a new row of blocks to be inserted at the bottom of the stack. Ignores the nextRow for match checking.
	 * @param state
	 * @param y
	 * @return
	 */
	private ArrayList<Block> generateNewRow(BoardState state, int rowN, boolean allowEmpties) {
		return generateNewRow(state, rowN, allowEmpties, state.blocksPerRow);		
	}
	
	private ArrayList<Block> generateNewRow(BoardState state, int rowN, boolean allowEmpties, int blocksPerRow) {
		ArrayList<Block> row = new ArrayList<Block>(blocksPerRow);
		for (int i = 0; i < blocksPerRow; i++) {
			Block b = null;
			Block oneUp = null;
			if(state.blocks.size() > 0) {
				oneUp = state.blocks.get(0).get(i);
			}
			if(!allowEmpties || BlockAttackActivity.getInstance().random.nextInt(100) > emptyProbability ||
				oneUp != null) {
				//Find the possible values for this tile
				int forbidden1 = -1, forbidden2 = -1;
				if(i > 1) {
					Block oneBack = row.get(i-1);
					Block twoBack = row.get(i-2);
					if(oneBack != null && twoBack != null && oneBack.type == twoBack.type) {
						forbidden1 = row.get(i-1).type;
					}
				}
				if(state.blocks.size() > 1) {
					Block twoUp = state.blocks.get(1).get(i);
					if(oneUp != null && twoUp != null && oneUp.type == twoUp.type) {
						forbidden2 = oneUp.type;
					}
				}
				int j = 0;
				int [] possibleValues = new int [Block.nTypes];
				for (int k = 0; k < Block.nTypes; k++) {
					if(k != forbidden1 && k != forbidden2) {
						possibleValues[j] = k;
						j++;
					}
				}
				int type = possibleValues[BlockAttackActivity.getInstance().random.nextInt(j)];
				
				b = new Block(i*Block.width, getRowPosition(rowN), type, new CellPosition(rowN, i));
			}
			row.add(i, b);
		}
		return row;
	}

}

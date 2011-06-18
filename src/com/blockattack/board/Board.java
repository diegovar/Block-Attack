package com.blockattack.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.input.touch.TouchEvent;

import com.blockattack.BlockAttackActivity;
import com.blockattack.board.controller.IBoardController;
import com.blockattack.objects.Block;

public class Board extends Rectangle implements IUpdateHandler {

	private IBoardController controller;
	private BoardState state;
	private int speed; //given in ticks per block
	private int blocksPerRow;
	private int initialX;
	private int initialY;
	private Scene scene;
	private Cursor cursor;
	
	private void attachChildBlocks(Collection<Block> bs) {
		if(bs != null) {
			for (Iterator<Block> iterator = bs.iterator(); iterator.hasNext();) {
				Block block = iterator.next();
				attachChild(block);
			}
		}
	}
	
	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
		
		int blockTouchedX = (int)pTouchAreaLocalX / Block.width;
		int blockTouchedY = getRowIndexFromPosition(pTouchAreaLocalY);
		Block blockTouched = state.blocks.get(blockTouchedY).get(blockTouchedX);
		if(pSceneTouchEvent.isActionOutside()) {
			state.firstBlockTouched = false;
			return true;
		}
		if(pTouchAreaLocalY >= this.getHeight() - state.blocks.size() * Block.height) {
			if(pSceneTouchEvent.isActionDown())
				state.setFirstPointCursorSelected(blockTouched);
			else if(pSceneTouchEvent.isActionUp() && state.firstBlockTouched) {
				//swap in lists
				if (state.firstBlock.currentRow == blockTouched.currentRow && Math.abs(state.firstBlock.currentCol - blockTouched.currentCol) == 1) {
					state.swap(state.firstBlock, blockTouched);
				}
				state.firstBlock.setScale(1.0f);
				state.firstBlockTouched = false;
			}
			return true;
		}
		return false;
	}
	
	private void setupBoard(IBoardController controller, int speed, int blocksPerRow, BoardState state, int pX, int pY, Scene scene, Cursor cursor) {
		this.controller = controller;
		this.speed = speed;
		this.blocksPerRow = blocksPerRow;
		this.state = state;
		this.setPosition(pX, pY);
		this.initialX = pX;
		this.initialY = pY;
		this.scene = scene;
		this.cursor = cursor;
		this.setAlpha(0.0f);
		
		attachChildBlocks(state.blocks.get(0));
		attachChildBlocks(state.nextRow);
		scene.registerTouchArea(this);
	}
	
	public Board(IBoardController controller, int speed, int blocksPerRow, int pX, int pY, int width, int height, Scene scene, BoardState state)
	{
		super(pX,pY,width,height);
		setupBoard(controller, speed, blocksPerRow, state, pX, pY, scene, state.cursor);
	}
	
	public Board(IBoardController controller, int speed, int blocksPerRow, int pX, int pY, int width, int height, Scene scene)
	{
		super(pX,pY,width,height);
		this.blocksPerRow = blocksPerRow;
		this.scene = scene;
		ArrayList<Block> bs = generateNewRow(null, 0);
		List<ArrayList<Block>> rows = new ArrayList<ArrayList<Block>>();
		rows.add(bs);
		this.cursor = new Cursor();
		setupBoard(controller, speed, blocksPerRow, new BoardState(rows, generateNewRow(null, -1),cursor), pX, pY, scene, cursor);
	}
	
	@Override
	public void onManagedUpdate(float pSecondsElapsed) {
		super.onManagedUpdate(pSecondsElapsed);
		advanceBlocks(speed);
		if(newRowNeeded(speed)) {
			ArrayList<Block> row = generateNewRow(state, -1);
			attachChildBlocks(row);
			promoteNextRow(row);
		}
	}

	public void reset() {
		// TODO Auto-generated method stub

	}
	
	private int getRowPosition(int i) {
		return (int)this.getHeight() - 1 - i * Block.height;
	}
	
	private int getRowIndexFromPosition(float y) {
		return (int)Math.floor((this.getHeight() - y)/Block.height) + 1;
	}
	
	/**
	 * Generates a new row of blocks
	 * @param state
	 * @param y
	 * @return
	 */
	private ArrayList<Block> generateNewRow(BoardState state, int rowN) {
		
		ArrayList<Block> row = new ArrayList<Block>(blocksPerRow);
		for (int i = 0; i < blocksPerRow; i++) {
			int type = BlockAttackActivity.getInstance().random.nextInt(Block.nTypes);
			Block b = new Block(i*Block.width, getRowPosition(rowN), type, rowN, i);
			row.add(i, b);
		}
		return row;
	}
	
	
	
	
	private void promoteNextRow(ArrayList<Block> newNextRow) {
		if(state.nextRow != null) {
			state.blocks.add(0, state.nextRow);
		}
		state.nextRow = newNextRow;
		for (Iterator<ArrayList<Block>> iterator = state.blocks.iterator(); iterator.hasNext();) {
			ArrayList<Block> row = iterator.next();
			for (Iterator<Block> iterator2 = row.iterator(); iterator2.hasNext();) {
				Block block = iterator2.next();
				block.setPosition(block.getX(), block.getY() - Block.height);
				block.currentRow++;
			}
		}
		
		if(state.blocks.size() >= 15) {
			
			BlockAttackActivity.getInstance().runOnUpdateThread(new Runnable() {
                public void run() {
                	ArrayList<Block> row = state.blocks.get(14);
        			for (Block block : row) {
        				scene.detachChild(block);
        			}
        			state.blocks.remove(14);
                }
	        });
		}
		
		setPosition(initialX, initialY);
		
		state.ticks = 0;
	}
	
	
	/**
	 * Advances the blocks in the board upwards. Called once per tick.
	 * @param speed The speed at which the blocks must move. Given in ticks per block
	 */
	private void advanceBlocks(int speed) {
		if(!state.blocks.isEmpty()) {
			float moveYTo = (float)Math.floor(state.ticks * Block.height / speed);
			if(moveYTo - getY() >= 1) {
				setPosition(getX(), - moveYTo);
			}
		}
		state.ticks++;
	}
	
	private boolean newRowNeeded(int speed) {
		return state.ticks > speed;
	}

}

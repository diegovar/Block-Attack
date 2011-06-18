package com.blockattack.board;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.blockattack.objects.Block;

/**
 * @author Diego
 *
 */
public class BoardState {

	public boolean running;
	
	public Cursor cursor;
	
	public long score;
	public List<ArrayList<Block>> blocks;
	public ArrayList<Block> nextRow;
	long ticks = 0;
	
	public boolean firstBlockTouched = true;
	public Block firstBlock;
	
	public BoardState(List<ArrayList<Block>> initialRows, ArrayList<Block> nextRow, Cursor cursor) {
		if(initialRows == null) throw new IllegalArgumentException("initialRows is null");
		if(nextRow == null) throw new IllegalArgumentException("nextRow is null");
		if(cursor == null) throw new IllegalArgumentException("cursor is null");
		
		blocks = initialRows;
		this.nextRow = nextRow;
		this.cursor = cursor;
		running = false;		
	}
	
	public void setFirstPointCursorSelected(Block b) {
		firstBlockTouched = true;
		firstBlock = b;
		b.setZIndex(1);
		b.setScale(1.2f);
	}
	
	public void swap(Block b1, Block b2) {
		blocks.get(b1.currentRow).set(b1.currentCol,b2);
		blocks.get(b2.currentRow).set(b2.currentCol,b1);
		//swap physically (sprite positions). No animation for now.
		float fBX = b1.getX();
		float fBY = b1.getY();
		b1.setPosition(b2.getX(),b2.getY());
		b2.setPosition(fBX, fBY);
		//swap columns and rows
		int r = b1.currentRow;
		int c = b1.currentCol;
		b1.currentRow = b2.currentRow;
		b1.currentCol = b2.currentCol;
		b2.currentCol = c;
		b2.currentRow = r;
	}
	
}

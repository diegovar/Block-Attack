package com.blockattack.board;

import java.util.ArrayList;

import com.blockattack.objects.Block;

/**
 * @author Diego
 *
 */
public class BoardState {

	/**
	 * Board definition
	 */
	public int speed; //given in ticks per block
	public int blocksPerRow;
	public int maximumRows;
	
	/**
	 * Board state
	 */
	public boolean running;
	public long score;
	public long ticks = 0;
	public ArrayList<Block> selectedBlocks = new ArrayList<Block>();
	public ArrayList<Block> fallingBlocks = new ArrayList<Block>();
	public ArrayList<Block> disappearingBlocks = new ArrayList<Block>();
	/**
	 * Board components
	 */
	public ArrayList<ArrayList<Block>> blocks;
	public ArrayList<Block> nextRow;
	
	public BoardState(ArrayList<ArrayList<Block>> initialRows, ArrayList<Block> nextRow, int speed, int blocksPerRow, int maximumRows) {
		blocks = initialRows;
		this.nextRow = nextRow;
		running = false;
		this.speed = speed;
		this.blocksPerRow = blocksPerRow;
		this.maximumRows = maximumRows;
	}
	
	public BoardState clone() {
		
		//clone blocks
		ArrayList<ArrayList<Block>> copyBlocks = new ArrayList<ArrayList<Block>>();
		for (ArrayList<Block> row : blocks) {
			ArrayList<Block> copyRow = new ArrayList<Block>();
			for (Block block : row) {
				copyRow.add(block != null ? block.clone() : null);
			}
			copyBlocks.add(copyRow);
		}
		
		//clone nextRow
		ArrayList<Block> copyNextRow = new ArrayList<Block>();
		for (Block block : nextRow) {
			copyNextRow.add(block.clone());
		}
		
		//selected blocks are not copied as they're not persistent.
		
		return new BoardState(copyBlocks, copyNextRow, speed, blocksPerRow, maximumRows);
	}
}

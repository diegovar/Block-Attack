package com.blockattack.board;

public interface IControlled {
	
	/**
	 * Coordinates are in number of blocks in the x and y directions
	 * @param x
	 * @param y
	 */
	public void setCursorPosition(int x, int y);
	
	/**
	 * Swaps the 2 blocks indicated by the cursor
	 */
	public void swap();
}

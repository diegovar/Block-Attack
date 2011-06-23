package com.blockattack.objects;

public class CellPosition {
	public int row;
	public int col;
	
	public CellPosition(int r, int c) {
		row = r;
		col = c;
	}
	
	public CellPosition clone() {
		return new CellPosition(row, col);
	}
}

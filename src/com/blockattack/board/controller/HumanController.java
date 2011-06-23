package com.blockattack.board.controller;

import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.input.touch.TouchEvent;

import com.blockattack.board.Board;
import com.blockattack.board.BoardState;
import com.blockattack.objects.Block;
import com.blockattack.objects.CellPosition;

public class HumanController extends Entity implements IBoardController {

	private float lastDownX;
	private float lastDownY;
	
	public HumanController() {
	}

	public boolean onTouch(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY, Board board) {
		BoardState state = board.getState();
		
		if(pSceneTouchEvent.isActionDown() && state.selectedBlocks.isEmpty()) {
			CellPosition touchedPos = new CellPosition(board.getRowIndexFromPosition(pTouchAreaLocalY), (int)pTouchAreaLocalX / Block.width);
			
			if(touchedPos.row < state.blocks.size()) { //touched an existing row
				Block blockTouched = state.blocks.get(touchedPos.row).get(touchedPos.col);
				
				if(blockTouched != null) { //the block space is not empty
					lastDownX = pTouchAreaLocalX;
					lastDownY = pTouchAreaLocalY;
					board.setBlockSelected(blockTouched);
					return true;
				}
			}
		} else {
			if(state.selectedBlocks.size() == 1 && pSceneTouchEvent.isActionUp()) {
				int direction = (int)Math.signum(pTouchAreaLocalX - lastDownX);
				Block firstBlock = state.selectedBlocks.get(0);
				if((int)pTouchAreaLocalX / Block.width != firstBlock.position.col && //make sure we didn't touch and release in the same block
					 !(direction == 1 && firstBlock.position.col == state.blocksPerRow - 1 || //we're not at the end
				     direction == -1 && firstBlock.position.col == 0)) { //or the beginning of the row
					//swap in lists
					Block secondBlock = state.blocks.get(firstBlock.position.row).get(firstBlock.position.col + direction);
					if(firstBlock.isSwappable() && (secondBlock == null || secondBlock.isSwappable())) {
						board.swapBlocks(firstBlock.position.clone(), new CellPosition(firstBlock.position.row, firstBlock.position.col + direction));
					}
				}
				firstBlock.setScale(1.0f);
				state.selectedBlocks.clear();
				return true;
			}
		}
		return false;
	}

	public void reset() {
		
	}

}

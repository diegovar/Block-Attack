package com.blockattack.board.controller;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.input.touch.TouchEvent;

import com.blockattack.board.Board;


public interface IBoardController extends IEntity {
	public boolean onTouch(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY, Board board);
	public void reset();
}

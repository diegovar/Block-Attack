package com.blockattack.objects;

import org.anddev.andengine.entity.sprite.Sprite;

import android.R.bool;

import com.blockattack.BlockAttackActivity;


public class Block extends Sprite {
	
	public int type;
	
	public static int width = 64;
	public static int height = 64;
	public static int nTypes = 4;
	public CellPosition position;
	private boolean selected = false;
	private boolean falling = false;
	private boolean swappable = true;
	private boolean disappearing = false;
	
	public Block(float pX, float pY, int type, CellPosition pos) {
		super(pX, pY, BlockAttackActivity.getInstance().textureRegions.get(blockNumberToTextureRegionId(type)));
		this.type = type;
		this.position = pos;
	}
	
	public static int blockNumberToTextureRegionId(int n) {
		switch (n) {
		case 0:
			return TextureRegionIds.block0.ordinal();
		case 1:
			return TextureRegionIds.block1.ordinal();
		case 2:
			return TextureRegionIds.block2.ordinal();
		case 3:
			return TextureRegionIds.block3.ordinal();
		case 4:
			return TextureRegionIds.block4.ordinal();
		case 5:
			return TextureRegionIds.block5.ordinal();
		default:
			return -1;
		}
	}
	
	public void setSelected(boolean s) {
		selected = s;
		if(s) {
			this.setScale(1.2f);
			this.setZIndex(1);
		} else {
			this.setScale(1.0f);
			this.setZIndex(0);
		}
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setFalling(boolean f) {
		this.falling = f;
	}
	
	public boolean isFalling() {
		return falling;
	}
	
	public Block clone() {
		return new Block(getX(), getY(), type, position);
	}
	
	public boolean isSwappable() {
		return swappable;
	}
	
	public void setSwappable(boolean s) {
		swappable = s;
	}
	
	public boolean isDisappearing() {
		return disappearing;
	}
	
	public void setDisappearing(boolean d) {
		disappearing = d;
	}
}

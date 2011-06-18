package com.blockattack.objects;

import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

import com.blockattack.BlockAttackActivity;
import com.blockattack.R;


public class Block extends Sprite {
	
	public int type;
	
	public static int width = 64;
	public static int height = 64;
	public static int nTypes = 4;
	public int currentRow;
	public int currentCol;
	
	public Block(float pX, float pY, int type, int row, int col) {
		super(pX, pY, BlockAttackActivity.getInstance().textureRegions.get(blockNumberToTextureRegionId(type)));
		this.type = type;
		this.currentRow = row;
		this.currentCol = col;
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
}

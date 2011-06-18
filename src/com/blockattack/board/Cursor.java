package com.blockattack.board;

import org.anddev.andengine.entity.sprite.TiledSprite;

import com.blockattack.BlockAttackActivity;
import com.blockattack.objects.TextureRegionIds;

public class Cursor extends TiledSprite{

	public boolean isDefined;
	public int pX;
	public int pY;
	
	public Cursor() {
		super(0,0,BlockAttackActivity.getInstance().textureRegions.getTiled(TextureRegionIds.cursor.ordinal()));
		this.setVisible(false);
	}
}

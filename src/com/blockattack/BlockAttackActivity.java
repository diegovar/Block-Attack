package com.blockattack;

import java.util.Random;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.FixedStepEngine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegionLibrary;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import com.blockattack.board.Board;
import com.blockattack.objects.Block;
import com.blockattack.objects.TextureRegionIds;

public class BlockAttackActivity extends BaseGameActivity {
    
	private int CAMERA_WIDTH = 480;//getResources().getDimensionPixelSize(R.dimen.cameraWidth);
	private int CAMERA_HEIGHT = 800;//getResources().getDimensionPixelSize(R.dimen.cameraHeight);
	private int STEPS_PER_SECOND = 60;//getResources().getInteger(R.integer.stepsPerSecond);
	
	public final Random random = new Random();
	
	private Scene mainScene;
	private Camera mainCamera;
	public TextureRegionLibrary textureRegions = new TextureRegionLibrary(20);
	
	/** Singleton **/
	private static BlockAttackActivity activity;
	
	public static BlockAttackActivity getInstance() {
		if(activity == null)
			activity = new BlockAttackActivity();
		return activity;
	}
	
	/** Constructor **/
	public BlockAttackActivity() {
		super();
		BlockAttackActivity.activity = this;
	}

	
	public Engine onLoadEngine() {
		
		mainCamera = new Camera(0,0,CAMERA_WIDTH, CAMERA_HEIGHT);
		
		return new FixedStepEngine(new EngineOptions(true, ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mainCamera), STEPS_PER_SECOND);
	}

	public void onLoadResources() {
		Texture blockTexture = new Texture(256,64);
		
		TextureRegionFactory.createFromAsset(blockTexture, this, "gfx/blocks/blocks.png", 0, 0);
		for (int i = 0; i < Block.nTypes; i++) {
			textureRegions.put(Block.blockNumberToTextureRegionId(i), TextureRegionFactory.extractFromTexture(blockTexture, i*Block.width, 0, Block.width, Block.height));
		}
		
		Texture cursorTexture = new Texture(128,64);
		
		textureRegions.put(TextureRegionIds.cursor.ordinal(), TextureRegionFactory.createTiledFromAsset(cursorTexture, this, "gfx/cursor.png", 0, 0, 1, 1));
		
		getEngine().getTextureManager().loadTexture(blockTexture);
		getEngine().getTextureManager().loadTexture(cursorTexture);
	}

	public Scene onLoadScene() {
		mainScene = new Scene();
		mainScene.setBackground(new ColorBackground(0.01f, 0.02f, 0.2f));
		
		int speed = 320;
		int rowSize = 6;//getResources().getInteger(R.integer.rowSize);
		int leftMargin = (CAMERA_WIDTH - rowSize * Block.width)/2; 
		
		Board b = new Board(null, speed, rowSize, 0, 0, rowSize * Block.width, CAMERA_HEIGHT, mainScene);
		Entity boardContainer = new Entity(leftMargin, 0);
		boardContainer.attachChild(b);
		mainScene.attachChild(boardContainer);
		
		this.getEngine().registerUpdateHandler(boardContainer);
		
		//mainScene.setTouchAreaBindingEnabled(true);
		
		return mainScene;
	}

	public void onLoadComplete() {
		
	}
}
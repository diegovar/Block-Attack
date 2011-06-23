package com.blockattack;

import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.FixedStepEngine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegionLibrary;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.view.KeyEvent;

import com.blockattack.board.Board;
import com.blockattack.board.controller.HumanController;
import com.blockattack.objects.Block;
import com.blockattack.objects.TextureRegionIds;

public class BlockAttackActivity extends BaseGameActivity implements IOnMenuItemClickListener {
    
	private int CAMERA_WIDTH = 480;//getResources().getDimensionPixelSize(R.dimen.cameraWidth);
	private int CAMERA_HEIGHT = 800;//getResources().getDimensionPixelSize(R.dimen.cameraHeight);
	private int STEPS_PER_SECOND = 60;//getResources().getInteger(R.integer.stepsPerSecond);
	
	private static final int MENU_RESET = 0;
	private static final int MENU_QUIT = 1;
	
	public final Random random = new Random();
	
	private Scene mainScene;
	private MenuScene menuScene;
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
		
		Texture menuTexture = new Texture(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		textureRegions.put(TextureRegionIds.menu_reset.ordinal(), TextureRegionFactory.createFromAsset(menuTexture, this, "gfx/menu_reset.png", 0, 0));
		textureRegions.put(TextureRegionIds.menu_quit.ordinal(), TextureRegionFactory.createFromAsset(menuTexture, this, "gfx/menu_quit.png", 0, 50));
		
		getEngine().getTextureManager().loadTexture(blockTexture);
		getEngine().getTextureManager().loadTexture(cursorTexture);
		getEngine().getTextureManager().loadTexture(menuTexture);
	}

	public Scene onLoadScene() {
		
		//Create menu scene
		menuScene = new MenuScene(mainCamera);
		
		final SpriteMenuItem resetMenuItem = new SpriteMenuItem(MENU_RESET, textureRegions.get(TextureRegionIds.menu_reset.ordinal()));
		resetMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(resetMenuItem);

		final SpriteMenuItem quitMenuItem = new SpriteMenuItem(MENU_QUIT, textureRegions.get(TextureRegionIds.menu_quit.ordinal()));
		quitMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(quitMenuItem);

		menuScene.buildAnimations();

		menuScene.setBackgroundEnabled(false);

		menuScene.setOnMenuItemClickListener(this);
		
		
		//Create main scene
		mainScene = new Scene();
		mainScene.setBackground(new ColorBackground(0.01f, 0.02f, 0.2f));
		
		int speed = 320;
		int rowSize = 6;//getResources().getInteger(R.integer.rowSize);
		int leftMargin = (CAMERA_WIDTH - rowSize * Block.width)/2; 
		int maxRows = 10;
		
		Board b = new Board(new HumanController(), speed, rowSize, maxRows, 0, 0);
		Entity boardContainer = new Entity(leftMargin, CAMERA_HEIGHT - maxRows * Block.height);
		boardContainer.attachChild(b);
		mainScene.attachChild(boardContainer);
		mainScene.registerTouchArea(b);
		
		this.getEngine().registerUpdateHandler(boardContainer);
		
		mainScene.setTouchAreaBindingEnabled(true);
		
		return mainScene;
	}

	public void onLoadComplete() {
		
	}
	
	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
			if(mainScene.hasChildScene()) {
				/* Remove the menu and reset it. */
				menuScene.back();
			} else {
				/* Attach the menu. */
				mainScene.setChildScene(menuScene, false, true, true);
			}
			return true;
		} else {
			return super.onKeyDown(pKeyCode, pEvent);
		}
	}

	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
			float pMenuItemLocalX, float pMenuItemLocalY) {
		switch(pMenuItem.getID()) {
		case MENU_RESET:

			mainScene.reset();
			

			/* Remove the menu and reset it. */
			mainScene.clearChildScene();
			menuScene.reset();
			return true;
		case MENU_QUIT:
			/* End Activity. */
			this.finish();
			return true;
		default:
			return false;
	}
	}
}
package com.sean.basketball;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

class Basketball extends ApplicationAdapter implements GestureDetector.GestureListener {
	private SpriteBatch batch;
	private Texture ballImg;
	private Texture rimImg;
	private Texture backboardImg;
	private World world;
	private OrthographicCamera camera;
	private Body ball;
	private Body leftRim;
	private Body rightRim;
	private Body backboard;
	private BitmapFont font;
	private OrthographicCamera uiCamera;

	private final float ballRadius = 0.4f;
	private final float rimRadius = 0.125f;
	private final float backboardHeight = 3.125f;
	private final float leftRimX = 12.25f;
	private final float rightRimX = 13.75f;
	private final float rimHeight = 5f;

	private boolean isP1Turn;
	private int p1Score;
	private int p2Score;
	private boolean scored;
	private float shotFrom;
	private boolean won;
	
	@Override
	public void create() {
		initAssets();
		initBall();
		initRims();
		initBackboard();
		initScoreboard();
	}

	@Override
	public void render() {
		update();
		moveBall();
	}
	
	@Override
	public void dispose() {
		batch.dispose();
		ballImg.dispose();
		backboardImg.dispose();
		rimImg.dispose();
		world.dispose();
		font.dispose();
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		return false;
	}

	@Override
	public boolean longPress(float x, float y) {
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		velocityX /= 25f;
		velocityY /= -25f;
//		Threshold for velocity is set at 110, if total velocity is above 110 then scale it down
		if (velocityX * velocityX + velocityY * velocityY > 14400) {
			double angle = Math.tan(velocityX / velocityY);
			velocityX = (float) (Math.asin(angle) * 120f);
			velocityY = (float) (Math.acos(angle) * 120f);
		}
		if (!ball.isAwake() && velocityX > 0 && velocityY > 0) {
			ball.setAwake(true);
			ball.applyForceToCenter(velocityX, velocityY, true);
//			cheats
//			ball.applyForceToCenter(70f, 100f, true);
			shotFrom = ball.getPosition().x;
		}
		return true;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		return false;
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		return false;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		return false;
	}

	@Override
	public void pinchStop() {

	}

	private void initAssets() {
		batch = new SpriteBatch();
		ballImg = new Texture("disk-blue.png");
		rimImg = new Texture("disk-red.png");
		backboardImg = new Texture("red.jpg");
		world = new World(new Vector2(0, -9.8f), true);
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 16, 8);
		uiCamera = new OrthographicCamera();
		uiCamera.setToOrtho(false, 640, 320);
		GestureDetector gestureDetector = new GestureDetector(this);
		Gdx.input.setInputProcessor(gestureDetector);
	}

	private void initBall() {
		BodyDef ballDef = new BodyDef();
		ballDef.type = BodyDef.BodyType.DynamicBody;
		ballDef.position.set(1f, 1f);

		CircleShape circle = new CircleShape();
		circle.setRadius(ballRadius);

		ball = world.createBody(ballDef);
		FixtureDef ballFixtureDef = new FixtureDef();
		ballFixtureDef.shape = circle;
		ballFixtureDef.density = 0.3f;
		ballFixtureDef.friction = 0.1f;
		ballFixtureDef.restitution = 0.6f;
		ball.createFixture(ballFixtureDef);
		ball.setAwake(false);
	}

	private void initRims() {
		BodyDef leftRimDef = new BodyDef();
		leftRimDef.position.set(leftRimX, rimHeight);
		BodyDef rightRimDef = new BodyDef();
		leftRimDef.type = BodyDef.BodyType.StaticBody;
		rightRimDef.position.set(rightRimX, rimHeight);
		leftRim = world.createBody(leftRimDef);
		rightRim = world.createBody(rightRimDef);
		rightRimDef.type = BodyDef.BodyType.StaticBody;

		CircleShape circleRim = new CircleShape();
		circleRim.setRadius(rimRadius);

		leftRim.createFixture(circleRim, 0.0f);
		rightRim.createFixture(circleRim, 0.0f);
	}

	private void initBackboard() {
		BodyDef backboardDef = new BodyDef();
		backboardDef.position.set(rightRimX + rimRadius, backboardHeight);
		backboardDef.type = BodyDef.BodyType.StaticBody;
		backboard = world.createBody(backboardDef);
		PolygonShape backboardBox = new PolygonShape();
		backboardBox.setAsBox(rimRadius, backboardHeight);
		backboard.createFixture(backboardBox, 0.0f);
	}

	private void initScoreboard() {
		isP1Turn = true;
		p1Score = 0;
		p2Score = 0;
		font = new BitmapFont();
		setFontBlack();
		font.getData().setScale(1f);
		scored = false;
		shotFrom = 1f;
		won = false;
	}

	private void update() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		if (won) {
			batch.setProjectionMatrix(uiCamera.combined);
			font.getData().setScale(3f);
			font.draw(batch, "Player " + (p1Score > p2Score ? 1 : 2) + " wins!", 180f, 180f);
			font.getData().setScale(1f);
		}
		else {
			batch.draw(ballImg, ball.getPosition().x - ballRadius, ball.getPosition().y - ballRadius,
					2 * ballRadius, 2 * ballRadius);
			batch.draw(backboardImg, backboard.getPosition().x - rimRadius, 0, 2 * rimRadius,
					backboard.getPosition().y + backboardHeight);
			batch.draw(backboardImg, leftRim.getPosition().x - rimRadius, leftRim.getPosition().y - rimRadius,
					rightRim.getPosition().x - leftRim.getPosition().x + 2 * rimRadius, 2 * rimRadius);
			drawScoreboard();
		}
		batch.end();
	}

	private void drawScoreboard() {
		batch.setProjectionMatrix(uiCamera.combined);
		if (isP1Turn) {
			setFontRed();
			font.draw(batch, "Player 1: " + p1Score, 30f, 300f);
			setFontBlack();
			font.draw(batch, "Player 2: " + p2Score, 30f, 275f);
			return;
		}
		setFontBlack();
		font.draw(batch, "Player 1: " + p1Score, 30f, 300f);
		setFontRed();
		font.draw(batch, "Player 2: " + p2Score, 30f, 275f);
	}

	private void setFontRed() {
		font.setColor(1f, 0.1f, 0.1f, 1f);
	}

	private void setFontBlack() {
		font.setColor(0f, 0f, 0f, 1f);
	}

	private void moveBall() {
		if (won) {
			if (Gdx.input.isTouched()) {
				isP1Turn = true;
				p1Score = 0;
				p2Score = 0;
				won = false;
				scored = false;
				shotFrom = 1f;
				ball.setAwake(false);
				ball.setTransform(1f, 1f, 0);
				return;
			}
		}
		float ballX = ball.getPosition().x;
		float ballY = ball.getPosition().y;
		if (!ball.isAwake()) {
			float tilt = Gdx.input.getAccelerometerY();
			if (tilt > 0 && ballX < 7.5f || tilt < 0 && ballX > 1) {
				ball.setAwake(false);
				ball.setTransform(ballX + tilt / 50f, ballY, 0);
			}
		}
		else if (ballX < 0 || ballX > 16 || ballY < 0) {
			if (scored) {
				int score = (int) (leftRimX - shotFrom - 1);
				if (isP1Turn) p1Score += score;
				else p2Score += score;
			}
			if (p1Score >= 30 || p2Score >= 30) {
				won = true;
			}
			isP1Turn = !isP1Turn;
			scored = false;
			shotFrom = 1f;
			ball.setAwake(false);
			ball.setTransform(1f, 1f, 0);
		}
		else {
//			scored
			if (!scored && ballX >= leftRimX && ballX <= rightRimX &&
					ballY <= rimHeight && ballY >= rimHeight - 0.05) {
				scored = true;
			}
			world.step(1/60f, 100, 100);
		}
	}
}

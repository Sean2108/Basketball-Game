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

/**
 * @author sean
 */
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

	/**
	 * create assets and objects
	 */
	@Override
	public void create() {
		initAssets();
		initBall();
		initRims();
		initBackboard();
		initScoreboard();
	}

	/**
	 * render the objects on to the screen, will be called every step
	 */
	@Override
	public void render() {
		update();
		moveBall();
	}

	/**
	 * garbage collector, dispose of unused assets
	 */
	@Override
	public void dispose() {
		batch.dispose();
		ballImg.dispose();
		backboardImg.dispose();
		rimImg.dispose();
		world.dispose();
		font.dispose();
	}

	/**
	 * creates batch, textures to impose on to the objects,
	 * world, camera for gameplay that uses a realistic metric scale
	 * and uiCamera for pixel scale displaying of text on screen,
	 * and a gesture detector to detect fling motions
	 */
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

	/**
	 * initialize ball as dynamic body, setting radius and other properties.
	 * initially ball is not awake until a force is applied on it (so that gravity
	 * does not act on it yet)
	 */
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

	/**
	 * initialize 2 rims, leftRim and rightRim, which will
	 * not be visible in the game world but be hidden underneath
	 * a horizontal line representing the rim. Since it is a 2D game,
	 * leftRim and rightRim represent the front and back of the rim
	 * respectively. The rims are static and will not move, but can
	 * interact with the ball.
	 */
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

	/**
	 * initialize backboard. Backboard is static and will not move,
	 * but will interact with the ball.
	 */
	private void initBackboard() {
		BodyDef backboardDef = new BodyDef();
		backboardDef.position.set(rightRimX + rimRadius, backboardHeight);
		backboardDef.type = BodyDef.BodyType.StaticBody;
		backboard = world.createBody(backboardDef);
		PolygonShape backboardBox = new PolygonShape();
		backboardBox.setAsBox(rimRadius, backboardHeight);
		backboard.createFixture(backboardBox, 0.0f);
	}

	/**
	 * initialize scoreboard, setting scores to 0 and setting p1's turn.
	 * creates font to display scores.
	 */
	private void initScoreboard() {
		resetGame();
		font = new BitmapFont();
		setFontBlack();
		font.getData().setScale(1f);
	}

	/**
	 * draws sprites and objects on the screen, with a white background
	 */
	private void update() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		if (won) {
			drawWon();
		}
		else {
			drawAssets();
		}
		batch.end();
	}

	/**
	 * draw victory message if a player has score >= 30
	 */
	private void drawWon() {
		batch.setProjectionMatrix(uiCamera.combined);
		font.getData().setScale(3f);
		font.draw(batch, "Player " + (p1Score > p2Score ? 1 : 2) + " wins!", 180f, 180f);
		font.getData().setScale(1f);
	}

	/**
	 * draw ball, rim, backboard and scoreboard
	 */
	private void drawAssets() {
		batch.draw(ballImg, ball.getPosition().x - ballRadius, ball.getPosition().y - ballRadius,
				2 * ballRadius, 2 * ballRadius);
		batch.draw(backboardImg, backboard.getPosition().x - rimRadius, 0, 2 * rimRadius,
				backboard.getPosition().y + backboardHeight);
		batch.draw(backboardImg, leftRim.getPosition().x - rimRadius, leftRim.getPosition().y - rimRadius,
				rightRim.getPosition().x - leftRim.getPosition().x + 2 * rimRadius, 2 * rimRadius);
		drawScoreboard();
	}

	/**
	 * if the player's turn is active, his score will be displayed in red, else black
	 */
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

	/**
	 * changes font color to red (signifies active turn)
	 */
	private void setFontRed() {
		font.setColor(1f, 0.1f, 0.1f, 1f);
	}

	/**
	 * changes font color to black (signifies inactive turn)
	 */
	private void setFontBlack() {
		font.setColor(0f, 0f, 0f, 1f);
	}

	/**
	 * checks situation and calls appropriate method.
	 * if a player touches the screen after win, reset game.
	 * if the ball is not in play, allow lateral movement of ball through tilting.
	 * check for ball out of bounds and mark whether player has scored.
	 * advance the game world by 1 step.
	 */
	private void moveBall() {
		if (won) {
			if (Gdx.input.isTouched()) {
				resetGame();
				return;
			}
		}
		float ballX = ball.getPosition().x;
		float ballY = ball.getPosition().y;
		if (!ball.isAwake()) {
			setBallPos(ballX, ballY);
		}
		else if (ballX < 0 || ballX > 16 || ballY < 0) {
			ballOutOfBounds();
		}
		else {
			if (!scored && ballX >= leftRimX && ballX <= rightRimX &&
					ballY <= rimHeight && ballY >= rimHeight - 0.2f) {
				scored = true;
			}
			world.step(1/60f, 100, 100);
		}
	}

	/**
	 * reset ball to starting position
	 */
	private void reset() {
		scored = false;
		shotFrom = 1f;
		ball.setAwake(false);
		ball.setTransform(1f, 1f, 0);
	}

	/**
	 * reset scoreboard and ball
	 */
	private void resetGame() {
		isP1Turn = true;
		p1Score = 0;
		p2Score = 0;
		won = false;
		reset();
	}

	/**
	 * detects tilt and moves ball
	 * @param ballX initial position of ball in x axis
	 * @param ballY initial position of ball in y axis
	 */
	private void setBallPos(float ballX, float ballY) {
		float tilt = Gdx.input.getAccelerometerY();
		if (tilt > 0 && ballX < 7.5f || tilt < 0 && ballX > 1) {
			ball.setAwake(false);
			ball.setTransform(ballX + tilt / 25f, ballY, 0);
		}
	}

	/**
	 * add score to player if the player scored, check if
	 * any player has won, then reset the ball to starting position.
	 * score is calculated linearly based on distance from rim.
	 * max score (max distance) = 10, min score (nearest possible distance
	 * from rim) = 3
	 */
	private void ballOutOfBounds() {
		if (scored) {
			int score = (int) (leftRimX - shotFrom - 1);
			if (isP1Turn) p1Score += score;
			else p2Score += score;
		}
		if (p1Score >= 30 || p2Score >= 30) {
			won = true;
		}
		isP1Turn = !isP1Turn;
		reset();
	}

	/**
	 * listens for fling action
	 * @param velocityX velocity of player's fling in x axis
	 * @param velocityY velocity of player's fling in y axis
	 * @param button unused
	 * @return unused
	 */
	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		velocityX /= 40f;
		velocityY /= -40f;
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
}

package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class Game extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	Texture playerImageRight;
	Texture playerImageLeft;
	Texture buddy;
	TiledMap map;
	TiledMapRenderer mapRenderer;
	private BitmapFont font;
	private OrthographicCamera camera;
	private World world;
	private Box2DDebugRenderer debugRenderer;
	private Slime player;
	private boolean jump = false;
	private boolean held = false;
	private boolean debug = false;
	private float power = 0.8f;
	private boolean falling = false;
	private Sound jumpSound;
	private Sound splat;
	private boolean gameOver;
	final float ppm = 32f;

	private Preferences prefs;

	@Override
	public void create () {
		world = new World(new Vector2(0, -10f), false);
		debugRenderer = new Box2DDebugRenderer();
		world.setContactListener(new ListenerClass());

		prefs = Gdx.app.getPreferences("My Preferences");

		this.player = new Slime(world, "slime", prefs.getFloat("playerx", 250) / ppm, prefs.getFloat("playery", 24) / ppm);

		jumpSound = Gdx.audio.newSound(Gdx.files.internal("146245148.mp3"));
		splat = Gdx.audio.newSound(Gdx.files.internal("108384389.mp3"));

		font = new BitmapFont();

		batch = new SpriteBatch();
		img = new Texture("wp7752490.jpg");
		buddy = new Texture("buddy.png");
		playerImageRight = new Texture("slime.png");
		playerImageLeft = new Texture("slime_but_left.png");
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 512/ppm, 288/ppm);
		map = new TmxMapLoader().load("test.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / ppm);

		camera.position.y = prefs.getFloat("cameray", 4.5f);
		camera.position.x = prefs.getFloat("camerax", 8);

		BodyDef bdef = new BodyDef();
		PolygonShape shape = new PolygonShape();
		FixtureDef fdef = new FixtureDef();
		Body body;
		EdgeShape es = new EdgeShape();

		for (MapObject object: map.getLayers().get(1).getObjects().getByType(RectangleMapObject.class)) {
			Rectangle rect = ((RectangleMapObject) object).getRectangle();

			bdef.type = BodyDef.BodyType.StaticBody;
			bdef.position.set((rect.getX() + rect.getWidth() / 2)/ppm, (rect.getY() + rect.getHeight() / 2)/ppm);

			body = world.createBody(bdef);

			shape.setAsBox((rect.getWidth() / 2)/ppm, (rect.getHeight() / 2)/ppm);
			fdef.shape = shape;
			fdef.restitution = 0f;
			fdef.friction = 10f;
			body.createFixture(fdef);
		}

		//Bouncy walls
		for (MapObject object: map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
			Rectangle rect = ((RectangleMapObject) object).getRectangle();

			bdef.type = BodyDef.BodyType.StaticBody;
			bdef.position.set((rect.getX() + rect.getWidth() / 2)/ppm, (rect.getY() + rect.getHeight() / 2)/ppm);

			body = world.createBody(bdef);

			es.set(0, -rect.getHeight()/ppm/2f + 1/ppm, 0, rect.getHeight()/ppm/2f - 1/ppm);
			fdef.shape = es;
			fdef.restitution = 0.8f;
			fdef.friction = 0f;
			body.createFixture(fdef);
		}
	}

	public void input(float delta) {
		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			player.setRight(false);
		}
		else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			player.setRight(true);
		}
		else if (Gdx.input.isKeyJustPressed(Keys.G)) {
			player.getBody().setTransform(player.getBody().getPosition().x + 0.1f, player.getBody().getPosition().y + 0.1f, 0);
		}
		else if (Gdx.input.isKeyJustPressed(Keys.H)) {
			player.getBody().setTransform(player.getBody().getPosition().x - 0.1f, player.getBody().getPosition().y + 0.1f, 0);
		}
		if (Gdx.input.isKeyJustPressed(Keys.D)) {
			debug = !debug;
		}
		if (Gdx.input.isKeyJustPressed(Keys.R)) {
			restart();
		}
		if (Gdx.input.isKeyJustPressed(Keys.Q)) {
			prefs.putFloat("playerx", player.getBody().getPosition().x*ppm);
			prefs.putFloat("playery", player.getBody().getPosition().y*ppm);
			prefs.putFloat("camerax", camera.position.x);
			prefs.putFloat("cameray", camera.position.y);
			prefs.flush();
			Gdx.app.exit();
		}
		if (player.getBody().getLinearVelocity().y < -10f) {falling = true;}
		float hForce = 0.0f;
		if (player.getGrounded() && player.getBody().getLinearVelocity().y == 0f && player.getBody().getLinearVelocity().x == 0f) {
			if (falling) {
				splat.play(0.02f);
				falling = false;
			}
			if (!jump) {
				if (Gdx.input.isKeyPressed(Keys.SPACE)) {
					held = true;
					power += 0.04f;
					//System.out.println(power);
				} else if (held && !Gdx.input.isKeyPressed(Keys.SPACE)) {
					held = false;
					jump = true;
				}
				if (power > 3.2f) {
					power = 3.2f;
					held = false;
					jump = true;
				}
				return;
			}
			if (jump) {
				if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
					hForce += 0.2f;
				}
				if (Gdx.input.isKeyPressed(Keys.LEFT)) {
					hForce -= 0.2f;
				}
				jump = false;
				//inAir = true;
				player.getBody().applyLinearImpulse(new Vector2(hForce * (power*1.5f + 1), power), player.getBody().getWorldCenter(),true);
				//System.out.println(power);
				jumpSound.play(0.02f);
			}
		}
		else {
			power = 0.8f;
		}
	}

	public void end() {
		prefs.putFloat("playerx", 250);
		prefs.putFloat("playery", 24);
		prefs.putFloat("camerax", 8);
		prefs.putFloat("cameray", 4.5f);
		if (Gdx.input.isKeyJustPressed(Keys.R)) {
			restart();
		}
		else if (Gdx.input.isKeyJustPressed(Keys.Q)) {
			prefs.flush();
			Gdx.app.exit();
		}
	}

	public void update(float delta) {
		if (!gameOver) {
			world.step(1 / 60f, 6, 2);

			input(delta);

			if (player.getBody().getPosition().y > 49 && player.getGrounded()) {
				gameOver = true;
			}
			if (player.getBody().getPosition().y > camera.position.y + 4.5) {
				camera.position.y += 9;
			} else if (player.getBody().getPosition().y < camera.position.y - 4.5) {
				camera.position.y -= 9;
			}
			camera.update();
		}
		else end();
	}

	@Override
	public void render () {
		float scale = Gdx.graphics.getWidth() / 512f;
		System.out.println(scale);
		update(Gdx.graphics.getDeltaTime());
		ScreenUtils.clear(1, 0, 0, 1);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
		mapRenderer.setView(camera);
		mapRenderer.render();
		batch.begin();
		Texture playerImage;
		if (player.isRight()) {
			playerImage = playerImageRight;
		} else {
			playerImage = playerImageLeft;
		}
		batch.draw(playerImage, player.getBody().getPosition().x * ppm * scale - playerImage.getWidth() / 2, player.getBody().getPosition().y * ppm * scale - playerImage.getHeight() / 2 - (camera.position.y - 4.5f) * ppm * scale, Gdx.graphics.getWidth() * 90 / 1920, Gdx.graphics.getHeight() * 61 / 1080);
		if (player.getBody().getPosition().y > 45) {
			batch.draw(buddy, 13f * ppm * scale, 3.925f * ppm * scale);
		}
		batch.end();
		if (debug) {
			debugRenderer.render(world, camera.combined);
		}
		if (gameOver) {
			batch.begin();
			font.getData().setScale(2);
			font.draw(batch, "Congrats, Bozo", 7 * ppm * scale, 6f * ppm * scale);
			font.draw(batch, "Press r to restart", 4 * ppm * scale, 4.5f * ppm * scale);
			font.draw(batch, "Press q to exit app", 9 * ppm * scale, 4.5f * ppm * scale);
			batch.end();
		}
	}

	private void restart() {
		player.resetSlime(250/ppm, 24/ppm);
		camera.position.x = 8f;
		camera.position.y = 4.5f;
		gameOver = false;
		System.out.println("restart");
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
		playerImageLeft.dispose();
		playerImageRight.dispose();
	}
}

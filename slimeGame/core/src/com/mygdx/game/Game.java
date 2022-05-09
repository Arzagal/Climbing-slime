package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class Game extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	Texture playerImage;
	private OrthographicCamera camera;
	private World world;
	private Box2DDebugRenderer debugRenderer;
	private Body player;


	public Body createSlime() {
		// First we create a body definition
		Body body;
		BodyDef bodyDef = new BodyDef();
		// We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		// Set our body's starting position in the world
		bodyDef.position.set(450, 10);
		bodyDef.fixedRotation = true;
		body = world.createBody(bodyDef);

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(49,33);
		body.createFixture(shape, 1.0f);
		shape.dispose();
		return body;
	}

	public Body createBox(int x, int y, int width, int height)
	{
		Body body;
		BodyDef def = new BodyDef();

		def.type = BodyDef.BodyType.StaticBody;

		def.position.set(x,y);
		def.fixedRotation = true;
		body = world.createBody(def);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width/2, height/2);

		body.createFixture(shape, 1.0f);
		shape.dispose();
		return body;
	}

	@Override
	public void create () {
		world = new World(new Vector2(0, -9.800f), true);
		debugRenderer = new Box2DDebugRenderer();

		player = createSlime();
		Body ground = createBox(350,0,900,10);
		Body jump1 = createBox(400,500,400,200);

		batch = new SpriteBatch();
		img = new Texture("wp7752490.jpg");
		//player = new Rectangle(0, 0, 20, 20);
		playerImage = new Texture("slime.png");
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1600, 900);
	}

	public void input(float delta) {
		float hForce = 0.0f;
		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			hForce -= 100f;
		}
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			hForce += 100f;
		}
		if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			player.applyLinearImpulse(0f, 300000f, player.getPosition().x, player.getPosition().y, true);
		}
		player.setLinearVelocity(hForce, player.getLinearVelocity().y);
	}

	public void update(float delta) {
		world.step(1/60f, 6, 2);

		input(delta);
	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 0, 0, 1);
		batch.begin();
		batch.draw(img, 0, 0);
		//batch.draw(playerImage, player.x, player.y);
		batch.end();
		debugRenderer.render(world, camera.combined);
		update(Gdx.graphics.getDeltaTime());
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
		playerImage.dispose();
	}
}

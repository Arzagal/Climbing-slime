package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.*;

public class Slime extends Sprite {
    public Body body;
    public String id;
    private boolean grounded;
    private float ppm = 32f;
    private boolean right;

    public Slime(World world, String id, float x, float y) {
        this.id = id;
        createSlime(world, x, y);
        setGrounded(false);
        this.right = true;
    }

    private void createSlime(World world, float x, float y) {
        BodyDef bodyDef = new BodyDef();
        // We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // Set our body's starting position in the world
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;
        this.body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(12f / ppm,7.8f / ppm);
        FixtureDef fixdef = new FixtureDef();
        fixdef.shape = shape;
        fixdef.isSensor = true;
        fixdef.restitution = 0f;
        fixdef.friction = 1f;
        this.body.createFixture(shape, 1f);
        this.body.createFixture(fixdef).setUserData(this);
        shape.dispose();
    }

    public Body getBody() {
        return this.body;
    }

    public boolean getGrounded() {
        return this.grounded;
    }

    public void setGrounded(boolean g) {
        this.grounded = g;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public void resetSlime(float x, float y) {
        this.body.setTransform(x, y, 0);
        this.body.setLinearVelocity(0,0);
    }
}

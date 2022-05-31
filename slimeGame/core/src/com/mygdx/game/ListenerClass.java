package com.mygdx.game;

import com.badlogic.gdx.physics.box2d.*;
import jdk.internal.platform.SystemMetrics;

public class ListenerClass implements ContactListener {
    @Override
    public void endContact(Contact contact) {
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();
        if (a.getUserData() instanceof Slime) {
            ((Slime) a.getUserData()).setGrounded(false);
        }
        else if (b.getUserData() instanceof Slime) {
            ((Slime) b.getUserData()).setGrounded(false);
        }
        //System.out.println("A collision ended");
        return;
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();
        System.out.println(a);
        System.out.println(b);
        if (a.getUserData() instanceof Slime) {
            ((Slime) a.getUserData()).setGrounded(true);
            System.out.println(((Slime) a.getUserData()).getBody().getPosition().y);
        }
        else if (b.getUserData() instanceof Slime) {
            ((Slime) b.getUserData()).setGrounded(true);
            System.out.println(((Slime) b.getUserData()).getBody().getPosition().y);
        }
        //System.out.println("A collision happened");
        return;
    }

}

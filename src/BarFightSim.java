import processing.core.*;
import toxi.physics.*;
import toxi.physics.behaviors.ParticleBehavior;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.VerletPhysics2D;
import toxi.physics2d.behaviors.AttractionBehavior;
import toxi.physics2d.behaviors.ParticleBehavior2D;
import toxi.geom.*;

public class BarFightSim extends PApplet {
	
	VerletPhysics2D physics;
	VerletParticle2D fighter1, fighter2;
	Vec2D fightLoc;
	float physicsDrag = 0.01f;
	
	boolean isFight = false;
	boolean isFightLocSet = false;
	
	float healthFighter1, healthFighter2;
	float fighterJitter = 0.1f;
	
	
	//play variables
	//fighter color
	int fighterColor = color(255,0,0);
	//ellipse radius
	float agentRadius = 13;
	
	
	public void setup()
	{
		size(500,500);
		smooth(4);
		
		physics = new VerletPhysics2D();
		physics.setWorldBounds(new Rect(0, 0, width, height));
		physics.setDrag(physicsDrag);
		
		fighter1 = new VerletParticle2D(new Vec2D(random(height),random(width)));
		fighter2 = new VerletParticle2D(new Vec2D(random(height),random(width)));
		physics.addBehavior(new AttractionBehavior(fighter1, agentRadius, -1.0f, fighterJitter));
		physics.addBehavior(new AttractionBehavior(fighter2, agentRadius, -1.0f, fighterJitter));
		
		physics.addParticle(fighter1);
		physics.addParticle(fighter2);
		
		
		
	}
	
	public void draw()
	{
		background(0);
		physics.update();
		
		//if there is no fight location yet - define a location by finding the the middle point between the two fighters.
		if (!isFightLocSet) {
			fightLoc = new Vec2D(fighter1.sub(fighter2)).scale(0.5f).add(fighter2);
			isFightLocSet = true;
			isFight = true;
			
			healthFighter1 = 100.0f;
			healthFighter2 = 100.0f;
		}
		
		if(isFight)
		{
			fill(255);
			rectMode(CENTER);
			rect(fightLoc.x, fightLoc.y, 10, 10);
//			println("fightLoc: "+fightLoc.x+", "+fightLoc.y);

			//add force to fightLoc
			fighter1.addForce(new Vec2D(fightLoc.sub(fighter1).scale(0.001f)));
			fighter2.addForce(new Vec2D(fightLoc.sub(fighter2).scale(0.001f)));
			
			//if fighter touch each other the fighter with the higher velocity loses no health
			if(fighter1.distanceTo(fighter2) < agentRadius)
			{
				float velF1 = fighter1.getVelocity().magnitude();
				float velF2 = fighter2.getVelocity().magnitude();
				
				if(velF1 > velF2){
					healthFighter2 -= 1;
				}else if(velF1 < velF2){
					healthFighter1 -= 1;
				}
				println("Figher 1: "+ healthFighter1 +"Fighter 2: "+ healthFighter2);
			}
		}
		
		fill(fighterColor);
		ellipse(fighter1.x, fighter1.y, agentRadius, agentRadius);
		fill(fighterColor,128);
		ellipse(fighter2.x, fighter2.y, agentRadius, agentRadius);
		
	}

}
import java.util.ArrayList;

import javax.swing.Box.Filler;

import controlP5.ControlP5;

import processing.core.*;
import toxi.physics.*;
import toxi.physics.behaviors.ParticleBehavior;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.VerletPhysics2D;
import toxi.physics2d.behaviors.AttractionBehavior;
import toxi.physics2d.behaviors.ParticleBehavior2D;
import toxi.geom.*;

@SuppressWarnings("serial")
public class BarFightSim extends PApplet {
	
	ControlP5 cp5;
	
	VerletPhysics2D physics;
	float physicsDrag = 0.01f;
	VerletParticle2D fighter1, fighter2;
	ArrayList<VerletParticle2D> spectators = new ArrayList<VerletParticle2D>();
	int spectatorAmount = 100;
	int spectatorColor = color(255,255,0);
	ArrayList<VerletParticle2D> intervener = new ArrayList<VerletParticle2D>();
	int intervenerAmount = 15;
	int intervenerColor = color(0,255,255);

	Vec2D fightLoc;
	float aggressionLevel;
	int lastHit;
	
	boolean isFight = false;
	boolean isFightLocSet = false;
	float dangerRadius = 100;
	int dangerRadiusColor = color(255,0,0);
	float spectatorRadius = 180;
	int spectatorRadiusColor = color(0,255,0);
	
	float healthFighter1, healthFighter2;
	float agressionLevelFighter1, agressionLevelFighter2; 
	float fighterJitter = 0.1f;
	int fighterColor01 = color(255,0,0);
	int fighterColor02 = color(255,166,0);
	
	float agentRadius = 13;

	
	
	public void setup()
	{
		size(500,500);
		smooth(4);
		
		cp5 = new ControlP5(this);
		  
		  // add a horizontal sliders, the value of this slider will be linked
		  // to variable 'sliderValue' 
		cp5.addSlider("spectatorAmount")
		     .setPosition(15,height-20)
		     .setRange(0,200)
		     ;
		cp5.addSlider("intervenerAmount")
	     .setPosition(15, height-30)
	     .setRange(0,30)
	     ;
		
		physics = new VerletPhysics2D();
		physics.setWorldBounds(new Rect(0, 0, width, height));
		physics.setDrag(physicsDrag);
		
		fighter1 = new VerletParticle2D(new Vec2D(random(height),random(width)));
		fighter2 = new VerletParticle2D(new Vec2D(random(height),random(width)));
		physics.addBehavior(new AttractionBehavior(fighter1, agentRadius, -1.0f, fighterJitter));
		physics.addBehavior(new AttractionBehavior(fighter2, agentRadius, -1.0f, fighterJitter));
		physics.addParticle(fighter1);
		physics.addParticle(fighter2);
		
		//set up spectators and add them to the 2Dphysics world
		for (int i = 0; i < spectatorAmount; i++) 
		{
			VerletParticle2D p = new VerletParticle2D(new Vec2D(random(height), random(width)));
			physics.addBehavior(new AttractionBehavior(p, agentRadius, -1.0f, 0));
			spectators.add(p);
			physics.addParticle(p);
		}
		
		//set up intervener and add them to the 2D physics world effective 
		for (int i = 0; i < intervenerAmount; i++) {
			VerletParticle2D p = new VerletParticle2D(new Vec2D(random(height), random(width)));
			physics.addBehavior(new AttractionBehavior(p, agentRadius, -1.0f, 0));
			intervener.add(p);
			physics.addParticle(p);
		}
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
			aggressionLevel = 50.0f;
		}
		
		//if we have a ongoing fight do this
		if(isFight)
		{
			//add force to fightLoc
			fighter1.addForce(new Vec2D(fightLoc.sub(fighter1).scale(0.001f)));
			fighter2.addForce(new Vec2D(fightLoc.sub(fighter2).scale(0.001f)));
			
			//if fighter touch each other the fighter with the higher velocity loses no health
			if(fighter1.distanceTo(fighter2) < agentRadius)
			{
				float velF1 = fighter1.getVelocity().magnitude();
				float velF2 = fighter2.getVelocity().magnitude();
				float damageOnHit = 2;
				
				if(velF1 > velF2){
					healthFighter2 -= damageOnHit;
				}else if(velF1 < velF2){
					healthFighter1 -= damageOnHit;
				}
				println("Figher 1: "+ healthFighter1 +" Fighter 2: "+ healthFighter2);
				
				//when the fighters touch the aggression level rises no matter what
				if(aggressionLevel < 100){
					aggressionLevel += 1;
				}
				
				//safe when the last hit was
				lastHit = frameCount;
				
				//if the health level of a fighter reaches 0 or below the fight is over. 
				if (healthFighter1 <= 0) {
					isFight = false;
					println("Fight is over fighter 2 wins.");
				} else if (healthFighter2 <= 0) {
					isFight = false;
					println("Fight is over fighter 1 wins.");
				}
			}
			
			//looks if the last hit was more than 60 frames away, if yes, reduce the afressionLevel
			if(frameCount-lastHit > 60 ){
				aggressionLevel -= 1;
				lastHit = frameCount;
			}
			
			if(aggressionLevel == 0){
				isFight = false;
			}
			
			//Spectator movement
			for (VerletParticle2D p : spectators) 
			{
				if(p.distanceTo(fightLoc) < dangerRadius)
				{
					//if inside danger zone add force away from the fightLoc
					p.addForce( p.sub(fightLoc).scale(0.05f * (1/p.distanceTo(fightLoc))));
				}else{
					//if outside the spectator zone add force to get close to it
					p.addForce( fightLoc.sub(p).scale(0.03f* (1/p.distanceTo(fightLoc))));
				}
				
			}
			
			//intervener behavior & movement
			//------------------------------
			
			//calculate best spectator positions
			
			
			for (VerletParticle2D p : intervener) 
			{
				if (p.distanceTo(fightLoc) > dangerRadius) {
					//force to get to the fight location
					p.addForce( fightLoc.sub(p).scale(0.03f* (1/p.distanceTo(fightLoc))));
				} else {
					//if inside the danger radius try to step between the to fighters by calculating the midelpoint between the fighter and going there.
					Vec2D tempFightLoc = new Vec2D(fighter1.sub(fighter2)).scale(0.5f).add(fighter2);
					p.addForce( tempFightLoc.sub(p).scale(0.03f* (1/p.distanceTo(tempFightLoc))));
				}
			}
			
			//draw fight location
			fill(255);
			rectMode(CENTER);
			rect(fightLoc.x, fightLoc.y, 10, 10);
			
			//draw danger and spectator radius
			noFill();
			stroke(dangerRadiusColor);
			ellipse(fightLoc.x, fightLoc.y, dangerRadius, dangerRadius);
			stroke(spectatorRadiusColor);
			ellipse(fightLoc.x, fightLoc.y, spectatorRadius, spectatorRadius);
		}
		
		//draw functions that whether there is a fight or not have to be drawn
		
		//draw fighters
		noStroke();
		fill(fighterColor01);
		ellipse(fighter1.x, fighter1.y, agentRadius, agentRadius);
		fill(fighterColor02);
		ellipse(fighter2.x, fighter2.y, agentRadius, agentRadius);
		
		//draw spectators
		fill(spectatorColor);
		for (VerletParticle2D p : spectators) 
		{
			noStroke();
			ellipse(p.x, p.y, agentRadius, agentRadius);
		}
		
		//draw intervener
		fill(intervenerColor);
		for (VerletParticle2D p : intervener) {
			noStroke();
			ellipse(p.x, p.y, agentRadius, agentRadius);
		}
		drawUI();
	}
	
	private void drawUI() {
		if (healthFighter1 >= 0) {
			fill(fighterColor01);
			text(healthFighter1, 0, 20);
			rect(30, 30, healthFighter1, 10);
		}
		
		if (healthFighter2 >= 0) {
		fill(fighterColor02);
		text(healthFighter2, width - 150, 20);
		rect(width - 100, 30, healthFighter2, 10);
		}
		
		//draw aggression level
		if(aggressionLevel >= 0){
			fill(240,140,131);
			text("Agression Level: "+aggressionLevel, width/2-50, 20);
			rect(width/2, 30, aggressionLevel, 10);
		}
		
		//draw help text
		fill(255);
		text("press 'r' to reset the fight.", width-200, height-10);
	}

	public void keyPressed(){
		if (keyPressed && key == 'r'){
			physics.particles.clear();
			spectators.clear();
			intervener.clear();
			isFightLocSet = false;
			setup();
		}
	}
	
	public static void main(String args[]){
      PApplet.main(new String[] { BarFightSim.class.getName() });
    }

}
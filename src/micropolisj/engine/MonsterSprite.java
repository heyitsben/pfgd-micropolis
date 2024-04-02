// This file is part of MicropolisJ.
// Copyright (C) 2013 Jason Long
// Portions Copyright (C) 1989-2007 Electronic Arts Inc.
//
// MicropolisJ is free software; you can redistribute it and/or modify
// it under the terms of the GNU GPLv3, with additional terms.
// See the README file, included in this distribution, for details.

package micropolisj.engine;

import static micropolisj.engine.TileConstants.*;

/**
 * Implements a monster (one of the Micropolis disasters).
 */
public class MonsterSprite extends Sprite
{
	int count;
	int soundCount;
	int destX;
	int destY;
	int origX;
	int origY;
	int step;
	boolean flag; //true if the monster wants to return home
	
	//BENNER: added rampaging variable to control whether a monster
	//goes directly to its destination or not
	boolean rampaging = true;
	
	//BENNER: records original frame value
	int origFrame;

	//GODZILLA FRAMES
	//   1...3 : northeast
	//   4...6 : southeast
	//   7...9 : southwest
	//  10..12 : northwest
	//      13 : north
	//      14 : east
	//      15 : south
	//      16 : west

	// movement deltas
	static int [] Gx = { 2, 2, -2, -2, 0 };
	static int [] Gy = { -2, 2, 2, -2, 0 };

	static int [] ND1 = {  0, 1, 2, 3 };
	static int [] ND2 = {  1, 2, 3, 0 };
	static int [] nn1 = {  2, 5, 8, 11 };
	static int [] nn2 = { 11, 2, 5,  8 };

	public MonsterSprite(Micropolis engine, int xpos, int ypos)
	{
		super(engine, SpriteKind.GOD);
		this.x = xpos * 16 + 8;
		this.y = ypos * 16 + 8;
		this.width = 48;
		this.height = 48;
		this.offx = -24;
		this.offy = -24;

		this.origX = x;
		this.origY = y;

		this.frame = xpos > city.getWidth() / 2 ?
			(ypos > city.getHeight() / 2 ? 10 : 7) :
			(ypos > city.getHeight() / 2 ? 1 : 4);
		System.out.println("frame: " + this.frame);
		this.origFrame = this.frame;

		this.count = 1000;
		CityLocation p = city.getLocationOfNuclearPower();
		//BENNER: sets destination if there is a nuclear power plant
		if (p != null) {
			this.destX = p.x * 16 + 8;
			this.destY = p.y * 16 + 8;
			this.flag = false;
			this.step = 1;
			rampaging = false;
		}
		//BENNER: sets location if there is not a nuclear power plant
		else {
			p = city.getLocationOfMaxPollution();
			this.destX = p.x;
			this.destY = p.y;
			this.flag = false;
			this.step = 1;
		}
	}

	@Override
	public void moveImpl()
	{
		if (this.frame == 0) {
			return;
		}

		if (soundCount > 0) {
			soundCount--;
		}

		int d = (this.frame - 1) / 3;   // basic direction
		int z = (this.frame - 1) % 3;   // step index (only valid for d<4)

		if (d < 4) { //turn n s e w
			assert step == -1 || step == 1;
			if (z == 2) step = -1;
			if (z == 0) step = 1;
			z += step;

			if (getDis(x, y, destX, destY) == 0) {
				//BENNER: replaces monster with RadMonster before removing it
				flag = true;
				System.out.println("reached nuclear tile");
				city.makeRadMonster(this.x/16, this.y/16, this.origX, this.origY, this.origFrame);
				this.frame = 0;
				return;
			}

			int c = getDir(x, y, destX, destY);
			c = (c - 1) / 2;   //convert to one of four basic headings
			assert c >= 0 && c < 4;
			
			//BENNER: monster only wanders if rampaging is true
			if (rampaging) {
				if ((c != d) && city.PRNG.nextInt(11) == 0) {
					// randomly determine direction to turn
					if (city.PRNG.nextInt(2) == 0) {
						z = ND1[d];
					}
					else {
						z = ND2[d];
					}
					d = 4;  //transition heading

					if (soundCount == 0) {
						city.makeSound(x/16, y/16, Sound.MONSTER);
						soundCount = 50 + city.PRNG.nextInt(101);
					}
				}
			}
			else {
				//BENNER: monster goes directly to target if rampaging
				d = c;
			}
		}
		else {
			assert this.frame >= 13 && this.frame <= 16;

			int z2 = (this.frame - 13) % 4;

			if (city.PRNG.nextInt(4) == 0) {
				int newFrame;
				if (city.PRNG.nextInt(2) == 0) {
					newFrame = nn1[z2];
				} else {
					newFrame = nn2[z2];
				}
				d = (newFrame-1) / 3;
				z = (newFrame-1) % 3;

				assert d < 4;
			}
			else {
				d = 4;
			}
		}

		this.frame = ((d * 3) + z) + 1;

		assert this.frame >= 1 && this.frame <= 16;

		this.x += Gx[d];
		this.y += Gy[d];

		if (this.count > 0) {
			this.count--;
		}

		int c = getChar(x, y);
		if (c == -1 ||
			(c == RIVER && this.count != 0 && false)
			) {
			this.frame = 0; //kill zilla
		}

		for (Sprite s : city.allSprites())
		{
			// if (checkSpriteCollision(s) && s.
			if (checkSpriteCollision(s) &&
				(s.kind == SpriteKind.AIR ||
				 s.kind == SpriteKind.COP ||
				 s.kind == SpriteKind.SHI ||
				 s.kind == SpriteKind.TRA)
				) {
				s.explodeSprite();
			}
		}

		destroyTile(x / 16, y / 16);
	}
}

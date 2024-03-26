// This file is part of MicropolisJ.
// Copyright (C) 2013 Jason Long
// Portions Copyright (C) 1989-2007 Electronic Arts Inc.
//
// MicropolisJ is free software; you can redistribute it and/or modify
// it under the terms of the GNU GPLv3, with additional terms.
// See the README file, included in this distribution, for details.

package micropolisj.engine;

/**
 * Enumeration of the various kinds of sprites that may appear in the city.
 */
public enum SpriteKind
{
	TRA(1,5),  //train
	COP(2,8),  //helicopter
	AIR(3,11), //airplane
	SHI(4,8),  //ship
	GOD(5,16), //monster
	TOR(6,3),  //tornado
	EXP(7,6),  //explosion
	BUS(8,4);  

	public final int objectId;
	public final int numFrames;

	private SpriteKind(int objectId, int numFrames)
	{
		this.objectId = objectId;
		this.numFrames = numFrames;
	}
}

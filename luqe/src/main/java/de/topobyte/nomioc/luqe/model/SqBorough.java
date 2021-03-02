// Copyright 2021 Sebastian Kuerten
//
// This file is part of nomioc.
//
// nomioc is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// nomioc is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with nomioc. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.nomioc.luqe.model;

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 * 
 */
public class SqBorough
{

	private int id;
	private int level;
	private String name;

	/**
	 * @return the id.
	 */
	public int getId()
	{
		return this.id;
	}

	/**
	 * @return the level.
	 */
	public int getLevel()
	{
		return this.level;
	}

	/**
	 * @return the name.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Create a new instance of SqBorough.
	 * 
	 * @param id
	 *            the id.
	 * @param level
	 *            the level.
	 * @param name
	 *            the name.
	 */
	public SqBorough(int id, int level, String name)
	{
		this.id = id;
		this.level = level;
		this.name = name;
	}

	@Override
	public int hashCode()
	{
		return this.id;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof SqBorough) {
			SqBorough o = (SqBorough) other;
			return o.id == this.id;
		}
		return false;
	}
}

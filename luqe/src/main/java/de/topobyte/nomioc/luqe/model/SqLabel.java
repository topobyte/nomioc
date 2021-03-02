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
public class SqLabel
{

	private int id;
	private int type;
	private String name;
	private int x;
	private int y;

	public int getId()
	{
		return id;
	}

	public int getType()
	{
		return type;
	}

	public String getName()
	{
		return name;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getY()
	{
		return this.y;
	}

	public int getX()
	{
		return this.x;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	@Override
	public int hashCode()
	{
		return this.id;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof SqLabel) {
			SqLabel o = (SqLabel) other;
			return o.id == this.id;
		}
		return false;
	}

}

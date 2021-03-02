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

package de.topobyte.nomioc.road;

import org.locationtech.jts.geom.Geometry;

import de.topobyte.osm4j.core.model.iface.EntityType;

public class OsmGeometry
{

	private EntityType type;
	private long id;
	private Geometry geometry;

	public OsmGeometry(EntityType type, long id, Geometry geometry)
	{
		this.type = type;
		this.id = id;
		this.geometry = geometry;
	}

	public EntityType getType()
	{
		return type;
	}

	public void setType(EntityType type)
	{
		this.type = type;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Geometry getGeometry()
	{
		return geometry;
	}

	public void setGeometry(Geometry geometry)
	{
		this.geometry = geometry;
	}

}

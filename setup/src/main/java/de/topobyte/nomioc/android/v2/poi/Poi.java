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

package de.topobyte.nomioc.android.v2.poi;

import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Point;

import de.topobyte.mercatorcoordinates.GeoConv;
import de.topobyte.nomioc.android.v2.model.hibernate.PoiType;
import de.topobyte.sqlitespatial.spatialindex.builder.Indexable;
import de.topobyte.sqlitespatial.spatialindex.builder.Node;

public class Poi implements Indexable
{
	String name;
	Point point;
	Map<String, String> tags;
	Set<PoiType> types;

	int mx;
	int my;

	Node<? extends Indexable> node = null;
	int sid;

	public Poi(String name, Point point, Map<String, String> tags,
			Set<PoiType> types)
	{
		this.name = name;
		this.point = point;
		this.tags = tags;
		this.types = types;
		mx = GeoConv.mercatorFromLongitude(point.getX());
		my = GeoConv.mercatorFromLatitude(point.getY());
	}

	@Override
	public int getX()
	{
		return mx;
	}

	@Override
	public int getY()
	{
		return my;
	}

	@Override
	public void setNode(Node<? extends Indexable> node)
	{
		this.node = node;
	}

	@Override
	public Node<? extends Indexable> getNode()
	{
		return node;
	}

	@Override
	public void setSid(int sid)
	{
		this.sid = sid;
	}

	public boolean wantsName()
	{
		for (PoiType type : types) {
			if (type.useName()) {
				return true;
			}
		}
		return false;
	}

	public boolean wantsMeta()
	{
		return wantsName();
	}
}

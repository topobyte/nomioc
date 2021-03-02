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

import de.topobyte.nomioc.android.v2.model.hibernate.PoiType;

public class PointPoi extends Poi
{

	public PointPoi(String name, Point point, Map<String, String> tags,
			Set<PoiType> types)
	{
		super(name, point, tags, types);
	}

}

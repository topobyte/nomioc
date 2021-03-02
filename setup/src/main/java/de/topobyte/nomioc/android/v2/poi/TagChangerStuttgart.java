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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;

import de.topobyte.adt.geo.BBox;
import de.topobyte.jsijts.JsiAndJts;

public class TagChangerStuttgart implements TagChanger
{

	private BBox exclusionBox = new BBox(9.1664, 48.7200, 9.1681, 48.7192);
	private Geometry exclusionGeometry = JsiAndJts
			.toGeometry(JsiAndJts.toRectangle(exclusionBox.toEnvelope()));

	@Override
	public void node(Point point, Map<String, String> tags)
	{
		stripParking(point, tags);
	}

	@Override
	public void way(LineString string, Map<String, String> tags)
	{
		stripParking(string, tags);
	}

	@Override
	public void polygon(MultiPolygon polygon, Map<String, String> tags)
	{
		stripParking(polygon, tags);
	}

	private void stripParking(Geometry geometry, Map<String, String> tags)
	{
		if (exclusionGeometry.contains(geometry)) {
			String amenity = tags.get("amenity");
			if (amenity != null && amenity.equals("parking")) {
				tags.remove("amenity");
			}
		}
	}

}

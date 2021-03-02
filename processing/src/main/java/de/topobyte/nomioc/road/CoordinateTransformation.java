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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.GeometryEditor;

import de.topobyte.geomath.WGS84;

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 */
public class CoordinateTransformation extends GeometryEditor.CoordinateOperation
{

	private final Type type;

	/**
	 * @param type
	 *            the type of transformation to be performed.
	 */
	public CoordinateTransformation(Type type)
	{
		this.type = type;
	}

	@Override
	public Coordinate[] edit(Coordinate[] coordinates, Geometry geometry)
	{
		int n = coordinates.length;
		Coordinate[] result = new Coordinate[n];
		for (int i = 0; i < n; i++) {
			result[i] = transform(coordinates[i]);
		}
		return result;
	}

	/**
	 * Convert a single Coordinate.
	 * 
	 * @param coordinate
	 *            the coordinate to transform.
	 * @return the transformed coordinate.
	 */
	public Coordinate transform(Coordinate coordinate)
	{
		Coordinate result = new Coordinate();
		switch (type) {
		case MERCATOR_TO_WGS_84:
			result.x = WGS84.merc2lon(coordinate.x);
			result.y = WGS84.merc2lat(coordinate.y);
			break;
		case WGS84_TO_MERCATOR:
			result.x = WGS84.lon2merc(coordinate.x);
			result.y = WGS84.lat2merc(coordinate.y);
			break;
		}
		return result;
	}

	public enum Type {
		/**
		 * Transform from WGS84 to Mercator projection.
		 */
		WGS84_TO_MERCATOR,
		/**
		 * Transform from Mercator to WGS84 projection.
		 */
		MERCATOR_TO_WGS_84
	}

}
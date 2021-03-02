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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.operation.distance.DistanceOp;

/**
 * A group of roads. A group of roads shares the same name, but consists of
 * different not necessarily adjacent segments
 *
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 */
public class RoadGroup
{

	private String name;
	private Set<OsmGeometry> roads;

	/**
	 * Create a new RoadGroup made up of the given roads.
	 *
	 * @param name
	 *            the name of this group.
	 *
	 * @param roads
	 *            the roads the group will contain.
	 */
	public RoadGroup(String name, Set<OsmGeometry> roads)
	{
		this.name = name;
		this.roads = roads;
	}

	/**
	 * Get this group's name.
	 *
	 * @return the name of this group.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Get the set of roads in this group.
	 *
	 * @return the roads making up this group.
	 */
	public Set<OsmGeometry> getRoads()
	{
		return roads;
	}

	/**
	 * Calculate the mean coordinate of this road group. That is the mean of all
	 * road's mean coordinates.
	 *
	 * @return this road group's mean coordinate.
	 */
	public Point getMeanCoordinate()
	{
		GeometryFactory factory = new GeometryFactory();

		Geometry union = toGeometryCollection();

		Envelope envelope = union.getEnvelopeInternal();
		double midLon = (envelope.getMinX() + envelope.getMaxX()) / 2;
		double midLat = (envelope.getMinY() + envelope.getMaxY()) / 2;

		Point mid = factory.createPoint(new Coordinate(midLon, midLat));

		DistanceOp distanceOp = new DistanceOp(union, mid);
		Coordinate[] nearestPoints = distanceOp.nearestPoints();
		Coordinate centerPoint = nearestPoints[0];

		Point point = factory.createPoint(centerPoint);
		return point;
	}

	@Override
	public int hashCode()
	{
		return roads.hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof RoadGroup) {
			RoadGroup other = (RoadGroup) o;
			if (!name.equals(other.name)) {
				return false;
			}
			return roads.equals(other.getRoads());
		}
		return false;
	}

	/**
	 * Create a MultiLineString containing all Lines of this group's roads.
	 *
	 * @return a MultiLineString representing the roads.
	 */
	public Geometry toGeometryCollection()
	{
		if (roads.size() == 1) {
			return roads.iterator().next().getGeometry();
		}
		Geometry[] geoms = new Geometry[roads.size()];
		int i = 0;
		for (OsmGeometry r : roads) {
			geoms[i++] = r.getGeometry();
		}
		return geoms[0].getFactory().createGeometryCollection(geoms);
	}

	public Geometry getLinework()
	{
		List<LineString> lines = new ArrayList<>();
		for (OsmGeometry or : roads) {
			Geometry r = or.getGeometry();
			if (r instanceof LineString) {
				lines.add((LineString) r);
			} else if (r instanceof Polygonal) {
				Geometry boundary = r.getBoundary();
				if (boundary instanceof LineString) {
					lines.add((LineString) boundary);
				} else if (boundary instanceof GeometryCollection) {
					for (int i = 0; i < boundary.getNumGeometries(); i++) {
						Geometry part = boundary.getGeometryN(i);
						if (part instanceof LineString) {
							lines.add((LineString) part);
						}
					}
				}
			}
		}
		if (lines.size() == 0) {
			return null;
		}
		LineString[] lineArray = lines.toArray(new LineString[0]);
		return lineArray[0].getFactory().createMultiLineString(lineArray);
	}

}

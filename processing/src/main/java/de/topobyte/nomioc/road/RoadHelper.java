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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.GeometryEditor;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slimjars.dist.gnu.trove.map.hash.THashMap;

import de.topobyte.adt.graph.Graph;
import de.topobyte.geomath.WGS84;

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 */
public class RoadHelper
{

	final static Logger logger = LoggerFactory.getLogger(RoadHelper.class);

	private double distinctionDistance;

	public RoadHelper(double distinctionDistance)
	{
		this.distinctionDistance = distinctionDistance;
	}

	public double getDistinctionDistance()
	{
		return distinctionDistance;
	}

	public void setDistinctionDistance(double distinctionDistance)
	{
		this.distinctionDistance = distinctionDistance;
	}

	/**
	 * Given a set of Roads, identify road-segments that seem to belong to the
	 * same logical street and group them.
	 *
	 * @param roads
	 *            the set of roads to process.
	 * @param names
	 *            the data source for way information.
	 * @return the possibly smaller set of roads.
	 */
	public Set<RoadGroup> groupLogically(Collection<OsmGeometry> roads,
			THashMap<OsmGeometry, String> names)
	{
		// first, group roads by name
		Map<String, Set<OsmGeometry>> nameToRoads = new HashMap<>();
		for (OsmGeometry r : roads) {
			String name = names.get(r);
			if (name == null) {
				continue;
			}
			if (nameToRoads.containsKey(name)) {
				nameToRoads.get(name).add(r);
			} else {
				Set<OsmGeometry> set = new HashSet<>(2);
				set.add(r);
				nameToRoads.put(name, set);
			}
		}

		// then group sets by distance
		Set<RoadGroup> grouped = new HashSet<>(roads.size());

		for (String name : nameToRoads.keySet()) {
			Set<OsmGeometry> roadSet = nameToRoads.get(name);
			Set<RoadGroup> groups = groupByDistance(name, roadSet);
			grouped.addAll(groups);
		}

		return grouped;
	}

	private Set<RoadGroup> groupByDistance(String name,
			Set<OsmGeometry> roadSet)
	{
		Set<RoadGroup> groups = new HashSet<>(2);

		GeometryEditor editor = new GeometryEditor();
		Set<Geometry> transformed = new HashSet<>();
		Map<Geometry, OsmGeometry> geomToRoad = new HashMap<>();
		for (OsmGeometry or : roadSet) {
			Geometry r = or.getGeometry();
			Geometry trans = editor.edit(r, new CoordinateTransformation(
					CoordinateTransformation.Type.WGS84_TO_MERCATOR));
			transformed.add(trans);
			geomToRoad.put(trans, or);
		}

		Graph<Geometry> graph = new Graph<>();
		for (Geometry g : transformed) {
			graph.addNode(g);
		}

		for (Geometry g : transformed) {
			for (Geometry h : transformed) {
				if (g.equals(h)) {
					continue;
				}
				double distance = getDistance(g, h);
				if (distance <= distinctionDistance) {
					graph.addEdge(g, h);
					graph.addEdge(h, g);
				}
			}
		}

		// get partition of graph... these are the groups.
		Set<Set<Geometry>> partition = graph.getPartition();
		for (Set<Geometry> set : partition) {
			Set<OsmGeometry> roads = new HashSet<>();
			for (Geometry g : set) {
				roads.add(geomToRoad.get(g));
			}
			groups.add(new RoadGroup(name, roads));
		}

		return groups;
	}

	private static double getDistance(Geometry g, Geometry h)
	{
		Coordinate[] points = DistanceOp.nearestPoints(g, h);
		CoordinateTransformation trans = new CoordinateTransformation(
				CoordinateTransformation.Type.MERCATOR_TO_WGS_84);
		Coordinate[] wgs = trans.edit(points, null);
		Coordinate c1 = wgs[0];
		Coordinate c2 = wgs[1];
		double dist = WGS84.haversineDistance(c1.x, c1.y, c2.x, c2.y);
		return dist;
	}

}
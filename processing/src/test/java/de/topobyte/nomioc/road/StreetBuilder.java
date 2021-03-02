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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slimjars.dist.gnu.trove.map.hash.THashMap;

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.geometry.OsmEntityGeometryHandler;

public class StreetBuilder implements OsmEntityGeometryHandler
{

	final static Logger logger = LoggerFactory.getLogger(StreetBuilder.class);

	private THashMap<OsmGeometry, String> names = new THashMap<>();
	private Set<OsmGeometry> waySet = new HashSet<>();

	@Override
	public void processNode(OsmNode node, Point point, Map<String, String> tags)
	{
		// ignore
	}

	@Override
	public void processMultipolygon(OsmWay way, MultiPolygon polygon,
			Map<String, String> tags, Point centroid)
	{
		addStreet(EntityType.Way, way, polygon, tags);
	}

	@Override
	public void processMultipolygon(OsmRelation relation, MultiPolygon polygon,
			Map<String, String> tags, Point centroid)
	{
		addStreet(EntityType.Relation, relation, polygon, tags);
	}

	@Override
	public void processWayString(OsmWay way, LineString string,
			Map<String, String> tags)
	{
		addStreet(EntityType.Way, way, string, tags);
	}

	public static boolean isStreet(Map<String, String> tags)
	{
		String highway = tags.get("highway");
		if (highway == null) {
			return false;
		}
		return true;
	}

	private void addStreet(EntityType type, OsmEntity object, Geometry geometry,
			Map<String, String> tags)
	{
		if (!isStreet(tags)) {
			return;
		}
		String name = tags.get("name");
		if (name == null) {
			return;
		}

		OsmGeometry osmGeometry = new OsmGeometry(type, object.getId(),
				geometry);

		names.put(osmGeometry, name);
		waySet.add(osmGeometry);
	}

	public List<Street> buildStreets()
	{
		logger.info("building groups");
		RoadHelper roadHelper = new RoadHelper(500);
		Set<RoadGroup> groups = roadHelper.groupLogically(waySet, names);

		List<Street> streets = new ArrayList<>();

		logger.info("iterating groups");
		for (RoadGroup group : groups) {
			Geometry linework = group.getLinework();
			String name = group.getName();
			logger.info("Street: " + name);
			if (linework == null) {
				logger.warn("No linework available");
				continue;
			}

			Street street = new Street(name, linework);

			streets.add(street);
		}

		return streets;
	}

	public Set<OsmGeometry> getWays()
	{
		return waySet;
	}

}

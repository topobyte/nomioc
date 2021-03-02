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

package de.topobyte.nomioc.android.v2.street;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slimjars.dist.gnu.trove.map.hash.THashMap;

import de.topobyte.diacritic.IDiacriticUtil;
import de.topobyte.diacritic.NormalizerDiacriticUtil;
import de.topobyte.jts.indexing.NearestNeighbourTesselation;
import de.topobyte.mercatorcoordinates.GeoConv;
import de.topobyte.nomioc.HibernateIndexBuilder;
import de.topobyte.nomioc.android.v2.model.hibernate.Borough;
import de.topobyte.nomioc.android.v2.model.hibernate.BoroughSet;
import de.topobyte.nomioc.android.v2.model.hibernate.PostalCode;
import de.topobyte.nomioc.android.v2.model.hibernate.PostalCodeSet;
import de.topobyte.nomioc.android.v2.model.hibernate.SpatialIndexFactoryStreet;
import de.topobyte.nomioc.android.v2.model.hibernate.Street;
import de.topobyte.nomioc.android.v2.regions.Regions;
import de.topobyte.nomioc.road.OsmGeometry;
import de.topobyte.nomioc.road.RoadGroup;
import de.topobyte.nomioc.road.RoadHelper;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.geometry.OsmEntityGeometryHandler;

public class StreetBuilder implements OsmEntityGeometryHandler
{

	final static Logger logger = LoggerFactory.getLogger(StreetBuilder.class);

	private SessionFactory sfOutput;

	private Regions regions;

	private THashMap<OsmGeometry, String> names = new THashMap<>();
	private Set<OsmGeometry> waySet = new HashSet<>();

	private Map<Set<Borough>, BoroughSet> boroughSets;
	private Map<Set<PostalCode>, PostalCodeSet> postalCodeSets;

	public StreetBuilder(SessionFactory sfOutput, Regions regions,
			Map<Set<Borough>, BoroughSet> boroughSets,
			Map<Set<PostalCode>, PostalCodeSet> postalCodeSets)
	{
		this.sfOutput = sfOutput;
		this.regions = regions;
		this.boroughSets = boroughSets;
		this.postalCodeSets = postalCodeSets;
	}

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

	/*
	 * Street stuff
	 */

	public void buildStreets()
	{
		logger.info("building groups");
		RoadHelper roadHelper = new RoadHelper(500);
		Set<RoadGroup> groups = roadHelper.groupLogically(waySet, names);

		IDiacriticUtil diacritic = new NormalizerDiacriticUtil();

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
			String simpleName = diacritic.simplify(name);
			boolean simplified = !simpleName.equals(name);
			String insertionName = simplified ? name : null;
			if (simplified) {
				logger.debug(String.format("%s -> %s", name, simpleName));
			}
			Point center = group.getMeanCoordinate();
			int x = GeoConv.mercatorFromLongitude(center.getX());
			int y = GeoConv.mercatorFromLatitude(center.getY());
			Street street = new Street(insertionName, simpleName, x, y);

			map(street, linework, regions);

			streets.add(street);
		}

		HibernateIndexBuilder.buildIndex(streets, sfOutput,
				new SpatialIndexFactoryStreet());

		for (Street street : streets) {
			sfOutput.getCurrentSession().persist(street);
		}
	}

	private void map(Street street, Geometry linework, Regions regions)
	{
		Set<Borough> boroughs = regions.getBoroughIndex()
				.intersecting(linework);
		Set<PostalCode> postalCodes = regions.getPostalCodeIndex()
				.intersecting(linework);

		BoroughSet boroughSet = boroughSets.get(boroughs);
		if (boroughSet == null) {
			boroughSet = new BoroughSet(boroughs);
			boroughSets.put(boroughs, boroughSet);
			sfOutput.getCurrentSession().persist(boroughSet);
		}

		PostalCodeSet postalCodeSet = postalCodeSets.get(postalCodes);
		if (postalCodeSet == null) {
			postalCodeSet = new PostalCodeSet(postalCodes);
			postalCodeSets.put(postalCodes, postalCodeSet);
			sfOutput.getCurrentSession().persist(postalCodeSet);
		}

		// street.setBoroughs(boroughs);
		// street.setPostalCodes(postalCodes);
		street.setBoroughSet(boroughSet);
		street.setPostalCodeSet(postalCodeSet);
	}

	public NearestNeighbourTesselation<String> buildNameLookup()
	{
		NearestNeighbourTesselation<String> map = new NearestNeighbourTesselation<>();
		for (Entry<OsmGeometry, String> entry : names.entrySet()) {
			map.add(entry.getKey().getGeometry(), entry.getValue());
		}
		return map;
	}
}

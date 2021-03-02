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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.hibernate.SessionFactory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.adt.multicollections.HashMultiSet;
import de.topobyte.adt.multicollections.MultiSet;
import de.topobyte.diacritic.IDiacriticUtil;
import de.topobyte.diacritic.NormalizerDiacriticUtil;
import de.topobyte.jts.indexing.GeometryTesselationMap;
import de.topobyte.jts.indexing.NearestNeighbourTesselation;
import de.topobyte.jts.indexing.NearestNeighbourTesselation.Entry;
import de.topobyte.nomioc.HibernateIndexBuilder;
import de.topobyte.nomioc.android.v2.config.MappingResult;
import de.topobyte.nomioc.android.v2.config.PoiMapper;
import de.topobyte.nomioc.android.v2.model.hibernate.Borough;
import de.topobyte.nomioc.android.v2.model.hibernate.BoroughSet;
import de.topobyte.nomioc.android.v2.model.hibernate.PoiType;
import de.topobyte.nomioc.android.v2.model.hibernate.PointOfInterest;
import de.topobyte.nomioc.android.v2.model.hibernate.PostalCode;
import de.topobyte.nomioc.android.v2.model.hibernate.PostalCodeSet;
import de.topobyte.nomioc.android.v2.model.hibernate.SpatialIndexFactoryPoi;
import de.topobyte.nomioc.android.v2.regions.Regions;
import de.topobyte.nomioc.android.v2.street.StreetBuilder;
import de.topobyte.nomioc.elevation.ElevationParser;
import de.topobyte.nomioc.elevation.ValueFormatter;
import de.topobyte.nomioc.elevation.model.LengthUnit;
import de.topobyte.nomioc.elevation.model.Value;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.geometry.OsmEntityGeometryHandler;

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 *
 */
public class PoiCreator implements OsmEntityGeometryHandler
{
	static final Logger logger = LoggerFactory.getLogger(PoiCreator.class);

	private final SessionFactory sfOutput;
	private IDiacriticUtil diacritic = new NormalizerDiacriticUtil();

	private PoiMapper poiMapper;

	private Map<Set<Borough>, BoroughSet> boroughSets;
	private Map<Set<PostalCode>, PostalCodeSet> postalCodeSets;

	private Regions regions;
	private NearestNeighbourTesselation<String> streetNameMap;

	private List<Poi> allPois = new ArrayList<>();
	private List<PointPoi> pointCandidates = new ArrayList<>();
	private Set<PolygonPoi> polygonCandidates = new HashSet<>();
	private GeometryTesselationMap<PolygonPoi> polygonIndex = new GeometryTesselationMap<>(
			false);

	private int nameMaxLength;

	private TagChanger tagPreprocessor;

	private ElevationParser elevationParser = new ElevationParser();
	private ValueFormatter elevationFormatter = new ValueFormatter(1);

	public PoiCreator(SessionFactory sfOutput, Regions regions,
			Map<Set<Borough>, BoroughSet> boroughSets,
			Map<Set<PostalCode>, PostalCodeSet> postalCodeSets,
			PoiMapper poiMapper,
			NearestNeighbourTesselation<String> streetNameMap,
			int nameMaxLength, TagChanger tagPreprocessor)
	{
		this.sfOutput = sfOutput;
		this.regions = regions;
		this.boroughSets = boroughSets;
		this.postalCodeSets = postalCodeSets;
		this.poiMapper = poiMapper;
		this.streetNameMap = streetNameMap;
		this.nameMaxLength = nameMaxLength;
		this.tagPreprocessor = tagPreprocessor;
	}

	@Override
	public void processNode(OsmNode node, Point point, Map<String, String> tags)
	{
		if (tagPreprocessor != null) {
			tagPreprocessor.node(point, tags);
		}

		List<MappingResult> results = poiMapper.determineTypes(tags);
		if (results.isEmpty()) {
			return;
		}
		for (MappingResult mr : results) {
			point(mr.getName(), point, tags, mr.getTypes());
		}
	}

	@Override
	public void processWayString(OsmWay way, LineString string,
			Map<String, String> tags)
	{
		if (StreetBuilder.isStreet(tags)) {
			return;
		}

		if (string.isClosed() && string.getNumPoints() > 3) {
			LinearRing ring = string.getFactory()
					.createLinearRing(string.getCoordinates());
			Polygon weak = ring.getFactory().createPolygon(ring, null);
			Geometry buffer = weak.buffer(0);
			if (!(buffer instanceof Polygon)) {
				logger.warn("Buffer is not a polygon");
				return;
			}
			Polygon polygon = (Polygon) buffer;
			Point centroid = polygon.getCentroid();

			if (tagPreprocessor != null) {
				tagPreprocessor.way(string, tags);
			}

			List<MappingResult> results = poiMapper.determineTypes(tags);
			if (results.isEmpty()) {
				return;
			}
			for (MappingResult mr : results) {
				polygonal(mr.getName(), polygon, tags, centroid, mr.getTypes());
			}
		}
	}

	@Override
	public void processMultipolygon(OsmWay way, MultiPolygon polygon,
			Map<String, String> tags, Point centroid)
	{
		processMultipolygonInternal(way, polygon, tags, centroid);
	}

	@Override
	public void processMultipolygon(OsmRelation relation, MultiPolygon polygon,
			Map<String, String> tags, Point centroid)
	{
		processMultipolygonInternal(relation, polygon, tags, centroid);
	}

	private void processMultipolygonInternal(OsmEntity entity,
			MultiPolygon polygon, Map<String, String> tags, Point centroid)
	{
		if (StreetBuilder.isStreet(tags)) {
			return;
		}

		if (tagPreprocessor != null) {
			tagPreprocessor.polygon(polygon, tags);
		}

		List<MappingResult> results = poiMapper.determineTypes(tags);
		if (results.isEmpty()) {
			return;
		}
		for (MappingResult mr : results) {
			polygonal(mr.getName(), polygon, tags, centroid, mr.getTypes());
		}
	}

	private void point(String name, Point point, Map<String, String> tags,
			Set<PoiType> types)
	{
		pointCandidates.add(new PointPoi(name, point, tags, types));
	}

	private void polygonal(String name, Geometry polygon,
			Map<String, String> tags, Point centroid, Set<PoiType> types)
	{
		if (centroid.isEmpty()) {
			logger.warn("Empty polygon centroid: " + tags);
			return;
		}
		PolygonPoi poi = new PolygonPoi(name, polygon, centroid, tags, types);
		polygonCandidates.add(poi);
		polygonIndex.add(polygon, poi);
	}

	public void buildPois()
	{
		removeOverlappingPolygons();

		allPois.addAll(pointCandidates);
		allPois.addAll(polygonCandidates);
		analyse(allPois);

		HibernateIndexBuilder.buildIndex(allPois, sfOutput,
				new SpatialIndexFactoryPoi());

		insertPois();
	}

	private void removeOverlappingPolygons()
	{
		for (PointPoi poi : pointCandidates) {
			String name1 = poi.name;
			if (name1 == null) {
				continue;
			}
			Set<PolygonPoi> overlapping = polygonIndex.covering(poi.point);
			for (PolygonPoi overlap : overlapping) {
				String name2 = overlap.name;
				if (name2 == null) {
					continue;
				}
				if (name1.equals(name2)) {
					logger.info("Removing overlapping polygon: " + poi.tags);
					poi.types.addAll(overlap.types);
					polygonCandidates.remove(overlap);
				}
			}
		}
	}

	private void analyse(List<Poi> pois)
	{
		MultiSet<String> multi = new HashMultiSet<>();
		for (Poi poi : pois) {
			String name = poi.name;
			if (name == null) {
				continue;
			}
			String simple = simple(name);
			if (!name.equals(simple)) {
				System.out.println("replace: " + name + " -> " + simple);
			}
			multi.add(simple);
		}
		for (String key : multi.keySet()) {
			int occurences = multi.occurences(key);
			if (occurences > 2) {
				System.out.println("multi: '" + key + "' " + occurences);
			}
		}
		for (Poi poi : pois) {
			String name = poi.name;
			if (name == null) {
				continue;
			}
			String simple = simple(name);
			// TODO: when re-enabling this, add check for
			// railwaystation/busstation/...
			if (multi.occurences(simple) > 2) {
				// clarify(poi);
			}
		}
	}

	private Pattern pattern1 = Pattern.compile("\\p{Punct}");
	private Pattern pattern2 = Pattern.compile("-");

	private String simple(String name)
	{
		String simple = pattern2.matcher(name).replaceAll(" ");
		simple = pattern1.matcher(simple).replaceAll("");
		return simple.toLowerCase(Locale.US);
	}

	private void clarify(Poi poi)
	{
		Map<String, String> tags = copyTags(poi.tags);
		String street = poi.tags.get("addr:street");
		if (street != null) {
			updateName(tags, street);
		} else {
			String nearestStreet = locateNearestStreet(poi.point);
			if (nearestStreet != null) {
				updateName(tags, nearestStreet);
			} else {
				System.out.println("no change possible: " + poi.name);
			}
		}
		poi.tags = tags;
	}

	private void updateName(Map<String, String> tags, String street)
	{
		String oldName = tags.get("name");
		String newName = oldName + " (" + street + ")";
		tags.put("name", newName);
		System.out.println("changed: '" + oldName + "' to '" + newName + "'");
	}

	private String locateNearestStreet(Point point)
	{
		Set<Entry<String>> candidates = streetNameMap
				.within(point.getCoordinate(), 0.005, 0.003);
		String name = null;
		double dBest = Double.MAX_VALUE;
		for (Entry<String> candidate : candidates) {
			double distance = candidate.getGeometry().distance(point);
			if (distance < dBest) {
				dBest = distance;
				name = candidate.getData();
			}
		}
		return name;
	}

	private Map<String, String> copyTags(Map<String, String> tags)
	{
		Map<String, String> copy = new HashMap<>();
		for (String key : tags.keySet()) {
			copy.put(key, tags.get(key));
		}
		return copy;
	}

	private void insertPois()
	{
		for (Poi poi : allPois) {
			createAndInsert(poi);
		}
	}

	private void createAndInsert(Poi poi)
	{
		boolean useName = poi.wantsName();
		boolean useMeta = poi.wantsMeta();

		String name = null, phone = null, website = null;
		if (useName) {
			name = poi.name;
		}
		if (isPeakOrVolcano(poi)) {
			String elevation = poi.tags.get("ele");
			String formatted = null;
			if (elevation != null) {
				Value value = elevationParser.parse(elevation);
				if (value != null) {
					formatted = elevationFormatter.format(
							value.getValue(LengthUnit.METERS, true).getValue());
				}
			}
			phone = formatted;
			logger.info(
					"Peak/Volcano. Name: '" + name + "', Height: " + formatted);
		} else if (useMeta) {
			phone = get(poi.tags, "contact:phone", "phone");
			website = get(poi.tags, "contact:website", "website");
		}

		if (name != null && name.length() > nameMaxLength) {
			String oldName = name;
			name = name.substring(0, nameMaxLength) + "…";
			logger.info(
					"Truncating long name '" + oldName + "' to '" + name + "'");
		}

		String simpleName = null, insertionName = null;
		if (name != null) {
			simpleName = diacritic.simplify(name);
			boolean simplified = !simpleName.equals(name);
			insertionName = simplified ? name : null;
			if (simplified) {
				logger.debug(String.format("%s -> %s", name, simpleName));
			}
		}

		PointOfInterest p = new PointOfInterest(insertionName, simpleName,
				phone, website, poi.mx, poi.my);

		p.setSid(poi.sid);

		Set<Borough> boroughs = regions.getBoroughIndex().covering(poi.point);
		Set<PostalCode> postalCodes = regions.getPostalCodeIndex()
				.covering(poi.point);

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

		p.setTypes(poi.types);
		// poi.setBoroughs(boroughs);
		p.setBoroughSet(boroughSet);
		// poi.setPostalCodes(postalCodes);
		p.setPostalCodeSet(postalCodeSet);

		sfOutput.getCurrentSession().persist(p);

		logger.info("insert: " + name);
	}

	private boolean isPeakOrVolcano(Poi poi)
	{
		for (PoiType type : poi.types) {
			String name = type.getName();
			if (name.equals("peak") || name.equals("volcano")) {
				return true;
			}
		}
		return false;
	}

	private String get(Map<String, String> tags, String key1, String key2)
	{
		if (tags.containsKey(key1)) {
			return tags.get(key1);
		}
		return tags.get(key2);
	}

}

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

package de.topobyte.nomioc.android.v2.regions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.jts.utils.PolygonHelper;
import de.topobyte.nomioc.android.v2.division.DivisionConfig;
import de.topobyte.nomioc.android.v2.model.hibernate.Borough;
import de.topobyte.nomioc.android.v2.model.hibernate.PostalCode;
import de.topobyte.nomioc.util.FileUtil;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.simplemapfile.core.EntityFile;
import de.topobyte.simplemapfile.xml.SmxFileReader;
import de.topobyte.simplemapfile.xml.SmxFileWriter;

public class RegionsUtil
{

	final static Logger logger = LoggerFactory.getLogger(RegionsUtil.class);

	public static Regions loadFromDirectory(Path dirBoundaries)
	{
		Regions regions = new Regions();

		Set<File> files = new HashSet<>();
		files = FileUtil.find(dirBoundaries.toFile(), 2, 10);
		files = FileUtil.onlyNormalFiles(files);
		for (File file : files) {
			logger.info("file: " + file);
			try {
				EntityFile entity = SmxFileReader.read(file);
				Geometry boundary = entity.getGeometry();

				boundary = PolygonHelper.polygonal(boundary);
				if (boundary == null) {
					System.out.println("Unable to dissolve collection");
					continue;
				}

				Map<String, String> tags = entity.getTags();
				String valBoundary = tags.get("boundary");
				if (valBoundary == null) {
					continue;
				}
				if (valBoundary.equals("postal_code")) {
					String valCode = tags.get("postal_code");

					logger.info(String.format("%s", valCode));
					PostalCode postalCode = new PostalCode(valCode);
					regions.getPostalCodeIndex().add(boundary, postalCode);
				} else if (valBoundary.equals("administrative")) {
					String valLevel = tags.get("admin_level");
					String valName = tags.get("name");
					int level = Integer.parseInt(valLevel);

					logger.info(String.format("%s, %s", valName, level));
					Borough borough = new Borough(valName, level);
					regions.getBoroughIndex().add(boundary, borough);
				}
			} catch (Exception e) {
				logger.error("unable to read file", e);
			}
		}

		return regions;
	}

	public static Regions loadDivision(Geometry cityBoundary, Path divisionDir,
			DivisionConfig divisionConfig, OsmFileInput inputFile)
			throws IOException
	{
		Regions regions = new Regions();

		logger.info("Adding place-node boroughs");
		Files.createDirectories(divisionDir);

		List<Coordinate> coordinates = new ArrayList<>();
		Map<Coordinate, String> placeMap = new HashMap<>();

		OsmIteratorInput input = inputFile.createIterator(true, false);
		OsmIterator iterator = input.getIterator();
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() != EntityType.Node) {
				continue;
			}
			OsmNode node = (OsmNode) container.getEntity();
			Map<String, String> tags = OsmModelUtil.getTagsAsMap(node);
			if (!tags.containsKey("place")) {
				continue;
			}
			String place = tags.get("place");
			if (!divisionConfig.usePlace(place)) {
				continue;
			}
			String name = tags.get("name");
			if (name == null) {
				continue;
			}
			Coordinate c = new Coordinate(node.getLongitude(),
					node.getLatitude());
			coordinates.add(c);
			placeMap.put(c, name);
		}
		input.close();

		Envelope bbox = cityBoundary.getEnvelopeInternal();
		Geometry gbbox = cityBoundary.getEnvelope();
		VoronoiDiagramBuilder vd = new VoronoiDiagramBuilder();
		vd.setClipEnvelope(bbox);

		vd.setSites(coordinates);

		List<?> cells = vd.getSubdivision()
				.getVoronoiCellPolygons(new GeometryFactory());

		int i = 0;
		for (Object o : cells) {
			i++;
			Geometry boundary = (Geometry) o;

			Geometry clipped;
			try {
				clipped = boundary.intersection(gbbox);
			} catch (TopologyException e) {
				clipped = boundary.buffer(0).intersection(gbbox);
			}
			int level = 8;

			Object coordinate = boundary.getUserData();
			String name = placeMap.get(coordinate);

			if (name == null) {
				logger.info("no name for coordinate: " + coordinate);
				continue;
			}

			Borough borough = new Borough(name, level);
			regions.getBoroughIndex().add(clipped, borough);

			EntityFile entityFile = new EntityFile();
			entityFile.setGeometry(clipped);
			entityFile.addTag("name", name);
			Path file = divisionDir.resolve("place_" + i + "_" + name + ".smx");
			try {
				SmxFileWriter.write(entityFile, file.toFile());
			} catch (Exception e) {
				logger.error("unable to write division file", e);
			}
		}

		return regions;
	}

}

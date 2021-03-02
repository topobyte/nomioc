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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.locationtech.jts.geom.Geometry;
import org.xml.sax.SAXException;

import de.topobyte.osm4j.diskstorage.nodedb.NodeDB;
import de.topobyte.osm4j.diskstorage.vardb.VarDB;
import de.topobyte.osm4j.diskstorage.waydb.WayRecordWithTags;
import de.topobyte.osm4j.processing.entities.ExecutableEntityProcessor;
import de.topobyte.osm4j.processing.entities.filter.DefaultEntityFilter;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.simplemapfile.core.EntityFile;
import de.topobyte.simplemapfile.xml.SmxFileReader;

public class TestGroupRoads
{

	public static void main(String[] args)
			throws IOException, ParserConfigurationException, SAXException
	{
		Path pathBase = Paths.get("/data/oxygen");
		String name = "planet-191007";

		Path pathSelenium = pathBase.resolve("selenium-extracts").resolve(name);
		Path pathEntityDbs = pathBase.resolve("entitydbs").resolve(name);

		String path = "germany/Salzgitter";

		Path input = pathSelenium.resolve(path + ".tbo");
		Path inputSmx = pathSelenium.resolve(path + ".smx");

		Path nodesData = pathEntityDbs.resolve(path + ".nodedb.dat");
		Path nodesIndex = pathEntityDbs.resolve(path + ".nodedb.idx");
		Path waysData = pathEntityDbs.resolve(path + ".waydb.dat");
		Path waysIndex = pathEntityDbs.resolve(path + ".waydb.idx");

		EntityFile cityEntity = SmxFileReader.read(inputSmx.toFile());
		Geometry cityBoundary = cityEntity.getGeometry();

		OsmFileInput inputFile = new OsmFileInput(input, FileFormat.TBO);

		NodeDB nodeDB = null;
		VarDB<WayRecordWithTags> wayDB = null;

		try {
			nodeDB = new NodeDB(nodesData, nodesIndex);
			wayDB = new VarDB<>(waysData, waysIndex, new WayRecordWithTags(0));
		} catch (FileNotFoundException e) {
			throw new IOException(
					"Unable to open entity database: " + e.getMessage());
		}

		StreetBuilder streetBuilder = new StreetBuilder();
		ExecutableEntityProcessor processor = new ExecutableEntityProcessor(
				streetBuilder, nodeDB, wayDB, cityBoundary, null,
				new DefaultEntityFilter());
		processor.prepare();
		processor.execute(inputFile, inputFile, inputFile);
		List<Street> streets = streetBuilder.buildStreets();
		Set<OsmGeometry> ways = streetBuilder.getWays();

		System.out.println(String.format("Built %d streets from %d segments",
				streets.size(), ways.size()));
	}

}

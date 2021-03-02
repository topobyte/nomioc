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

package de.topobyte.nomioc.android.v2.task;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.jts.indexing.NearestNeighbourTesselation;
import de.topobyte.nomioc.android.v2.config.ConfigBuilder;
import de.topobyte.nomioc.android.v2.config.PoiClass;
import de.topobyte.nomioc.android.v2.config.PoiConfig;
import de.topobyte.nomioc.android.v2.config.PoiMapper;
import de.topobyte.nomioc.android.v2.hibernate.AndroidSessionFactory;
import de.topobyte.nomioc.android.v2.model.hibernate.Borough;
import de.topobyte.nomioc.android.v2.model.hibernate.BoroughSet;
import de.topobyte.nomioc.android.v2.model.hibernate.PoiType;
import de.topobyte.nomioc.android.v2.model.hibernate.PostalCode;
import de.topobyte.nomioc.android.v2.model.hibernate.PostalCodeSet;
import de.topobyte.nomioc.android.v2.poi.PoiCreator;
import de.topobyte.nomioc.android.v2.poi.TagChanger;
import de.topobyte.nomioc.android.v2.regions.Regions;
import de.topobyte.nomioc.android.v2.street.StreetBuilder;
import de.topobyte.osm4j.diskstorage.nodedb.NodeDB;
import de.topobyte.osm4j.diskstorage.vardb.VarDB;
import de.topobyte.osm4j.diskstorage.waydb.WayRecordWithTags;
import de.topobyte.osm4j.processing.entities.ExecutableEntityProcessor;
import de.topobyte.osm4j.processing.entities.filter.DefaultEntityFilter;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.processutils.ProcessLogger;
import de.topobyte.sqlitespatial.StatsTableManipulator;
import de.topobyte.sqliteutils.SqliteUtil;
import de.topobyte.various.utils.tasks.TaskException;

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 *
 */
public class CreateDatabase
{

	final static Logger logger = LoggerFactory.getLogger(CreateDatabase.class);

	private final static int MAX_NAME_LENGTH = 100;

	private OsmFileInput inputFile;
	private Geometry cityBoundary;
	private Path pathPoiConfig;
	private TagChanger tagPreprocessor;
	private Path nodesIndex;
	private Path nodesData;
	private Path waysIndex;
	private Path waysData;
	private Path databaseFile;
	private Path failedPolygonsDir;

	public CreateDatabase(OsmFileInput inputFile, Geometry cityBoundary,
			Path pathPoiConfig, Regions regions, TagChanger tagPreprocessor,
			Path nodesIndex, Path nodesData, Path waysIndex, Path waysData,
			Path databaseFile, Path failedPolygonsDir)
	{
		this.inputFile = inputFile;
		this.cityBoundary = cityBoundary;
		this.pathPoiConfig = pathPoiConfig;
		this.regions = regions;
		this.tagPreprocessor = tagPreprocessor;
		this.nodesIndex = nodesIndex;
		this.nodesData = nodesData;
		this.waysIndex = waysIndex;
		this.waysData = waysData;
		this.databaseFile = databaseFile;
		this.failedPolygonsDir = failedPolygonsDir;
	}

	private Regions regions;

	private PoiConfig poiConfig;

	private SessionFactory sfOutput;
	private PoiMapper poiMapper;
	private PoiCreator poiCreator;

	private NodeDB nodeDB = null;
	private VarDB<WayRecordWithTags> wayDB = null;

	private String prefix = "jdbc:sqlite:";

	public void prepare() throws IOException, DatabaseCreationException
	{
		logger.info("reading configuration");

		if (pathPoiConfig == null) {
			throw new DatabaseCreationException(
					"unable to resolve poi configuration");
		}

		logger.info("reading poi configuration");
		try {
			poiConfig = ConfigBuilder.build(pathPoiConfig);
		} catch (Exception e) {
			throw new DatabaseCreationException(
					"unable to open or read poi configuration", e);
		}

		try {
			nodeDB = new NodeDB(nodesData, nodesIndex);
			wayDB = new VarDB<>(waysData, waysIndex, new WayRecordWithTags(0));
		} catch (FileNotFoundException e) {
			throw new IOException(
					"Unable to open entity database: " + e.getMessage());
		}

		String hibernateConfigPath = "hib/hibernate_sqlite.android.v2.cfg.xml";

		String databasePath = databaseFile.toString();

		logger.info("opening target database: " + databaseFile);
		System.setProperty("dbpath", prefix + databasePath);
		AndroidSessionFactory androidSessionFactory = new AndroidSessionFactory(
				hibernateConfigPath);
		sfOutput = androidSessionFactory.getSessionFactory();

		logger.info("creating target schema");
		SchemaExport schemaExport = new SchemaExport(
				androidSessionFactory.getConfiguration());
		schemaExport.create(true, true);

		logger.info("transfering admin entity instances");
		sfOutput.getCurrentSession().beginTransaction();

		/*
		 * boroughs and postal codes
		 */

		for (Borough borough : regions.getBoroughIndex().values()) {
			sfOutput.getCurrentSession().persist(borough);
		}
		for (PostalCode postalCode : regions.getPostalCodeIndex().values()) {
			sfOutput.getCurrentSession().persist(postalCode);
		}

		logger.info("creating poi types");

		poiMapper = new PoiMapper(poiConfig);

		for (PoiClass type : poiConfig.getClasses()) {
			PoiType poiType = poiMapper.getPoiType(type.getIdentifier());
			sfOutput.getCurrentSession().persist(poiType);
		}
		for (PoiClass type : poiConfig.getClassesNoName()) {
			PoiType poiType = poiMapper.getPoiType(type.getIdentifier());
			sfOutput.getCurrentSession().persist(poiType);
		}

		for (String type : poiMapper.getExtraTypes()) {
			PoiType poiType = poiMapper.getPoiType(type);
			sfOutput.getCurrentSession().persist(poiType);
		}

		sfOutput.getCurrentSession().getTransaction().commit();
	}

	public void execute()
			throws IOException, DatabaseCreationException, TaskException
	{
		Map<Set<Borough>, BoroughSet> boroughSets = new HashMap<>();
		Map<Set<PostalCode>, PostalCodeSet> postalCodeSets = new HashMap<>();

		logger.info("creating streets");
		sfOutput.getCurrentSession().beginTransaction();

		StreetBuilder streetBuilder = new StreetBuilder(sfOutput, regions,
				boroughSets, postalCodeSets);
		ExecutableEntityProcessor processor = new ExecutableEntityProcessor(
				streetBuilder, nodeDB, wayDB, cityBoundary, failedPolygonsDir,
				new DefaultEntityFilter());
		processor.prepare();
		processor.execute(inputFile, inputFile, inputFile);
		streetBuilder.buildStreets();

		sfOutput.getCurrentSession().getTransaction().commit();

		NearestNeighbourTesselation<String> streetNameMap = streetBuilder
				.buildNameLookup();

		logger.info("creating pois");
		sfOutput.getCurrentSession().beginTransaction();

		poiCreator = new PoiCreator(sfOutput, regions, boroughSets,
				postalCodeSets, poiMapper, streetNameMap, MAX_NAME_LENGTH,
				tagPreprocessor);
		processor = new ExecutableEntityProcessor(poiCreator, nodeDB, wayDB,
				cityBoundary, failedPolygonsDir, new DefaultEntityFilter());
		processor.prepare();
		processor.execute(inputFile, inputFile, inputFile);
		poiCreator.buildPois();

		sfOutput.getCurrentSession().getTransaction().commit();
		sfOutput.close();

		createPoitypesIndex();

		try {
			SqliteUtil.vacuum(databaseFile.toFile());
		} catch (IOException e) {
			throw new DatabaseCreationException("Error while performing vacuum",
					e);
		}

		try {
			SqliteUtil.analyze(databaseFile.toFile());
		} catch (SQLException e) {
			throw new DatabaseCreationException(
					"Error while performing analyze", e);
		}

		manipulateStatsTable();

		poiMapper.getRestAnalyzer().analyzeOtherHistogram();
	}

	private void createPoitypesIndex() throws DatabaseCreationException
	{
		try {
			logger.info("Creating extra index on pois_types");
			Process process = Runtime.getRuntime().exec(new String[] {
					"sqlite3", databaseFile.toString(),
					"create index pois_types_type on pois_types (types_id)" });
			ProcessLogger processLogger = new ProcessLogger(process, logger);
			processLogger.waitForEnd();
		} catch (IOException e) {
			throw new DatabaseCreationException(
					"Error while creating extra index on poi types", e);
		}
	}

	private void manipulateStatsTable() throws DatabaseCreationException
	{
		try {
			StatsTableManipulator manipulator = new StatsTableManipulator();
			Connection connection = DriverManager
					.getConnection(prefix + databaseFile.toString());
			manipulator.manipulateStatsTable(connection);
			connection.close();
		} catch (SQLException e) {
			throw new DatabaseCreationException(
					"Error while manipulating stats table", e);
		}
	}

}

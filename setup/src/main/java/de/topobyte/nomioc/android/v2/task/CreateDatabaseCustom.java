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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.melon.io.ModTimes;
import de.topobyte.melon.paths.PathUtil;
import de.topobyte.nomioc.android.v2.regions.Regions;
import de.topobyte.nomioc.android.v2.regions.RegionsUtil;
import de.topobyte.osm4j.diskstorage.DbExtensions;
import de.topobyte.osm4j.diskstorage.EntityDbSetup;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.simplemapfile.core.EntityFile;
import de.topobyte.simplemapfile.xml.SmxFileReader;
import de.topobyte.various.utils.tasks.TaskException;
import de.topobyte.various.utils.tasks.TaskUtil;

public class CreateDatabaseCustom
{

	final static Logger logger = LoggerFactory
			.getLogger(CreateDatabaseCustom.class);

	private String pathFailingIntersections;
	private boolean updateOnly;

	private Path division;
	private Path failures;
	private Path inputFile;
	private Path boundaryFile;
	private Path regionsDir;
	private Path nodeDbDataFile;
	private Path nodeDbIndexFile;
	private Path wayDbDataFile;
	private Path wayDbIndexFile;
	private Path databaseFile;

	private Path pathPoiConfig;
	private String pathDivisionConfig;

	private Path basenameNodeDb;
	private Path basenameWayDb;

	public void setup(Path input, Path boundary, Path regions, Path output,
			Path pathPoiConfig, String pathFailingIntersections,
			boolean updateOnly, Path basenameNodeDb, Path basenameWayDb)
	{
		this.inputFile = input;
		this.boundaryFile = boundary;
		this.regionsDir = regions;
		this.databaseFile = output;
		this.basenameNodeDb = basenameNodeDb;
		this.basenameWayDb = basenameWayDb;
		this.pathPoiConfig = pathPoiConfig;
		this.pathFailingIntersections = pathFailingIntersections;
		this.updateOnly = updateOnly;
	}

	public void prepare() throws TaskException
	{
		logger.info("input file: " + inputFile);
		logger.info("output file: " + databaseFile);
		logger.info("nodes database: " + basenameNodeDb);
		logger.info("ways database: " + basenameWayDb);

		/*
		 * check some preconditions
		 */

		TaskUtil.ensureExistsAndIsReadable(inputFile, "input file");

		TaskUtil.ensureDirectoryExistsAndIsWritable(basenameNodeDb.getParent(),
				"temporary directory");

		if (pathDivisionConfig != null) {
			TaskUtil.ensureDirectoryExistsAndIsWritable(division,
					"division directory");
		}

		TaskUtil.ensureDirectoryExistsAndIsWritable(basenameNodeDb.getParent(),
				"nodedb parent");
		TaskUtil.ensureDirectoryExistsAndIsWritable(basenameWayDb.getParent(),
				"waydb parent");

		failures = null;
		if (pathFailingIntersections != null) {
			failures = Paths.get(pathFailingIntersections);
			TaskUtil.ensureDirectoryExistsAndIsWritable(failures,
					"failures directory");
		}

		nodeDbDataFile = extend(basenameNodeDb, DbExtensions.EXTENSION_DATA);
		nodeDbIndexFile = extend(basenameNodeDb, DbExtensions.EXTENSION_INDEX);
		wayDbDataFile = extend(basenameWayDb, DbExtensions.EXTENSION_DATA);
		wayDbIndexFile = extend(basenameWayDb, DbExtensions.EXTENSION_INDEX);
	}

	private Path extend(Path basename, String extension)
	{
		return basename.resolveSibling(basename.getFileName() + extension);
	}

	public void execute()
			throws IOException, DatabaseCreationException, TaskException
	{
		logger.info("data file: " + inputFile);
		logger.info("database file: " + databaseFile);

		TaskUtil.ensureExistsAndIsReadable(inputFile, "input file");

		PathUtil.createParentDirectories(databaseFile);

		if (Files.exists(databaseFile)) {
			if (updateOnly) {
				if (ModTimes.isNewerThan(databaseFile, inputFile)) {
					logger.info("database file is newer than input, skipping");
					return;
				}
			}
			boolean delete = Files.deleteIfExists(databaseFile);
			if (!delete) {
				logger.warn("unable to delete old database");
			}
		}

		Path parent = databaseFile.toAbsolutePath().getParent();
		TaskUtil.ensureDirectoryExistsAndIsWritable(parent, "output directory");

		// end setup

		/*
		 * create node and way databases
		 */

		logger.info("creating databases");

		logger.info("nodedb index: " + nodeDbIndexFile);
		logger.info("nodedb data: " + nodeDbDataFile);
		logger.info("waydb index: " + wayDbIndexFile);
		logger.info("waydb data: " + wayDbDataFile);

		try {
			EntityDbSetup.createNodeDb(inputFile, nodeDbIndexFile,
					nodeDbDataFile);
		} catch (IOException e) {
			throw new DatabaseCreationException(
					"errror while creating node database", e);
		}

		try {
			EntityDbSetup.createWayDb(inputFile, wayDbIndexFile, wayDbDataFile,
					true);
		} catch (IOException e) {
			throw new DatabaseCreationException(
					"errror while creating way database", e);
		}

		OsmFileInput input = new OsmFileInput(inputFile, FileFormat.TBO);

		logger.info("loading city boundary");
		Geometry cityBoundary = null;

		logger.info("smx file: " + boundaryFile);
		try {
			EntityFile entity = SmxFileReader.read(boundaryFile.toFile());
			cityBoundary = entity.getGeometry();
		} catch (Exception e) {
			throw new DatabaseCreationException("unable to read city file", e);
		}

		Regions regions = new Regions();
		if (regionsDir != null) {
			regions = RegionsUtil.loadFromDirectory(regionsDir);
		}

		/*
		 * Create database
		 */

		CreateDatabase creator = new CreateDatabase(input, cityBoundary,
				pathPoiConfig, regions, null, nodeDbIndexFile, nodeDbDataFile,
				wayDbIndexFile, wayDbDataFile, databaseFile, failures);

		try {
			creator.prepare();
		} catch (IOException e) {
			throw new DatabaseCreationException(
					"error while creating file, prepare()", e);
		}
		try {
			creator.execute();
		} catch (IOException e) {
			throw new DatabaseCreationException(
					"error while creating file, execute()", e);
		}
	}

}

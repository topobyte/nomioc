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

package de.topobyte.nomioc.executables.android;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.nomioc.android.v2.task.CreateDatabaseCustom;
import de.topobyte.nomioc.android.v2.task.DatabaseCreationException;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;
import de.topobyte.various.utils.tasks.TaskException;

public class DatabaseCreatorCustom
{

	final static Logger logger = LoggerFactory
			.getLogger(DatabaseCreatorCustom.class);

	final static String HELP_MESSAGE = DatabaseCreatorCustom.class
			.getSimpleName() + " [options]";

	final static String OPTION_INPUT = "input";
	final static String OPTION_BOUNDARY = "boundary";
	final static String OPTION_REGIONS = "regions";
	final static String OPTION_CONFIG = "config";
	final static String OPTION_OUTPUT = "output";
	final static String OPTION_NODEDB = "node-db";
	final static String OPTION_WAYDB = "way-db";
	final static String OPTION_FAILED_INTERSECTIONS = "failures";
	final static String OPTION_UPDATE = "update-only";

	public static void addOptions(Options options)
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_INPUT, true, true, "file", "input");
		OptionHelper.addL(options, OPTION_BOUNDARY, true, true, "file", "boundary");
		OptionHelper.addL(options, OPTION_REGIONS, true, false, "directory", "regions (admin boundaries and postal codes)");
		OptionHelper.addL(options, OPTION_CONFIG, true, true, "file", "config");
		OptionHelper.addL(options, OPTION_OUTPUT, true, true, "directory", "where to put the results");
		OptionHelper.addL(options, OPTION_NODEDB, true, true, "basename", "node database");
		OptionHelper.addL(options, OPTION_WAYDB, true, true, "basename", "way database");
		OptionHelper.addL(options, OPTION_FAILED_INTERSECTIONS, true, false, "directory", "where to dump failing intersections");
		OptionHelper.addL(options, OPTION_UPDATE, false, false, "boolean", "whether to consider only items with timestamps older than input file");
		// @formatter:on
	}

	public static class Arguments
	{
		public String pathInput;
		public String pathBoundary;
		public String pathRegions;
		public String pathConfig;
		public String pathOutput;
		public String pathNodeDB;
		public String pathWayDB;
		public String pathFailingIntersections;
		public boolean updateOnly;
	}

	public static Arguments parse(CommandLine line)
	{
		Arguments args = new Arguments();

		args.pathInput = line.getOptionValue(OPTION_INPUT);
		args.pathBoundary = line.getOptionValue(OPTION_BOUNDARY);
		args.pathRegions = line.getOptionValue(OPTION_REGIONS);
		args.pathConfig = line.getOptionValue(OPTION_CONFIG);
		args.pathOutput = line.getOptionValue(OPTION_OUTPUT);
		args.pathNodeDB = line.getOptionValue(OPTION_NODEDB);
		args.pathWayDB = line.getOptionValue(OPTION_WAYDB);
		args.pathFailingIntersections = line
				.getOptionValue(OPTION_FAILED_INTERSECTIONS);
		args.updateOnly = line.hasOption(OPTION_UPDATE);

		return args;
	}

	public static void main(String[] args)
			throws IOException, DatabaseCreationException, TaskException
	{
		Options options = new Options();
		addOptions(options);

		CommandLine commandLine = null;
		try {
			commandLine = new DefaultParser().parse(options, args);
		} catch (ParseException e) {
			System.out
					.println("unable to parse command line: " + e.getMessage());
			new HelpFormatter().printHelp(HELP_MESSAGE, options);
			System.exit(1);
		}
		if (commandLine == null) {
			return;
		}

		Arguments arguments = parse(commandLine);

		Path pathRegions = arguments.pathRegions == null ? null
				: Paths.get(arguments.pathRegions);

		CreateDatabaseCustom task = new CreateDatabaseCustom();
		task.setup(Paths.get(arguments.pathInput),
				Paths.get(arguments.pathBoundary), pathRegions,
				Paths.get(arguments.pathOutput),
				Paths.get(arguments.pathConfig),
				arguments.pathFailingIntersections, arguments.updateOnly,
				Paths.get(arguments.pathNodeDB),
				Paths.get(arguments.pathWayDB));
		task.prepare();
		task.execute();
	}

}

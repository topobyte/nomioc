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

package de.topobyte.nomioc;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.topobyte.nomioc.android.v2.task.CreateDatabaseCustom;
import de.topobyte.nomioc.android.v2.task.DatabaseCreationException;
import de.topobyte.system.utils.SystemPaths;
import de.topobyte.various.utils.tasks.TaskException;

public class TestCreateDatabaseStadtportalBerlin
{

	public static void main(String[] args)
			throws IOException, TaskException, DatabaseCreationException
	{
		Path pathInput = Paths.get(
				"/data/stadtportal/germany/region-data/62422-Berlin/data.tbo");
		Path pathOutput = Paths.get("/tmp/berlin/Berlin.sqlite");
		Path pathBoundary = Paths.get(
				"/data/stadtportal/germany/output/cities/62422-Berlin.smx");
		Path pathPoiConfig = SystemPaths.HOME
				.resolve("git/oxygen/regions/world/configuration/pois.xml");

		boolean updateOnly = false;

		Path pathTemp = Paths.get("/tmp/berlin");
		Path baseNodeDb = pathTemp.resolve("nomioc.nodedb");
		Path baseWayDb = pathTemp.resolve("nomioc.waydb");

		CreateDatabaseCustom filter = new CreateDatabaseCustom();
		filter.setup(pathInput, pathBoundary, pathOutput, pathPoiConfig, null,
				updateOnly, baseNodeDb, baseWayDb);
		filter.prepare();
		filter.execute();
	}

}

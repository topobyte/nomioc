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

import de.topobyte.nomioc.android.v2.task.CreateDatabaseCustom;
import de.topobyte.nomioc.android.v2.task.DatabaseCreationException;
import de.topobyte.system.utils.SystemPaths;
import de.topobyte.various.utils.tasks.TaskException;

public class TestCreateDatabaseForst
{

	public static void main(String[] args)
			throws IOException, DatabaseCreationException, TaskException
	{
		Path dir = SystemPaths.HOME.resolve("misc/stadtplan/forst");
		Path pathInput = dir.resolve("Forst.tbo");
		Path pathOutput = dir.resolve("Forst.sqlite");
		Path pathBoundary = dir.resolve("Forst.smx");
		Path pathPoiConfig = SystemPaths.HOME
				.resolve("git/oxygen/regions/world/configuration/pois.xml");

		boolean updateOnly = false;

		Path pathTemp = dir.resolve("tmp");
		Path baseNodeDb = pathTemp.resolve("nomioc.nodedb");
		Path baseWayDb = pathTemp.resolve("nomioc.waydb");

		CreateDatabaseCustom filter = new CreateDatabaseCustom();
		filter.setup(pathInput, pathBoundary, pathOutput, pathPoiConfig, null,
				updateOnly, baseNodeDb, baseWayDb);
		filter.prepare();
		filter.execute();
	}

}

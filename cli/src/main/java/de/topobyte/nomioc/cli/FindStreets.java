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

package de.topobyte.nomioc.cli;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.luqe.iface.IConnection;
import de.topobyte.luqe.iface.QueryException;
import de.topobyte.nomioc.luqe.dao.Dao;
import de.topobyte.nomioc.luqe.dao.MatchMode;
import de.topobyte.nomioc.luqe.dao.SortOrder;
import de.topobyte.nomioc.luqe.model.SqRoad;

public class FindStreets
{

	final static Logger logger = LoggerFactory.getLogger(FindStreets.class);

	public static void main(String[] args)
	{
		if (args.length < 2) {
			System.out.println("usage: " + FindStreets.class.getSimpleName()
					+ " <database> <query>");
			System.exit(1);
		}

		String pathDatabase = args[0];
		String query = args[1];

		Util.initJdbc();

		IConnection db = null;
		try {
			db = Util.openConnection(pathDatabase);
		} catch (SQLException e) {
			logger.error("Error while opening database", e);
			System.exit(1);
		}

		try {
			query(db, query);
		} catch (Exception e) {
			logger.error("Error while performing query", e);
			System.exit(1);
		}
	}

	private static void query(IConnection db, String query)
			throws QueryException
	{
		List<SqRoad> roads = Dao.getRoads(db, query, MatchMode.ANYWHERE,
				SortOrder.ASCENDING, 100, 0);

		for (SqRoad road : roads) {
			String bs = road.getBoroughsAsString(db);
			System.out.println(road.getNameSafe() + " (" + bs + ")");
		}
	}

}

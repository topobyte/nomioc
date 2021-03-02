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

import com.slimjars.dist.gnu.trove.iterator.TIntIterator;
import com.slimjars.dist.gnu.trove.map.TIntObjectMap;
import com.slimjars.dist.gnu.trove.map.hash.TIntObjectHashMap;
import com.slimjars.dist.gnu.trove.set.TIntSet;

import de.topobyte.luqe.iface.IConnection;
import de.topobyte.luqe.iface.QueryException;
import de.topobyte.nomioc.luqe.dao.Dao;
import de.topobyte.nomioc.luqe.dao.MatchMode;
import de.topobyte.nomioc.luqe.dao.SortOrder;
import de.topobyte.nomioc.luqe.model.SqPoi;
import de.topobyte.nomioc.luqe.model.SqPoiType;

public class FindPois
{

	final static Logger logger = LoggerFactory.getLogger(FindPois.class);

	public static void main(String[] args)
	{
		if (args.length < 2) {
			System.out.println("usage: " + FindPois.class.getSimpleName()
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
		TIntObjectMap<String> typeMap = new TIntObjectHashMap<>();
		List<SqPoiType> typeList = Dao.getTypes(db);
		for (SqPoiType type : typeList) {
			typeMap.put(type.getId(), type.getName());
		}

		List<SqPoi> pois = Dao.getPois(db, query, MatchMode.ANYWHERE,
				SortOrder.ASCENDING, 100, 0);
		pois = Dao.fillTypes(db, pois);

		for (SqPoi poi : pois) {
			String bs = poi.getBoroughsAsString(db);
			System.out.println(poi.getNameSafe() + " (" + bs + ")");
			TIntSet types = poi.getTypes();
			TIntIterator iter = types.iterator();
			System.out.print("  ");
			while (iter.hasNext()) {
				String type = typeMap.get(iter.next());
				System.out.print(type);
				if (iter.hasNext()) {
					System.out.print(", ");
				}
			}
			System.out.println();
		}
	}

}

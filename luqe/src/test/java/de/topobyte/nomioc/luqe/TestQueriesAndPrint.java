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

package de.topobyte.nomioc.luqe;

import java.nio.file.Path;
import java.util.List;

import de.topobyte.luqe.iface.IConnection;
import de.topobyte.nomioc.luqe.dao.Dao;
import de.topobyte.nomioc.luqe.model.SqPoiType;
import de.topobyte.system.utils.SystemPaths;

public class TestQueriesAndPrint
{

	public static void main(String[] args) throws Exception
	{
		TestQueriesAndPrint task = new TestQueriesAndPrint();
		task.execute();
	}

	private void execute() throws Exception
	{
		Path path = SystemPaths.CWD;
		Path pathDatabase = path.resolve("src/test/resources/Bayreuth.sqlite");

		IConnection db = Util.openConnection(pathDatabase.toString());

		List<SqPoiType> types = Dao.getTypes(db);

		for (SqPoiType type : types) {
			int numberOfPois = Dao.getNumberOfPois(db, type.getId());
			System.out.println(
					String.format("%s: %d", type.getName(), numberOfPois));
		}

		QueryTester tester = new QueryTester(db);

		int numRoads = Dao.getNumberOfRoads(db);
		System.out.println("Roads: " + numRoads);

		System.out.println("# Anything containing 'gold'");
		tester.findByName("gold");

		System.out.println("# Restaurants containing 'gold'");
		tester.findByTypeAndName("restaurant", "gold");
	}

}

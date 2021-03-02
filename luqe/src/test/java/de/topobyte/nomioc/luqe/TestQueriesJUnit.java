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

import org.junit.Before;
import org.junit.Test;

import de.topobyte.luqe.iface.IConnection;
import de.topobyte.system.utils.SystemPaths;

public class TestQueriesJUnit
{

	private IConnection db;
	private QueryTester tester;

	@Before
	public void prepare() throws Exception
	{
		Path path = SystemPaths.CWD;
		Path pathDatabase = path.resolve("src/test/resources/Bayreuth.sqlite");

		db = Util.openConnection(pathDatabase.toString());
		tester = new QueryTester(db);
	}

	@Test
	public void testFindByName() throws Exception
	{
		tester.findByName("gold", 11);
	}

	@Test
	public void testFindByTypeAndName() throws Exception
	{
		tester.findByTypeAndName("restaurant", "gold", 5);
	}

}

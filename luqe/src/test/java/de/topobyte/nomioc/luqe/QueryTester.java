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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import com.slimjars.dist.gnu.trove.iterator.TIntIterator;
import com.slimjars.dist.gnu.trove.set.TIntSet;
import com.slimjars.dist.gnu.trove.set.hash.TIntHashSet;

import de.topobyte.luqe.iface.IConnection;
import de.topobyte.luqe.iface.QueryException;
import de.topobyte.nomioc.luqe.dao.Dao;
import de.topobyte.nomioc.luqe.dao.MatchMode;
import de.topobyte.nomioc.luqe.dao.SortOrder;
import de.topobyte.nomioc.luqe.model.SqPoi;
import de.topobyte.nomioc.luqe.model.SqPoiType;

public class QueryTester
{

	private IConnection db;
	private Map<String, Integer> typeToId = new HashMap<>();
	private Map<Integer, String> idToType = new HashMap<>();

	public QueryTester(IConnection db) throws QueryException
	{
		this.db = db;

		List<SqPoiType> types = Dao.getTypes(db);

		for (SqPoiType type : types) {
			typeToId.put(type.getName(), type.getId());
			idToType.put(type.getId(), type.getName());
		}
	}

	public void findByName(String query) throws QueryException
	{
		findByName(query, -1);
	}

	public void findByName(String query, int numExpected) throws QueryException
	{
		List<SqPoi> pois = Dao.getPois(db, query, MatchMode.ANYWHERE,
				SortOrder.ASCENDING, 1000, 0);
		pois = Dao.fillTypes(db, pois);
		for (SqPoi poi : pois) {
			System.out.println(String.format("%s: %s", poi.getSimpleName(),
					getTypeNames(poi)));
		}
		int num = Dao.getNumberOfPois(db, "gold", MatchMode.ANYWHERE);
		System.out.println(String.format("getNumberOfPois(): %d", num));

		if (numExpected >= 0) {
			Assert.assertEquals(numExpected, num);
			Assert.assertEquals(numExpected, pois.size());
		}

		Assert.assertEquals(num, pois.size());
	}

	public void findByTypeAndName(String type, String query)
			throws QueryException
	{
		findByTypeAndName(type, query, -1);
	}

	public void findByTypeAndName(String type, String query, int numExpected)
			throws QueryException
	{
		TIntSet qtypes = new TIntHashSet();
		qtypes.add(typeToId.get(type));
		List<SqPoi> pois = Dao.getPois(db, query, MatchMode.ANYWHERE,
				SortOrder.ASCENDING, qtypes, 1000, 0);
		pois = Dao.fillTypes(db, pois);
		for (SqPoi poi : pois) {
			System.out.println(String.format("%s: %s", poi.getSimpleName(),
					getTypeNames(poi)));
		}
		int num = Dao.getNumberOfPois(db, "gold", MatchMode.ANYWHERE, qtypes);
		System.out.println(String.format("getNumberOfPois(): %d", num));

		if (numExpected >= 0) {
			Assert.assertEquals(numExpected, num);
			Assert.assertEquals(numExpected, pois.size());
		}

		Assert.assertEquals(num, pois.size());
	}

	private List<String> getTypeNames(SqPoi poi)
	{
		List<String> typeNames = new ArrayList<>();
		TIntIterator iterator = poi.getTypes().iterator();
		while (iterator.hasNext()) {
			typeNames.add(idToType.get(iterator.next()));
		}
		return typeNames;
	}

}

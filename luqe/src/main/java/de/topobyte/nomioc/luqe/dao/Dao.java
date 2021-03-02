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

package de.topobyte.nomioc.luqe.dao;

import java.util.ArrayList;
import java.util.List;

import com.slimjars.dist.gnu.trove.TIntCollection;
import com.slimjars.dist.gnu.trove.iterator.TIntIterator;
import com.slimjars.dist.gnu.trove.list.TIntList;
import com.slimjars.dist.gnu.trove.map.TIntObjectMap;
import com.slimjars.dist.gnu.trove.map.hash.TIntObjectHashMap;
import com.slimjars.dist.gnu.trove.set.TIntSet;
import com.slimjars.dist.gnu.trove.set.hash.TIntHashSet;

import de.topobyte.adt.geo.BBox;
import de.topobyte.luqe.iface.IConnection;
import de.topobyte.luqe.iface.IPreparedStatement;
import de.topobyte.luqe.iface.IResultSet;
import de.topobyte.luqe.iface.QueryException;
import de.topobyte.mercatorcoordinates.GeoConv;
import de.topobyte.nomioc.luqe.model.SqLabel;
import de.topobyte.nomioc.luqe.model.SqPoi;
import de.topobyte.nomioc.luqe.model.SqPoiType;
import de.topobyte.nomioc.luqe.model.SqRoad;
import de.topobyte.sqlitespatial.spatialindex.access.SpatialIndex;

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 * 
 */
public class Dao
{

	/**
	 * Fetch a list of resulting roads.
	 * 
	 * @param db
	 *            a Connection to use for the query
	 * @param querystring
	 *            the name to search for.
	 * @param limit
	 *            a number limiting the number of results.
	 * @return the list of results.
	 * @throws QueryException
	 */
	public static List<SqRoad> getRoads(IConnection db, String querystring,
			MatchMode matchMode, SortOrder order, int limit, int offset)
			throws QueryException
	{
		String stmt = "select s.id, s.x, s.y, s.name, s.simple_name"
				+ " from streets s" + " where s.simple_name like ?"
				+ " order by s.simple_name " + order(order) + " limit " + limit
				+ " offset " + offset;

		IPreparedStatement statement = db.prepareStatement(stmt);
		statement.setString(1, getArgument(querystring, matchMode));

		IResultSet results = statement.executeQuery();
		List<SqRoad> list = listOfRoadFromResults(results);
		results.close();
		return list;
	}

	public static List<SqRoad> getRoadsInIndex(IConnection db,
			String querystring, MatchMode matchMode, TIntSet sids, int limit,
			int offset) throws QueryException
	{
		String questionMarks = buildList(sids.size());

		String stmt = "select s.id, s.x, s.y, s.name, s.simple_name"
				+ " from streets s" + " where s.simple_name like ?"
				+ " and sid in (" + questionMarks + ")" + " limit " + limit
				+ " offset " + offset;

		IPreparedStatement statement = db.prepareStatement(stmt);
		statement.setString(1, getArgument(querystring, matchMode));

		addParameters(statement, 2, sids.iterator());

		IResultSet results = statement.executeQuery();
		List<SqRoad> list = listOfRoadFromResults(results);
		results.close();
		return list;
	}

	/**
	 * Fetch a list of resulting POIs.
	 * 
	 * @param db
	 *            a Connection to use for the query
	 * @param querystring
	 *            the name to search for.
	 * @param limit
	 *            a number limiting the number of results.
	 * @param offset
	 *            a number setting the number of results to omit at the
	 *            beginning
	 * @return the list of results.
	 * @throws QueryException
	 */
	public static List<SqPoi> getPois(IConnection db, String querystring,
			MatchMode matchMode, SortOrder order, int limit, int offset)
			throws QueryException
	{
		String stmt = "select p.id, p.x, p.y,"
				+ " p.name, p.simple_name, p.phone, p.website" + " from pois p"
				+ " where p.simple_name like ?" + " order by p.simple_name "
				+ order(order) + " limit " + limit + " offset " + offset;

		IPreparedStatement statement = db.prepareStatement(stmt);
		statement.setString(1, getArgument(querystring, matchMode));

		IResultSet results = statement.executeQuery();
		List<SqPoi> list = listOfPoiFromResults(results);
		results.close();
		return list;
	}

	public static List<SqPoi> getPois(IConnection db, String querystring,
			MatchMode matchMode, SortOrder order, TIntSet types, int limit,
			int offset) throws QueryException
	{
		String questionMarks = buildList(types.size());

		String stmt = "select p.id, p.x, p.y,"
				+ " p.name, p.simple_name, p.phone, p.website"
				+ " from pois p JOIN pois_types pt ON (p.id = pt.pois_id)"
				+ " where p.simple_name like ?" + " and pt.types_id in ("
				+ questionMarks + ")" + " order by p.simple_name "
				+ order(order) + " limit " + limit + " offset " + offset;

		IPreparedStatement statement = db.prepareStatement(stmt);
		statement.setString(1, getArgument(querystring, matchMode));
		addParameters(statement, 2, types.iterator());

		IResultSet results = statement.executeQuery();
		List<SqPoi> list = listOfPoiFromResults(results);
		results.close();
		return list;
	}

	public static List<SqPoi> getPoisInIndex(IConnection db, String querystring,
			MatchMode matchMode, TIntSet sids, int limit, int offset)
			throws QueryException
	{
		String questionMarks = buildList(sids.size());

		String stmt = "select p.id, p.x, p.y,"
				+ " p.name, p.simple_name, p.phone, p.website" + " from pois p"
				+ " where p.simple_name like ?" + " and sid in ("
				+ questionMarks + ")" + " limit " + limit + " offset " + offset;

		IPreparedStatement statement = db.prepareStatement(stmt);
		statement.setString(1, getArgument(querystring, matchMode));
		addParameters(statement, 2, sids.iterator());

		IResultSet results = statement.executeQuery();
		List<SqPoi> list = listOfPoiFromResults(results);
		results.close();
		return list;
	}

	public static List<SqPoi> getPoisInIndex(IConnection db, String querystring,
			MatchMode matchMode, TIntSet sids, TIntSet types, int limit,
			int offset) throws QueryException
	{
		String questionMarks1 = buildList(sids.size());
		String questionMarks2 = buildList(types.size());

		String stmt = "select p.id, p.x, p.y,"
				+ " p.name, p.simple_name, p.phone, p.website"
				+ " from pois p JOIN pois_types pt ON (p.id = pt.pois_id)"
				+ " where p.simple_name like ?" + " and sid in ("
				+ questionMarks1 + ")" + " and pt.types_id in ("
				+ questionMarks2 + ")" + " limit " + limit + " offset "
				+ offset;

		IPreparedStatement statement = db.prepareStatement(stmt);
		statement.setString(1, getArgument(querystring, matchMode));
		addParameters(statement, 2, sids.iterator());
		addParameters(statement, 2 + sids.size(), types.iterator());

		IResultSet results = statement.executeQuery();
		List<SqPoi> list = listOfPoiFromResults(results);
		results.close();
		return list;
	}

	public static List<SqPoi> fillTypes(IConnection db, List<SqPoi> pois)
			throws QueryException
	{
		if (pois.isEmpty()) {
			return pois;
		}

		String stmt = "select pt.pois_id, pt.types_id from pois_types pt"
				+ " WHERE pois_id in (" + buildList(pois.size()) + ")"
				+ " ORDER BY pois_id";

		IPreparedStatement statement = db.prepareStatement(stmt);
		for (int i = 0; i < pois.size(); i++) {
			statement.setInt(i + 1, pois.get(i).getId());
		}

		List<SqPoi> ps = new ArrayList<>();
		for (SqPoi poi : pois) {
			ps.add(new SqPoi(poi));
		}

		TIntObjectMap<SqPoi> map = new TIntObjectHashMap<>();
		for (SqPoi poi : ps) {
			map.put(poi.getId(), poi);
		}

		IResultSet results = statement.executeQuery();
		while (results.next()) {
			int id = results.getInt(1);
			int type = results.getInt(2);
			SqPoi poi = map.get(id);
			TIntSet types = poi.getTypes();
			if (types == null) {
				types = new TIntHashSet();
				poi.setTypes(types);
			}
			types.add(type);
		}
		results.close();

		return ps;
	}

	public static List<SqLabel> getLabels(IConnection db,
			SpatialIndex spatialIndex, BBox bbox, TIntList ids)
			throws QueryException
	{
		int minX = GeoConv.mercatorFromLongitude(bbox.getLon1());
		int maxX = GeoConv.mercatorFromLongitude(bbox.getLon2());
		int minY = GeoConv.mercatorFromLatitude(bbox.getLat1());
		int maxY = GeoConv.mercatorFromLatitude(bbox.getLat2());

		TIntSet sids = spatialIndex.getSpatialIndexIds(minX, maxX, minY, maxY);

		String questionMarks1 = buildList(ids.size());
		String questionMarks2 = buildList(sids.size());

		String stmt = "select pois.id, pois_types.types_id,"
				+ " pois.x, pois.y, pois.name, pois.simple_name"
				+ " from pois join pois_types on pois.id=pois_types.pois_id"
				+ " where pois_types.types_id in (" + questionMarks1 + ")"
				+ " and sid in (" + questionMarks2 + ")"
				+ " and pois.y between ? and ? and pois.x between ? and ?";

		IPreparedStatement statement = db.prepareStatement(stmt);

		List<String> parameters = new ArrayList<>();
		addParameters(parameters, ids);
		addParameters(parameters, sids);

		parameters.add(Integer.toString(minY));
		parameters.add(Integer.toString(maxY));
		parameters.add(Integer.toString(minX));
		parameters.add(Integer.toString(maxX));

		String[] args = parameters.toArray(new String[0]);
		statement.setArguments(args);

		IResultSet results = statement.executeQuery();
		List<SqLabel> list = listOfLabelFromResults(results);
		results.close();
		return list;
	}

	public static List<SqLabel> getLabels(IConnection db, BBox bbox,
			TIntList ids) throws QueryException
	{
		int minX = GeoConv.mercatorFromLongitude(bbox.getLon1());
		int maxX = GeoConv.mercatorFromLongitude(bbox.getLon2());
		int minY = GeoConv.mercatorFromLatitude(bbox.getLat1());
		int maxY = GeoConv.mercatorFromLatitude(bbox.getLat2());

		String questionMarks1 = buildList(ids.size());

		String stmt = "select pois.id, pois_types.types_id,"
				+ " pois.x, pois.y, pois.name, pois.simple_name"
				+ " from pois join pois_types on pois.id=pois_types.pois_id"
				+ " where pois_types.types_id in (" + questionMarks1 + ")"
				+ " and pois.y between ? and ? and pois.x between ? and ?";

		IPreparedStatement statement = db.prepareStatement(stmt);

		List<String> parameters = new ArrayList<>();
		addParameters(parameters, ids);

		parameters.add(Integer.toString(minY));
		parameters.add(Integer.toString(maxY));
		parameters.add(Integer.toString(minX));
		parameters.add(Integer.toString(maxX));

		String[] args = parameters.toArray(new String[0]);
		statement.setArguments(args);

		IResultSet results = statement.executeQuery();
		List<SqLabel> list = listOfLabelFromResults(results);
		results.close();
		return list;
	}

	public static List<SqPoiType> getTypes(IConnection db) throws QueryException
	{
		List<SqPoiType> types = new ArrayList<>();

		String stmt = "select * from poitypes";
		IPreparedStatement statement = db.prepareStatement(stmt);

		IResultSet results = statement.executeQuery();
		while (results.next()) {
			int id = results.getInt(1);
			String name = results.getString(2);
			types.add(new SqPoiType(id, name));
		}

		return types;
	}

	/**
	 * Fetch the id of a class of POIs to use in subsequent queries for POIs.
	 * 
	 * @param db
	 *            the database to query
	 * @param typeIdentifier
	 *            the type identifier in the database
	 * @return the id of this POI-class of -1 if the class cannot be found in
	 *         the database.
	 * @throws QueryException
	 */
	public static int getPoiTypeId(IConnection db, String typeIdentifier)
			throws QueryException
	{
		String stmt = "select id from poitypes " + " where poitypes.name = ?";

		IPreparedStatement statement = db.prepareStatement(stmt);
		statement.setString(1, typeIdentifier);

		IResultSet results = statement.executeQuery();
		int id = -1;
		if (results.next()) {
			id = results.getInt(1);
		}
		results.close();
		return id;
	}

	private static List<SqRoad> listOfRoadFromResults(IResultSet results)
			throws QueryException
	{
		List<SqRoad> list = new ArrayList<>();
		while (results.next()) {
			SqRoad road = new SqRoad();
			list.add(road);
			road.setId(results.getInt(1));
			road.setX(results.getInt(2));
			road.setY(results.getInt(3));
			road.setName(results.getString(4));
			road.setSimpleName(results.getString(5));
		}
		return list;
	}

	public static List<SqPoi> listOfPoiFromResults(IResultSet results)
			throws QueryException
	{
		List<SqPoi> list = new ArrayList<>();
		while (results.next()) {
			SqPoi poi = new SqPoi();
			list.add(poi);
			poi.setId(results.getInt(1));
			poi.setX(results.getInt(2));
			poi.setY(results.getInt(3));
			poi.setName(results.getString(4));
			poi.setSimpleName(results.getString(5));
			poi.setPhone(results.getString(6));
			poi.setWebsite(results.getString(7));
		}
		return list;
	}

	private static List<SqLabel> listOfLabelFromResults(IResultSet results)
			throws QueryException
	{
		List<SqLabel> list = new ArrayList<>();
		while (results.next()) {
			SqLabel label = new SqLabel();
			list.add(label);
			label.setId(results.getInt(1));
			label.setType(results.getInt(2));
			label.setX(results.getInt(3));
			label.setY(results.getInt(4));
			String name = results.getString(5);
			if (name == null) {
				name = results.getString(6);
			}
			label.setName(name);
		}
		return list;
	}

	private static String getArgument(String querystring, MatchMode matchMode)
	{
		switch (matchMode) {
		default:
		case EXACT:
			return querystring;
		case ANYWHERE:
			return "%" + querystring + "%";
		case BEGIN_WITH:
			return querystring + "%";
		case END_WITH:
			return "%" + querystring;
		}
	}

	private static String order(SortOrder order)
	{
		if (order == SortOrder.ASCENDING) {
			return "ASC";
		} else {
			return "DESC";
		}
	}

	private static String buildList(int n)
	{
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < n - 1; i++) {
			buffer.append("?");
			buffer.append(",");
		}
		buffer.append("?");
		return buffer.toString();
	}

	private static void addParameters(IPreparedStatement statement, int idx,
			TIntIterator iterator) throws QueryException
	{
		while (iterator.hasNext()) {
			statement.setInt(idx++, iterator.next());
		}
	}

	private static void addParameters(List<String> params, TIntCollection ids)
	{
		TIntIterator iterator = ids.iterator();
		while (iterator.hasNext()) {
			params.add(Integer.toString(iterator.next()));
		}
	}

	/*
	 * Legacy methods, currently not needed
	 */

	public static List<SqPoi> getPoisInBbox(IConnection db, String querystring,
			MatchMode matchMode, TIntSet sids, int minY, int maxY, int minX,
			int maxX, int limit, int offset) throws QueryException
	{
		String questionMarks = buildList(sids.size());

		String stmt = "select p.id, p.x, p.y,"
				+ " p.name, p.simple_name, p.phone, p.website" + " from pois p"
				+ " where p.simple_name like ?" + " and p.y between ? and ?"
				+ " and p.x between ? and ?" + " and sid in (" + questionMarks
				+ ")" + " limit " + limit + " offset " + offset;

		IPreparedStatement statement = db.prepareStatement(stmt);
		statement.setString(1, getArgument(querystring, matchMode));
		statement.setInt(2, minY);
		statement.setInt(3, maxY);
		statement.setInt(4, minX);
		statement.setInt(5, maxX);
		addParameters(statement, 6, sids.iterator());

		IResultSet results = statement.executeQuery();
		List<SqPoi> list = listOfPoiFromResults(results);
		results.close();
		return list;
	}

	// TODO: implement types clause
	public static List<SqPoi> getPoisInBbox(IConnection db, String querystring,
			MatchMode matchMode, TIntSet types, TIntSet sids, int minY,
			int maxY, int minX, int maxX, int limit, int offset)
			throws QueryException
	{
		String stmt = "select p.id, p.x, p.y,"
				+ " p.name, p.simple_name, p.phone, p.website"
				+ " from pois p JOIN pois_types pt ON (p.id = pt.pois_id)"
				+ " JOIN poitypes t ON (pt.types_id = t.id)"
				+ " where p.simple_name like ?" + " and p.y between ? and ?"
				+ " and p.x between ? and ?" + " limit " + limit + " offset "
				+ offset;

		IPreparedStatement statement = db.prepareStatement(stmt);
		statement.setString(1, getArgument(querystring, matchMode));
		statement.setInt(2, minY);
		statement.setInt(3, maxY);
		statement.setInt(4, minX);
		statement.setInt(5, maxX);

		IResultSet results = statement.executeQuery();
		List<SqPoi> list = listOfPoiFromResults(results);
		results.close();
		return list;
	}

	public static List<SqPoi> getPoisInBboxExtension(IConnection db,
			String querystring, MatchMode matchMode, TIntSet sids, int minY,
			int maxY, int minX, int maxX, int innerMinY, int innerMaxY,
			int innerMinX, int innerMaxX, int limit, int offset)
			throws QueryException
	{
		String questionMarks = buildList(sids.size());

		String stmt = "select p.id, p.x, p.y,"
				+ " p.name, p.simple_name, p.phone, p.website" + " from pois p"
				+ " where p.simple_name like ?"
				+ " and p.y between ? and ? and p.x between ? and ?"
				+ " and p.y not between ? and ? and p.x not between ? and ?"
				+ " and sid in (" + questionMarks + ")" + " limit " + limit
				+ " offset " + offset;

		IPreparedStatement statement = db.prepareStatement(stmt);
		statement.setString(1, getArgument(querystring, matchMode));
		statement.setInt(2, minY);
		statement.setInt(3, maxY);
		statement.setInt(4, minX);
		statement.setInt(5, maxX);
		statement.setInt(6, innerMinY);
		statement.setInt(7, innerMaxY);
		statement.setInt(8, innerMinX);
		statement.setInt(9, innerMaxX);
		addParameters(statement, 10, sids.iterator());

		IResultSet results = statement.executeQuery();
		List<SqPoi> list = listOfPoiFromResults(results);
		results.close();
		return list;
	}

	public static int getNumberOfPois(IConnection db, String querystring,
			MatchMode matchMode) throws QueryException
	{
		String stmt = "select count(p.id) from pois p"
				+ " where p.simple_name like ?";

		IPreparedStatement statement = db.prepareStatement(stmt);
		statement.setString(1, getArgument(querystring, matchMode));
		IResultSet results = statement.executeQuery();
		results.next();
		int count = results.getInt(1);
		results.close();
		return count;
	}

	public static boolean isNumberOfPoisGreaterThan(IConnection db,
			String querystring, MatchMode matchMode, int n)
			throws QueryException
	{
		String stmt = "select count(p.id) > ? from pois p"
				+ " where p.simple_name like ?";

		IPreparedStatement statement = db.prepareStatement(stmt);
		statement.setInt(1, n);
		statement.setString(2, getArgument(querystring, matchMode));
		IResultSet results = statement.executeQuery();
		results.next();
		int bool = results.getInt(1);
		results.close();
		return bool == 1;
	}

	public static int getNumberOfPois(IConnection db, String querystring,
			MatchMode matchMode, TIntSet types) throws QueryException
	{
		String questionMarks = buildList(types.size());

		String stmt = "select count(pois.id)"
				+ " from pois join pois_types on pois.id=pois_types.pois_id"
				+ " where pois_types.types_id in (" + questionMarks + ")"
				+ " and pois.simple_name like ?";

		IPreparedStatement statement = db.prepareStatement(stmt);

		List<String> parameters = new ArrayList<>();
		addParameters(parameters, types);
		parameters.add(getArgument(querystring, matchMode));

		String[] args = parameters.toArray(new String[0]);
		statement.setArguments(args);

		IResultSet results = statement.executeQuery();
		results.next();
		int count = results.getInt(1);
		results.close();
		return count;
	}

	/**
	 * Get the number of roads.
	 * 
	 * @param db
	 *            the database to query
	 * @return the number of roads.
	 * @throws QueryException
	 */
	public static int getNumberOfRoads(IConnection db) throws QueryException
	{
		String stmt = "select count(streets.id) from streets";
		IPreparedStatement statement = db.prepareStatement(stmt);
		IResultSet results = statement.executeQuery();
		results.next();
		int count = results.getInt(1);
		results.close();
		return count;
	}

	/**
	 * Get the number of POIs of a given type.
	 * 
	 * @param db
	 *            the database to query
	 * @param typeId
	 *            the type's id.
	 * @return the number of entries of this kind.
	 * @throws QueryException
	 */
	public static int getNumberOfPois(IConnection db, int typeId)
			throws QueryException
	{
		String stmt = "select count(pois.id)"
				+ " from pois join pois_types on pois.id=pois_types.pois_id"
				+ " where pois_types.types_id = '" + typeId + "'";
		IPreparedStatement statement = db.prepareStatement(stmt);
		IResultSet results = statement.executeQuery();
		results.next();
		int count = results.getInt(1);
		results.close();
		return count;
	}

}

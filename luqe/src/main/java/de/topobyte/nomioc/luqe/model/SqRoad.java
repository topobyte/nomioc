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

package de.topobyte.nomioc.luqe.model;

import java.util.HashSet;
import java.util.Set;

import de.topobyte.luqe.iface.IConnection;
import de.topobyte.luqe.iface.IPreparedStatement;
import de.topobyte.luqe.iface.IResultSet;
import de.topobyte.luqe.iface.QueryException;

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 * 
 */
public class SqRoad extends SqEntity
{

	public void setBoroughs(Set<SqBorough> boroughs)
	{
		this.boroughs = boroughs;
	}

	public void setPostcodes(Set<SqPostcode> postcodes)
	{
		this.postcodes = postcodes;
	}

	private Set<SqBorough> boroughs = null;
	private Set<SqPostcode> postcodes = null;

	/**
	 * Retrieve the set of boroughs for this road.
	 * 
	 * @param db
	 *            a connection to use to query.
	 * @return a set of boroughs.
	 * @throws QueryException
	 */
	@Override
	public Set<SqBorough> getBoroughs(IConnection db) throws QueryException
	{
		if (this.boroughs != null) {
			return this.boroughs;
		}
		this.boroughs = new HashSet<>();
		String stmt = "select boroughs.id, boroughs.level, boroughs.name"
				+ " from streets s"
				+ " join borough_sets_boroughs bs on boroughs=bs.borough_sets_id"
				+ " join boroughs on bs.boroughs_id=boroughs.id" //
				+ " where s.id=" + this.id;
		IPreparedStatement statement = db.prepareStatement(stmt);
		IResultSet results = statement.executeQuery();

		while (results.next()) {
			int bid = results.getInt(1);
			int level = results.getInt(2);
			String bname = results.getString(3);
			this.boroughs.add(new SqBorough(bid, level, bname));
		}

		results.close();
		return this.boroughs;
	}

	/**
	 * Retrieve the set of postcodes for this road.
	 * 
	 * @param db
	 *            a connection to use to query.
	 * @return a set of postcodes.
	 * @throws QueryException
	 */
	@Override
	public Set<SqPostcode> getPostcodes(IConnection db) throws QueryException
	{
		if (this.postcodes != null) {
			return this.postcodes;
		}
		this.postcodes = new HashSet<>();
		String stmt = "select postalcodes.id, postalcodes.code "
				+ "from streets s "
				+ "join postal_code_sets_postalcodes ps on postal_codes=ps.postal_code_sets_id "
				+ "join postalcodes on ps.codes_id=postalcodes.id "
				+ "where s.id=" + this.id;
		IPreparedStatement statement = db.prepareStatement(stmt);
		IResultSet results = statement.executeQuery();

		while (results.next()) {
			int pid = results.getInt(1);
			String pname = results.getString(2);
			this.postcodes.add(new SqPostcode(pid, pname));
		}

		results.close();
		return this.postcodes;
	}

	@Override
	public int hashCode()
	{
		return this.id;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof SqRoad) {
			SqRoad o = (SqRoad) other;
			return o.id == this.id;
		}
		return false;
	}
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import de.topobyte.luqe.iface.IConnection;
import de.topobyte.luqe.iface.QueryException;

public abstract class SqEntity
{
	protected int id;
	private String name;
	private String simpleName;
	private int x, y;

	public SqEntity()
	{
		// empty constructor
	}

	public SqEntity(SqEntity other)
	{
		id = other.id;
		name = other.name;
		simpleName = other.simpleName;
		y = other.y;
		x = other.x;
	}

	/**
	 * @return the id
	 */
	public int getId()
	{
		return this.id;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the simple name
	 */
	public String getSimpleName()
	{
		return simpleName;
	}

	/**
	 * @return the name if available and the simple name otherwise
	 */
	public String getNameSafe()
	{
		if (name == null) {
			return simpleName;
		}
		return name;
	}

	public int getY()
	{
		return y;
	}

	public int getX()
	{
		return x;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setSimpleName(String simpleName)
	{
		this.simpleName = simpleName;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public abstract Set<SqBorough> getBoroughs(IConnection db)
			throws QueryException;

	public abstract Set<SqPostcode> getPostcodes(IConnection db)
			throws QueryException;

	/**
	 * Return a String in format A, B, C where A, B, C are boroughs of this
	 * road.
	 * 
	 * @param database
	 *            a connection to use
	 * @return a pretty String
	 * @throws QueryException
	 */
	public String getBoroughsAsString(IConnection database)
			throws QueryException
	{
		Set<SqBorough> boroughs = getBoroughs(database);
		return getBorouhgsAsString(boroughs);
	}

	public static String getBorouhgsAsString(Set<SqBorough> boroughs)
	{
		StringBuilder strb = new StringBuilder();
		if (boroughs.size() > 0) {
			List<SqBorough> bs = new ArrayList<>();
			for (SqBorough b : boroughs) {
				bs.add(b);
			}
			Collections.sort(bs, new Comparator<SqBorough>() {

				@Override
				public int compare(SqBorough o1, SqBorough o2)
				{
					if (o1.getLevel() != o2.getLevel()) {
						return o1.getLevel() - o2.getLevel();
					}
					return o1.getName().compareTo(o2.getName());
				}
			});
			for (int i = 0; i < bs.size(); i++) {
				SqBorough dbBorough = bs.get(i);
				strb.append(dbBorough.getName());
				if (i < bs.size() - 1) {
					strb.append(", ");
				}
			}
		}
		return strb.toString();
	}

	/**
	 * Return a String in format A, B, C where A, B, C are postal codes of this
	 * road.
	 * 
	 * @param database
	 *            a connection to use
	 * @return a pretty String
	 * @throws QueryException
	 */
	public String getPostcodesAsString(IConnection database)
			throws QueryException
	{
		Set<SqPostcode> postCodes = getPostcodes(database);
		return getPostcodesAsString(postCodes);
	}

	public static String getPostcodesAsString(Set<SqPostcode> postCodes)
	{
		StringBuilder strb = new StringBuilder();
		if (postCodes.size() > 0) {
			List<SqPostcode> bs = new ArrayList<>();
			for (SqPostcode b : postCodes) {
				bs.add(b);
			}
			Collections.sort(bs, new Comparator<SqPostcode>() {

				@Override
				public int compare(SqPostcode o1, SqPostcode o2)
				{
					return o1.getPostcode().compareTo(o2.getPostcode());
				}
			});
			for (int i = 0; i < bs.size(); i++) {
				SqPostcode dbPostcode = bs.get(i);
				strb.append(dbPostcode.getPostcode());
				if (i < bs.size() - 1) {
					strb.append(", ");
				}
			}
		}
		return strb.toString();
	}

	/**
	 * Produce a verbose String for detailed output
	 * 
	 * @param database
	 *            a database
	 * @param coordinates
	 *            whether to show coordinates.
	 * @return a pretty string representation
	 * @throws QueryException
	 */
	public String getLocationQualifier(IConnection database)
			throws QueryException
	{
		StringBuilder strb = new StringBuilder();
		String bs = getBoroughsAsString(database);
		if (bs.length() > 0) {
			strb.append("(");
			strb.append(bs);
			strb.append(")");
		}
		String ps = getPostcodesAsString(database);
		if (ps.length() > 0) {
			strb.append(" (");
			strb.append(ps);
			strb.append(")");
		}
		return strb.toString();
	}

	/**
	 * Produce a verbose String for detailed output
	 * 
	 * @param database
	 *            a database
	 * @param coordinates
	 *            whether to show coordinates.
	 * @return a pretty string representation
	 * @throws QueryException
	 */
	public String toVerboseString(IConnection database, boolean coordinates)
			throws QueryException
	{
		StringBuilder strb = new StringBuilder();
		if (coordinates) {
			strb.append(toString());
		} else {
			strb.append(getNameSafe());
		}
		Set<SqBorough> boroughs = getBoroughs(database);
		if (!boroughs.isEmpty()) {
			strb.append(" (");
			strb.append(getBorouhgsAsString(boroughs));
			strb.append(")");
		}
		Set<SqPostcode> postCodes = getPostcodes(database);
		if (!postCodes.isEmpty()) {
			strb.append(" (");
			strb.append(getPostcodesAsString(postCodes));
			strb.append(")");
		}
		return strb.toString();
	}
}

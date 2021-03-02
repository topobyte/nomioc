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

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 * 
 */
public class SqPostcode
{

	private int id;
	private String code;

	/**
	 * Create a new instance of SqPostcode.
	 * 
	 * @param id
	 *            the database id.
	 * @param code
	 *            the postcode.
	 */
	public SqPostcode(int id, String code)
	{
		this.id = id;
		this.code = code;
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
	public String getPostcode()
	{
		return this.code;
	}

	@Override
	public int hashCode()
	{
		return this.id;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof SqPostcode) {
			SqPostcode o = (SqPostcode) other;
			return o.id == this.id;
		}
		return false;
	}
}

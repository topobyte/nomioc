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

package de.topobyte.nomioc.android.v2.config;

import java.util.List;

public class PoiClass
{

	private String identifier;
	private NameConstraint nameConstraint;
	private List<String> nameKeys;
	private boolean forceSingle;

	public PoiClass(String identifier, NameConstraint nameConstraint,
			List<String> nameKeys, boolean forceSingle)
	{
		this.identifier = identifier;
		this.nameConstraint = nameConstraint;
		this.nameKeys = nameKeys;
		this.forceSingle = forceSingle;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public boolean hasName()
	{
		return nameConstraint != NameConstraint.NO_NAME;
	}

	public boolean requiresName()
	{
		return nameConstraint == NameConstraint.MUST_HAVE;
	}

	public boolean hasSpecialNameKeys()
	{
		return nameKeys != null;
	}

	public List<String> getNameKeys()
	{
		return nameKeys;
	}

	public boolean isForceSingle()
	{
		return forceSingle;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof PoiClass)) {
			return false;
		}
		PoiClass other = (PoiClass) obj;
		return other.identifier.equals(identifier);
	}

	@Override
	public int hashCode()
	{
		return identifier.hashCode();
	}

}

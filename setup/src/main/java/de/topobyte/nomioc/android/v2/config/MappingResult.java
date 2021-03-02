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

import java.util.Set;

import de.topobyte.nomioc.android.v2.model.hibernate.PoiType;

public class MappingResult
{
	private final String name;
	private final Set<PoiType> types;

	public MappingResult(String name, Set<PoiType> types)
	{
		this.name = name;
		this.types = types;
	}

	public String getName()
	{
		return name;
	}

	public Set<PoiType> getTypes()
	{
		return types;
	}

}

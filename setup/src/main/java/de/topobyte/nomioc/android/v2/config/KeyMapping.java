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

import java.util.HashMap;
import java.util.Map;

public class KeyMapping
{

	private Map<String, PoiClass> valueToClass = new HashMap<>();
	private Map<String, PoiClass> nameToClass = new HashMap<>();

	public void add(String value, PoiClass pc)
	{
		PoiClass p = storeByName(pc);
		valueToClass.put(value, p);
	}

	private PoiClass storeByName(PoiClass pc)
	{
		PoiClass p = nameToClass.get(pc.getIdentifier());
		if (p == null) {
			p = pc;
			nameToClass.put(p.getIdentifier(), p);
		}
		return p;
	}

	public PoiClass get(String value)
	{
		return valueToClass.get(value);
	}

}

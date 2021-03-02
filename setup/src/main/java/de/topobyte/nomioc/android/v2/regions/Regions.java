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

package de.topobyte.nomioc.android.v2.regions;

import de.topobyte.jts.indexing.GeometryTesselationMap;
import de.topobyte.nomioc.android.v2.model.hibernate.Borough;
import de.topobyte.nomioc.android.v2.model.hibernate.PostalCode;

public class Regions
{

	private GeometryTesselationMap<Borough> boroughIndex = new GeometryTesselationMap<>(
			true);
	private GeometryTesselationMap<PostalCode> postalCodeIndex = new GeometryTesselationMap<>(
			true);

	public GeometryTesselationMap<Borough> getBoroughIndex()
	{
		return boroughIndex;
	}

	public GeometryTesselationMap<PostalCode> getPostalCodeIndex()
	{
		return postalCodeIndex;
	}

}

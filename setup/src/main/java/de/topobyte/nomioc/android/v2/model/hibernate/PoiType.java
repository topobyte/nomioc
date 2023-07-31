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

package de.topobyte.nomioc.android.v2.model.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 *
 */
@Entity
@Table(name = "poitypes", indexes = {
		@Index(name = "names", columnList = "name") })
public class PoiType
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "name")
	private String name;

	@Transient
	private boolean useName;

	public PoiType()
	{
		// for hibernate
	}

	public PoiType(String name, boolean useName)
	{
		this.name = name;
		this.useName = useName;
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public boolean useName()
	{
		return useName;
	}
}

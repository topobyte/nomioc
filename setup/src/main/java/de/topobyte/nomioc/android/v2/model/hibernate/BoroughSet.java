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

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "borough_sets")
public class BoroughSet
{
	@Id
	@GenericGenerator(name = "generator", strategy = "increment")
	@GeneratedValue(generator = "generator")
	private int id;

	@ManyToMany
	@JoinTable(joinColumns = @JoinColumn(name = "borough_sets_id"))
	private Set<Borough> boroughs;

	public BoroughSet()
	{
		// hibernate constructor
	}

	public BoroughSet(Set<Borough> boroughs)
	{
		this.boroughs = boroughs;
	}

	public int getId()
	{
		return id;
	}

	public Set<Borough> getBoroughs()
	{
		return boroughs;
	}

	public void setBoroughs(Set<Borough> boroughs)
	{
		this.boroughs = boroughs;
	}

}

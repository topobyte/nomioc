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

import de.topobyte.sqlitespatial.spatialindex.builder.Indexable;
import de.topobyte.sqlitespatial.spatialindex.builder.Node;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 *
 */
@Entity
@Table(name = "streets", indexes = {
		@Index(name = "street_simple_name", columnList = "simple_name"),
		@Index(name = "sidx_streets", columnList = "sid") })
public class Street implements Indexable
{
	@Id
	@GeneratedValue
	private int id;

	@Column(name = "name")
	private String name;

	@Column(name = "simple_name")
	private String simpleName;

	@ManyToOne
	@JoinColumn(name = "boroughs")
	private BoroughSet boroughSet;

	@ManyToOne
	@JoinColumn(name = "postal_codes")
	private PostalCodeSet postalCodeSet;

	@Column(name = "x")
	private int x;

	@Column(name = "y")
	private int y;

	@Column(name = "sid")
	private int sid;

	@Transient
	Node<? extends Indexable> node = null;

	public Street()
	{
		// hibernate constructor
	}

	public Street(String name, String simpleName, int x, int y)
	{
		this.name = name;
		this.simpleName = simpleName;
		this.x = x;
		this.y = y;
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getSimpleName()
	{
		return simpleName;
	}

	public Set<Borough> getBoroughs()
	{
		return boroughSet.getBoroughs();
	}

	public Set<PostalCode> getPostalCodes()
	{
		return postalCodeSet.getCodes();
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setSimpleName(String simpleName)
	{
		this.simpleName = simpleName;
	}

	public BoroughSet getBoroughSet()
	{
		return boroughSet;
	}

	public void setBoroughSet(BoroughSet boroughSet)
	{
		this.boroughSet = boroughSet;
	}

	public PostalCodeSet getPostalCodeSet()
	{
		return postalCodeSet;
	}

	public void setPostalCodeSet(PostalCodeSet postalCodeSet)
	{
		this.postalCodeSet = postalCodeSet;
	}

	@Override
	public int getX()
	{
		return x;
	}

	@Override
	public int getY()
	{
		return y;
	}

	public void setX(int lon)
	{
		this.x = lon;
	}

	public void setY(int lat)
	{
		this.y = lat;
	}

	public int getSid()
	{
		return sid;
	}

	@Override
	public void setSid(int sid)
	{
		this.sid = sid;
	}

	@Override
	public void setNode(Node<? extends Indexable> node)
	{
		this.node = node;
	}

	@Override
	public Node<? extends Indexable> getNode()
	{
		return node;
	}

}

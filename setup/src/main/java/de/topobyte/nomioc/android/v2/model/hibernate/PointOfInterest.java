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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 *
 */
@Entity
@Table(name = "pois", indexes = {
		@Index(name = "poi_simple_name", columnList = "simple_name"),
		@Index(name = "sidx_pois", columnList = "sid") })
public class PointOfInterest
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@ManyToMany
	@JoinTable(name = "pois_types", joinColumns = @JoinColumn(name = "pois_id"))
	// TODO: test if this should be enabled:
	// @JoinTable(name = "pois_types", indexes = { @Index(name = "poi_type",
	// columnList = "types_id") })
	private Set<PoiType> types;

	@Column(name = "name")
	private String name;

	@Column(name = "simple_name", columnDefinition = "varchar collate nocase")
	private String simpleName;

	@Column(name = "phone")
	private String phone;

	@Column(name = "website")
	private String website;

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

	public PointOfInterest()
	{
		// for hibernate
	}

	public PointOfInterest(String name, String simpleName, String phone,
			String website, int x, int y)
	{
		this.name = name;
		this.simpleName = simpleName;
		this.phone = phone;
		this.website = website;
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

	public String getPhone()
	{
		return phone;
	}

	public String getWebsite()
	{
		return website;
	}

	public Set<PoiType> getTypes()
	{
		return types;
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

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public void setWebsite(String website)
	{
		this.website = website;
	}

	public void setTypes(Set<PoiType> types)
	{
		this.types = types;
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

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public int getSid()
	{
		return sid;
	}

	public void setSid(int sid)
	{
		this.sid = sid;
	}

}

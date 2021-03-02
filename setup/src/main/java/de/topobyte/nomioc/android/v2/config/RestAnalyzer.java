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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import de.topobyte.guava.util.MultisetUtil;
import de.topobyte.guava.util.Order;

public class RestAnalyzer
{
	private Set<String> ignorableKeys = new HashSet<>();
	private Set<String> ignorableKeyPrefixes = new HashSet<>();
	private Map<String, Set<String>> ignorableTags = new HashMap<>();

	public RestAnalyzer()
	{
		for (String key : new String[] { "highway", "maxspeed", "oneway", "lit",
				"surface", "frequency", "electrified", "gauge", "lanes",
				"cycleway", "voltage", "layer", "sidewalk", "smoothness",
				"postal_code", "int_ref", "foot", "note", "source", "ref",
				"old_ref", "access", "tracktype", "power", "footway", "width",
				"fee", "junction", "wikipedia", "admin_level", "barrier",
				"wheelchair", "boundary", "tracks", "lcn", "start_date",
				"tunnel", "network", "workrules", "detail", "route",
				"traffic_sign", "wires", "psv", "hgv", "rcn", "pzb", "level",
				"created_by", "cutting", "scenic", "cables", "maxweight",
				"embankment", "covered", "interval", "maxheight", "headway",
				"radio", "is_in", "motor_vehicle", "motorcar",
				"operating_procedure", "old_name", "tilting_technology",
				"loc_ref", "combined_transport_codification" }) {
			ignorableKeys.add(key);
		}
		for (String key : new String[] { "railway:", "addr:", "cycleway:",
				"source:", "note:", "power:", "seamark:", "is_in:", "roof:",
				"parking:", "lanes:", "zone:", "wikipedia:", "ref:",
				"maxspeed:", "turn:", "oneway:", "eddy_" }) {
			ignorableKeyPrefixes.add(key);
		}
		addIT("railway", "rail");
		addIT("railway", "abandoned");
		addIT("usage", "main");
		addIT("usage", "branch");
		addIT("bicycle", "designated");
		addIT("landuse", "industrial");
		addIT("type", "route");
		addIT("type", "boundary");
		addIT("type", "multipolygon");
		addIT("type", "associatedStreet");
		addIT("building", "industrial");
		addIT("service", "driveway");
		for (String key : new String[] { "building", "bridge", "bicycle",
				"foot", "wheelchair", "highspeed", "tunnel", "area", "bus",
				"segregated", "embankment", "train", "boat", "motorroad",
				"light_rail", "horse", "noexit", "motorcycle", "motorcar",
				"motor_vehicle", "mofa", "winter_service" }) {
			addIT(key, "yes");
			addIT(key, "no");
		}
	}

	public void addToOtherHistogram(Map<String, String> tags)
	{
		tags: for (Map.Entry<String, String> tag : tags.entrySet()) {
			String key = tag.getKey();
			String value = tag.getValue();
			if (key.equals("name")) {
				continue;
			}
			if (ignorableKeys.contains(key)) {
				continue;
			}
			for (String prefix : ignorableKeyPrefixes) {
				if (key.startsWith(prefix)) {
					continue tags;
				}
			}
			if (canTagBeIgnored(key, value)) {
				continue;
			}
			otherTags.add(key + "=" + value);
		}
	}

	public void analyzeOtherHistogram()
	{
		List<Multiset.Entry<String>> list = MultisetUtil.entries(otherTags,
				Order.ASCENDING, Order.ASCENDING);
		for (Multiset.Entry<String> entry : list) {
			System.out.println(entry.getElement() + ": " + entry.getCount());
		}
	}

	private void addIT(String key, String value)
	{
		Set<String> values = ignorableTags.get(key);
		if (values == null) {
			values = new HashSet<>();
			ignorableTags.put(key, values);
		}
		values.add(value);
	}

	public boolean canTagBeIgnored(String key, String value)
	{
		Set<String> values = ignorableTags.get(key);
		if (values == null) {
			return false;
		}
		return values.contains(value);
	}

	private Multiset<String> otherTags = HashMultiset.create();
}

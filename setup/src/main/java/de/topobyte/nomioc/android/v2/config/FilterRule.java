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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterRule
{

	final static Logger logger = LoggerFactory.getLogger(FilterRule.class);

	private String key;
	private String value;

	private List<FilterRule> filters = new ArrayList<>();
	private Map<String, KeyMapping> mappings = new HashMap<>();

	public FilterRule(String key, String value)
	{
		this.key = key;
		this.value = value;
	}

	public void add(FilterRule filter)
	{
		filters.add(filter);
	}

	private KeyMapping getMapping(String key)
	{
		KeyMapping mapping = mappings.get(key);
		if (mapping == null) {
			mapping = new KeyMapping();
			mappings.put(key, mapping);
		}
		return mapping;
	}

	public void add(String key, String value, PoiClass pc)
	{
		KeyMapping mapping = getMapping(key);
		mapping.add(value, pc);
	}

	public void determine(Map<String, String> tags, Set<PoiClass> types)
	{
		// If no key has been set in the filter, go on anyway
		if (key != null) {
			// Check for the available value v
			String v = tags.get(key);
			// Ignore if there is no value
			if (v == null) {
				return;
			}
			// If the filter contains a value, check if it is present in the
			// tag's value list
			if (value != null) {
				Set<String> values = values(v);
				if (!values.contains(value)) {
					return;
				}
			}
		}
		// Now apply mappings and recurse to other filters
		boolean multiple = false;
		for (Entry<String, String> tag : tags.entrySet()) {
			KeyMapping mapping = mappings.get(tag.getKey());
			if (mapping == null) {
				continue;
			}
			Set<String> values = values(tag.getValue());
			if (values.size() > 1) {
				multiple = true;
			}
			for (String value : values) {
				PoiClass pc = mapping.get(value);
				if (pc == null) {
					continue;
				}
				if (pc.requiresName() && hasName(pc, tags)
						|| !pc.requiresName()) {
					types.add(pc);
				}
			}
		}
		if (multiple) {
			logger.info("Multiple values: " + tags);
		}
		for (FilterRule filter : filters) {
			filter.determine(tags, types);
		}
	}

	private Set<String> values(String values)
	{
		Set<String> result = new HashSet<>();
		String[] splitted = values.split(";");
		for (String value : splitted) {
			result.add(value.trim());
		}
		return result;
	}

	private boolean hasName(PoiClass pc, Map<String, String> tags)
	{
		List<String> nameKeys = pc.getNameKeys();
		if (nameKeys == null) {
			nameKeys = new ArrayList<>();
			nameKeys.add("name");
		}
		for (String key : nameKeys) {
			if (tags.containsKey(key)) {
				return true;
			}
		}
		return false;
	}
}

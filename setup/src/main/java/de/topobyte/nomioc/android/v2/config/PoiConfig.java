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

public class PoiConfig
{

	public static String FILENAME = "pois.xml";

	private FilterRule main = new FilterRule(null, null);
	private Map<String, IgnoreRules> ignores = new HashMap<>();

	private List<PoiClass> classes = new ArrayList<>();
	private Set<PoiClass> classesSet = new HashSet<>();

	private List<PoiClass> classesNoName = new ArrayList<>();
	private Set<PoiClass> classesSetNoName = new HashSet<>();

	FilterRule getMainFilter()
	{
		return main;
	}

	public List<PoiClass> getClasses()
	{
		return classes;
	}

	public List<PoiClass> getClassesNoName()
	{
		return classesNoName;
	}

	public void add(FilterRule filter, String key, String value, String name,
			NameConstraint nc, List<String> names, boolean forceSingle)
	{
		PoiClass pc = new PoiClass(name, nc, names, forceSingle);
		add(filter, key, value, pc);
	}

	private void add(FilterRule filter, String key, String value, PoiClass pc)
	{
		if (pc.hasName()) {
			addType(pc);
		} else {
			addTypeNoName(pc);
		}
		filter.add(key, value, pc);
	}

	private void addType(PoiClass pc)
	{
		if (classesSet.contains(pc)) {
			return;
		}
		classes.add(pc);
		classesSet.add(pc);
	}

	private void addTypeNoName(PoiClass pc)
	{
		if (classesSetNoName.contains(pc)) {
			return;
		}
		classesNoName.add(pc);
		classesSetNoName.add(pc);
	}

	public void addIgnore(String key, String value, boolean nocase)
	{
		IgnoreRules rules = getIgnore(key);
		rules.add(value, nocase);
	}

	public void addIgnore(String key, String pattern)
	{
		IgnoreRules rules = getIgnore(key);
		rules.add(pattern);
	}

	private IgnoreRules getIgnore(String key)
	{
		IgnoreRules rules = ignores.get(key);
		if (rules == null) {
			rules = new IgnoreRules();
			ignores.put(key, rules);
		}
		return rules;
	}

	public Set<PoiClass> determine(Map<String, String> tags)
	{
		Set<PoiClass> types = new HashSet<>();
		main.determine(tags, types);
		return types;
	}

	public boolean shouldIgnore(Map<String, String> tags)
	{
		for (Entry<String, String> tag : tags.entrySet()) {
			IgnoreRules rules = ignores.get(tag.getKey());
			if (rules == null) {
				continue;
			}
			if (rules.matches(tag.getValue())) {
				return true;
			}
		}
		return true;
	}

	public void addFilter(FilterRule filter, FilterRule subFilter)
	{
		filter.add(filter);
	}

}

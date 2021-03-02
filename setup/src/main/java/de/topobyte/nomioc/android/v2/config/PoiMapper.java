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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.nomioc.android.v2.model.hibernate.PoiType;

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 *
 */
public class PoiMapper
{
	static final Logger logger = LoggerFactory.getLogger(PoiMapper.class);

	private PoiType otherShops;
	private PoiType otherPois;

	private PoiConfig poiConfig;
	private Map<String, PoiType> namePoiMap;

	private List<String> extraTypes = new ArrayList<>();

	private RestAnalyzer analyzer;

	public PoiMapper(PoiConfig poiConfig)
	{
		this.poiConfig = poiConfig;

		namePoiMap = new HashMap<>();
		for (PoiClass type : poiConfig.getClasses()) {
			PoiType poiType = new PoiType(type.getIdentifier(), type.hasName());
			namePoiMap.put(type.getIdentifier(), poiType);
		}
		for (PoiClass type : poiConfig.getClassesNoName()) {
			PoiType poiType = new PoiType(type.getIdentifier(), type.hasName());
			namePoiMap.put(type.getIdentifier(), poiType);
		}

		otherShops = add("other-shops", true);
		otherPois = add("other", true);

		analyzer = new RestAnalyzer();
	}

	private PoiType add(String name, boolean useName)
	{
		logger.info("Adding extra type: " + name);
		PoiType poiType = namePoiMap.get(name);
		if (poiType != null) {
			logger.info("No need to add it, already there");
			return poiType;
		}
		logger.info("Yep, really have to add it");
		poiType = new PoiType(name, useName);
		namePoiMap.put(name, poiType);
		extraTypes.add(name);
		return poiType;
	}

	public PoiType getPoiType(String type)
	{
		return namePoiMap.get(type);
	}

	public List<String> getExtraTypes()
	{
		return extraTypes;
	}

	public List<MappingResult> determineTypes(Map<String, String> tags)
	{
		Set<PoiClass> classes = poiConfig.determine(tags);

		List<Set<PoiClass>> splitted = splitSingle(classes);

		List<MappingResult> results = new ArrayList<>();

		for (Set<PoiClass> classGroup : splitted) {
			MappingResult mr = map(classGroup, tags);
			if (mr == null) {
				continue;
			}
			results.add(mr);
		}

		if (classes.isEmpty()) {
			MappingResult special = mapSpecial(tags);
			if (special != null) {
				results.add(special);
			}
		}

		return results;
	}

	private MappingResult map(Set<PoiClass> classes, Map<String, String> tags)
	{
		Set<PoiType> types = new HashSet<>();

		for (PoiClass pc : classes) {
			types.add(namePoiMap.get(pc.getIdentifier()));
		}

		List<String> nameKeys = null;
		boolean bogus = false;
		for (PoiClass pc : classes) {
			List<String> keys = pc.getNameKeys();
			if (keys == null) {
				continue;
			}
			if (nameKeys != null) {
				bogus = true;
				continue;
			}
			nameKeys = keys;
		}
		if (bogus) {
			logger.warn("Multiply defined name keys");
		}

		if (nameKeys == null) {
			nameKeys = new ArrayList<>();
			nameKeys.add("name");
		}

		String name = getName(tags, nameKeys);
		return types.size() == 0 ? null : new MappingResult(name, types);
	}

	private MappingResult mapSpecial(Map<String, String> tags)
	{
		String name = tags.get("name");
		if (name == null) {
			return null;
		}

		Set<PoiType> types = new HashSet<>();
		if (tags.containsKey("shop")) {
			types.add(otherShops);
		} else if (tags.containsKey("leisure")) {
			types.add(otherPois);
		} else if (tags.containsKey("building")) {
			String building = tags.get("building");
			if (building.equals("public")) {
				types.add(otherPois);
			}
			types.add(otherPois);
		} else {
			analyzer.addToOtherHistogram(tags);
			if (!poiConfig.shouldIgnore(tags)) {
				types.add(otherPois);
			}
		}

		if (types.isEmpty()) {
			return null;
		}

		return new MappingResult(name, types);
	}

	private List<Set<PoiClass>> splitSingle(Set<PoiClass> classes)
	{
		List<Set<PoiClass>> list = new ArrayList<>();
		Iterator<PoiClass> iterator = classes.iterator();
		while (iterator.hasNext()) {
			PoiClass pc = iterator.next();
			if (pc.isForceSingle()) {
				iterator.remove();
				Set<PoiClass> single = new HashSet<>();
				single.add(pc);
				list.add(single);
			}
		}
		if (!classes.isEmpty()) {
			list.add(classes);
		}
		return list;
	}

	private String getName(Map<String, String> tags, List<String> nameKeys)
	{
		for (String key : nameKeys) {
			String name = tags.get(key);
			if (name == null) {
				continue;
			}
			return name;
		}
		return null;
	}

	public RestAnalyzer getRestAnalyzer()
	{
		return analyzer;
	}

}

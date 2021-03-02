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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.xmlbeans.XmlException;

import de.topobyte.nomioc.android.v2.config.parser.Class;
import de.topobyte.nomioc.android.v2.config.parser.Classes;
import de.topobyte.nomioc.android.v2.config.parser.ConfigDocument;
import de.topobyte.nomioc.android.v2.config.parser.ConfigDocument.Config;
import de.topobyte.nomioc.android.v2.config.parser.Filter;
import de.topobyte.nomioc.android.v2.config.parser.Ignore;
import de.topobyte.nomioc.android.v2.config.parser.Map;
import de.topobyte.nomioc.android.v2.config.parser.Util;

public class ConfigBuilder
{

	public static PoiConfig build(Path file) throws XmlException, IOException
	{
		try (InputStream input = Files.newInputStream(file)) {
			return build(input);
		}
	}

	public static PoiConfig build(InputStream input)
			throws XmlException, IOException
	{
		ConfigDocument document = ConfigDocument.Factory.parse(input);

		Config config = document.getConfig();
		Map[] maps = config.getMapArray();
		Filter[] filters = config.getFilterArray();
		Ignore[] ignores = config.getIgnoreArray();

		PoiConfig pc = new PoiConfig();

		FilterRule main = pc.getMainFilter();

		build(pc, main, maps);

		build(pc, main, filters);

		for (Ignore ignore : ignores) {
			String key = ignore.getKey();
			boolean nocase = ignore.isSetNocase();
			String value = ignore.getValue();
			String pattern = ignore.getPattern();
			if (value != null) {
				pc.addIgnore(key, value, nocase);
			} else if (pattern != null) {
				pc.addIgnore(key, pattern);
			}
		}

		return pc;
	}

	private static void build(PoiConfig pc, FilterRule parentFilterRule,
			Filter[] filters)
	{
		for (Filter filter : filters) {
			FilterRule filterRule = new FilterRule(filter.getKey(),
					filter.getValue());
			parentFilterRule.add(filterRule);

			Map[] maps = filter.getMapArray();
			build(pc, filterRule, maps);

			Filter[] subFilters = filter.getFilterArray();
			build(pc, filterRule, subFilters);
		}
	}

	private static void build(PoiConfig pc, FilterRule filter, Map[] maps)
	{
		for (Map map : maps) {
			String key = map.getKey();
			Class[] classes = map.getClass1Array();
			for (Class c : classes) {
				String value = c.getValue();
				String category = c.getCat();

				String namesArg = c.getNames();
				List<String> names = null;
				if (namesArg != null) {
					String[] split = namesArg.split(",");
					names = Arrays.asList(split);
				}

				NameConstraint nc = Util.get(c.getName());
				boolean forceSingle = c.isSetForcesingle();

				if (category == null) {
					category = value;
				}
				pc.add(filter, key, value, category, nc, names, forceSingle);
			}
			Classes[] classeses = map.getClassesArray();
			for (Classes c : classeses) {
				String values = c.getValues();
				String category = c.getCat();
				NameConstraint nc = Util.get(c.getName());
				boolean forceSingle = c.isSetForcesingle();
				for (String value : values.split(",")) {
					String cat = category != null ? category : value;
					pc.add(filter, key, value, cat, nc, null, forceSingle);
				}
			}
		}
	}
}

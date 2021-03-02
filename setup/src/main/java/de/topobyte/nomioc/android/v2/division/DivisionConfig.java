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

package de.topobyte.nomioc.android.v2.division;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DivisionConfig
{

	final static Logger logger = LoggerFactory.getLogger(DivisionConfig.class);

	public static DivisionConfig readConfiguration(String pathPoiConfig)
			throws IOException
	{
		DivisionConfig config = new DivisionConfig();

		BufferedReader reader = new BufferedReader(
				new FileReader(pathPoiConfig));
		String fline;
		while ((fline = reader.readLine()) != null) {
			String value = fline.trim();
			if (value.length() == 0) {
				continue;
			}
			config.placeTags.add(value);
		}

		reader.close();

		return config;
	}

	private Set<String> placeTags = new HashSet<>();

	private DivisionConfig()
	{
		// private constructor
	}

	public boolean usePlace(String place)
	{
		return placeTags.contains(place);
	}

}

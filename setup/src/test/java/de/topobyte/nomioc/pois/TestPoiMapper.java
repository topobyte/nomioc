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

package de.topobyte.nomioc.pois;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.junit.Test;

import com.google.common.base.Splitter;

import de.topobyte.melon.resources.Resources;
import de.topobyte.nomioc.android.v2.config.ConfigBuilder;
import de.topobyte.nomioc.android.v2.config.MappingResult;
import de.topobyte.nomioc.android.v2.config.PoiConfig;
import de.topobyte.nomioc.android.v2.config.PoiMapper;
import de.topobyte.nomioc.android.v2.model.hibernate.PoiType;

public class TestPoiMapper
{

	@Test
	public void test() throws XmlException, IOException
	{
		PoiConfig config = ConfigBuilder.build(Resources.stream("pois.xml"));
		PoiMapper mapper = new PoiMapper(config);

		Map<String, String> tags = new HashMap<>();
		String def = "wheelchair=limited, internet_access=wlan, level=-1, amenity=coworking_space, addr:country=DE, internet_access:fee=no, addr:postcode=10179, layer=-1, operator=Deutsche Bahn AG, addr:city=Berlin, addr:housenumber=6-9, name=DB mindbox, addr:street=Holzmarktstraße, contact:website=http://www.mindboxberlin.com";
		for (String tag : Splitter.on(",").trimResults().split(def)) {
			List<String> parts = Splitter.on("=").splitToList(tag);
			tags.put(parts.get(0), parts.get(1));
		}
		System.out.println(tags);

		List<MappingResult> results = mapper.determineTypes(tags);
		for (MappingResult result : results) {
			for (PoiType type : result.getTypes()) {
				System.out.println(type.getName());
			}
		}
	}

}

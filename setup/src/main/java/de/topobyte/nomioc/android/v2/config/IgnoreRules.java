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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class IgnoreRules
{

	private Set<String> nocases = new HashSet<>();
	private Set<String> cases = new HashSet<>();
	private Set<Pattern> patterns = new HashSet<>();

	public void add(String value, boolean nocase)
	{
		if (nocase) {
			nocases.add(value.toLowerCase());
		} else {
			cases.add(value);
		}
	}

	public void add(String pattern)
	{
		patterns.add(Pattern.compile(pattern));
	}

	public boolean matches(String value)
	{
		if (cases.contains(value)) {
			return true;
		}
		if (nocases.contains(value.toLowerCase())) {
			return true;
		}
		for (Pattern pattern : patterns) {
			if (pattern.matcher(value).matches()) {
				return true;
			}
		}
		return false;
	}

}

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

package de.topobyte.nomioc.android.v2.config.parser;

import de.topobyte.nomioc.android.v2.config.NameConstraint;
import de.topobyte.nomioc.android.v2.config.parser.NameRestriction.Enum;

public class Util
{

	public static NameConstraint get(Enum name)
	{
		if (name == null) {
			return NameConstraint.MAY_HAVE;
		}
		switch (name.intValue()) {
		default:
		case Enum.INT_MAYBE:
			return NameConstraint.MAY_HAVE;
		case Enum.INT_NONE:
			return NameConstraint.NO_NAME;
		case Enum.INT_REQUIRED:
			return NameConstraint.MUST_HAVE;
		}
	}
}

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

package de.topobyte.nomioc.elevation;

import java.text.NumberFormat;
import java.util.Locale;

import de.topobyte.nomioc.elevation.model.DoubleValue;
import de.topobyte.nomioc.elevation.model.IntegerValue;
import de.topobyte.nomioc.elevation.model.NumberValue;

public class ValueFormatter
{

	private NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

	public ValueFormatter(int maxFractionDigits)
	{
		format.setGroupingUsed(false);
		format.setMaximumFractionDigits(maxFractionDigits);
	}

	public String format(NumberValue value)
	{
		if (value instanceof IntegerValue) {
			IntegerValue iv = (IntegerValue) value;
			return Integer.toString(iv.getIntegerValue());
		}
		DoubleValue v = (DoubleValue) value;
		double d = v.getValue();
		if ((d == Math.floor(d))) {
			int i = (int) Math.floor(d);
			return Integer.toString(i);
		}

		return format.format(d);
	}

}

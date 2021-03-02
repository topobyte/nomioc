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

package de.topobyte.nomioc.elevation.model;

public class Value
{

	private NumberValue value;
	private LengthUnit unit;

	public Value(NumberValue value, LengthUnit unit)
	{
		this.value = value;
		this.unit = unit;
	}

	public NumberValue getValue()
	{
		return value;
	}

	public LengthUnit getUnit()
	{
		return unit;
	}

	public Value getValue(LengthUnit targetUnit, boolean preferIntegral)
	{
		if (targetUnit == unit) {
			return this;
		}
		if (unit == LengthUnit.FEET && targetUnit == LengthUnit.METERS) {
			double v = value.getValue();
			double meters = v * 0.3048;
			if (preferIntegral) {
				int i = (int) Math.round(meters);
				return new Value(new IntegerValue(i), LengthUnit.METERS);
			}
			return new Value(new DoubleValue(meters), LengthUnit.METERS);
		} else if (unit == LengthUnit.METERS && targetUnit == LengthUnit.FEET) {
			double v = value.getValue();
			double feet = v / 0.3048;
			if (preferIntegral) {
				int i = (int) Math.round(feet);
				return new Value(new IntegerValue(i), LengthUnit.FEET);
			}
			return new Value(new DoubleValue(feet), LengthUnit.FEET);
		}
		return null;
	}

}

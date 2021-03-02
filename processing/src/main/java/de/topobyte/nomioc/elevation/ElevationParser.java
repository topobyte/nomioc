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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.adt.multicollections.HashMultiSet;
import de.topobyte.adt.multicollections.MultiSet;
import de.topobyte.nomioc.elevation.model.DoubleValue;
import de.topobyte.nomioc.elevation.model.IntegerValue;
import de.topobyte.nomioc.elevation.model.LengthUnit;
import de.topobyte.nomioc.elevation.model.NumberValue;
import de.topobyte.nomioc.elevation.model.Value;

public class ElevationParser
{

	final static Logger logger = LoggerFactory.getLogger(ElevationParser.class);

	private List<NamedPattern> patterns = new ArrayList<>();

	private MultiSet<String> counter = new HashMultiSet<>();
	private int unmatched = 0;
	private int parseErrors = 0;

	public ElevationParser()
	{
		String n = "([1-9]\\d*)";
		String d = "(\\d*)";

		String comma = n + "," + d;
		String dot = n + "\\." + d;
		String dotcomma = n + "\\." + d + "," + d;

		List<String> meterEndings = new ArrayList<>();
		for (String m : new String[] { "m", "M", "м", "mt", "meter", "meters",
				"metres", "metros" }) {
			meterEndings.add(m);
			meterEndings.add("\\+" + m);
			meterEndings.add(m + "\\.");
		}

		List<String> feetEndings = new ArrayList<>();
		for (String ft : new String[] { "ft", "feet" }) {
			feetEndings.add(ft);
			feetEndings.add("\\+" + ft);
			feetEndings.add(ft + "\\.");
		}

		NumberExtractor nePlain = new NumberExtractor() {

			@Override
			public NumberValue getNumberResult(Matcher matcher)
			{
				return new IntegerValue(Integer.parseInt(matcher.group(1)));
			}
		};
		NumberExtractor ne1Sep = new NumberExtractor() {

			@Override
			public NumberValue getNumberResult(Matcher matcher)
			{
				return fraction(matcher.group(1), matcher.group(2));
			}
		};
		NumberExtractor ne2Sep = new NumberExtractor() {

			@Override
			public NumberValue getNumberResult(Matcher matcher)
			{
				return fraction(matcher.group(1) + matcher.group(2),
						matcher.group(3));
			}
		};

		List<PrefixClass> prefixes = new ArrayList<>();
		prefixes.add(new PrefixClass("plain", n, nePlain, LengthUnit.METERS));
		prefixes.add(
				new PrefixClass("comma", comma, ne1Sep, LengthUnit.METERS));
		prefixes.add(new PrefixClass("dot", dot, ne1Sep, LengthUnit.METERS));
		prefixes.add(new PrefixClass("dotcomma", dotcomma, ne2Sep,
				LengthUnit.METERS));

		for (PrefixClass pc : prefixes) {
			patterns.add(new NamedPattern(pc.name, Pattern.compile(pc.prefix),
					pc.extractor, pc.unit));
		}
		for (PrefixClass pc : prefixes) {
			add(pc.name, pc.prefix, meterEndings, pc.extractor,
					LengthUnit.METERS);
		}
		for (PrefixClass pc : prefixes) {
			add(pc.name, pc.prefix, feetEndings, pc.extractor, LengthUnit.FEET);
		}
	}

	protected NumberValue fraction(String vi, String vf)
	{
		int integral = Integer.parseInt(vi);
		double f = 0;
		if (vf.length() > 0) {
			int fraction = Integer.parseInt(max(vf, 6));
			if (fraction != 0) {
				f = fraction / (Math.pow(10, Math.ceil(Math.log10(fraction))));
			}
		}
		return new DoubleValue(integral + f);
	}

	protected String max(String string, int len)
	{
		if (string.length() <= len) {
			return string;
		}
		return string.substring(0, len);
	}

	private void add(String prefixName, String prefix, List<String> endings,
			NumberExtractor extractor, LengthUnit unit)
	{
		for (String ending : endings) {
			patterns.add(new NamedPattern(prefixName + " + '" + ending + "'",
					Pattern.compile(prefix + ending), extractor, unit));
		}
	}

	public Value parse(String elevation)
	{
		elevation = elevation.trim();
		elevation = elevation.replaceAll(" ", "");

		for (NamedPattern np : patterns) {
			Matcher matcher = np.pattern.matcher(elevation);
			if (matcher.matches()) {
				counter.add(np.name);
				try {
					return np.convert(matcher);
				} catch (NumberFormatException e) {
					parseErrors++;
					System.out.println("ERROR: " + elevation);
					break;
				}
			}
		}
		logger.warn("unmatched: '" + elevation + "'");
		unmatched++;
		return null;
	}

	public void evaluatePatterns()
	{
		for (NamedPattern np : patterns) {
			int occs = counter.occurences(np.name);
			logger.info("Pattern '" + np.name + "': " + occs);
		}
		logger.info("# unmatched: " + unmatched);
		logger.info("# parse errors: " + parseErrors);
	}

	private class PrefixClass
	{
		String name;
		String prefix;
		NumberExtractor extractor;
		LengthUnit unit;

		public PrefixClass(String name, String prefix,
				NumberExtractor extractor, LengthUnit unit)
		{
			this.name = name;
			this.prefix = prefix;
			this.extractor = extractor;
			this.unit = unit;
		}
	}

	private class NamedPattern
	{
		String name;
		Pattern pattern;
		NumberExtractor extractor;
		LengthUnit unit;

		public NamedPattern(String name, Pattern pattern,
				NumberExtractor extractor, LengthUnit unit)
		{
			this.name = name;
			this.pattern = pattern;
			this.extractor = extractor;
			this.unit = unit;
		}

		public Value convert(Matcher matcher)
		{
			NumberValue number = extractor.getNumberResult(matcher);
			return new Value(number, unit);
		}
	}

}

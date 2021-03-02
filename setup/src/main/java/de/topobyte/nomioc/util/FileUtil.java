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

package de.topobyte.nomioc.util;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class FileUtil
{

	public static Set<File> onlyNormalFiles(Set<File> in)
	{
		Set<File> files = new HashSet<>();
		for (File file : in) {
			if (file.isFile()) {
				files.add(file);
			}
		}
		return files;
	}

	public static Set<File> find(File inputFile)
	{
		return find(inputFile, 0, Integer.MAX_VALUE);
	}

	public static Set<File> find(File inputFile, int minDepth, int maxDepth)
	{
		Set<File> files = new HashSet<>();
		collect(files, inputFile, 0, minDepth, maxDepth);
		return files;
	}

	private static void collect(Set<File> files, File inputFile,
			int currentDepth, int minDepth, int maxDepth)
	{
		if (currentDepth >= minDepth && currentDepth <= maxDepth) {
			files.add(inputFile);
		}
		if (currentDepth < maxDepth) {
			if (inputFile.isDirectory()) {
				File[] childs = inputFile.listFiles();
				for (File file : childs) {
					collect(files, file, currentDepth + 1, minDepth, maxDepth);
				}
			}
		}
	}

}
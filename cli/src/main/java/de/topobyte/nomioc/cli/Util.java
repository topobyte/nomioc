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

package de.topobyte.nomioc.cli;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.luqe.jdbc.JdbcConnection;

public class Util
{

	final static Logger logger = LoggerFactory.getLogger(Util.class);

	public static void initJdbc()
	{
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			logger.error("Error while loading SQL driver", e);
		}
	}

	public static JdbcConnection openConnection(String pathDatabase)
			throws SQLException
	{
		String prefix = "jdbc:sqlite:";
		Connection connection = DriverManager
				.getConnection(prefix + pathDatabase);
		return new JdbcConnection(connection);
	}

}

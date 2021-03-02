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

package de.topobyte.nomioc.android.v2.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 */
public class AndroidSessionFactory
{

	private StandardServiceRegistry serviceRegistry;
	private SessionFactory sessionFactory;
	private Configuration cfg = new Configuration();

	/**
	 * Create a new instance of TheSessionfactory.
	 *
	 * @param configPath
	 *            the hibernate configuration path to use
	 */
	public AndroidSessionFactory(String configPath)
	{
		String dbpath = System.getProperty("dbpath");
		if (dbpath != null) {
			cfg.setProperty("hibernate.connection.url", dbpath);
		}

		cfg.configure(configPath);

		serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(cfg.getProperties()).build();

		sessionFactory = cfg.buildSessionFactory(serviceRegistry);
	}

	/**
	 * rebuild the sessionfactory from configuration.
	 */
	public void rebuild()
	{
		sessionFactory = cfg.buildSessionFactory(serviceRegistry);
	}

	/**
	 * @return the SessionFactory;
	 */
	public SessionFactory getSessionFactory()
	{
		return sessionFactory;
	}

	/**
	 * Open a new session.
	 *
	 * @return a session.
	 */
	public Session getSession()
	{
		return sessionFactory.openSession();
	}

	/**
	 * @return the configuration used for creating this instance.
	 */
	public Configuration getConfiguration()
	{
		return cfg;
	}

}

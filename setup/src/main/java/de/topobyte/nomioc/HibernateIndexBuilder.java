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

package de.topobyte.nomioc;

import java.util.List;

import org.hibernate.SessionFactory;

import de.topobyte.nomioc.android.v2.model.hibernate.SpatialIndexItem;
import de.topobyte.sqlitespatial.spatialindex.builder.IndexBuilder;
import de.topobyte.sqlitespatial.spatialindex.builder.Indexable;
import de.topobyte.sqlitespatial.spatialindex.builder.Node;
import de.topobyte.sqlitespatial.spatialindex.builder.Rectangle;

public class HibernateIndexBuilder
{

	public static <T extends Indexable> void buildIndex(List<T> items,
			SessionFactory sfOutput, SpatialIndexFactory factory)
	{
		IndexBuilder<T> indexBuilder = new IndexBuilder<>();
		Node<T> root = indexBuilder.build(items, 128);

		List<Node<T>> leafs = indexBuilder.getLeafs(root);
		for (int i = 0; i < leafs.size(); i++) {
			Node<T> leaf = leafs.get(i);

			Rectangle r = leaf.getEnvelope();
			SpatialIndexItem item = factory.create(i, r.getMinX(), r.getMaxX(),
					r.getMinY(), r.getMaxY());
			System.out.println("persist!");
			sfOutput.getCurrentSession().persist(item);

			List<T> leafItems = leaf.getItems();
			for (T t : leafItems) {
				if (t.getNode() == leaf) {
					t.setSid(i);
				}
			}
		}
	}

}

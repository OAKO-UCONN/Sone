/*
 * Sone - MemoryAlbumBuilder.java - Copyright © 2013 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.data.impl;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.database.AlbumBuilder;
import net.pterodactylus.sone.database.Database;

/**
 * {@link AlbumBuilder} implementation that creates {@link DefaultAlbum}
 * objects.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DefaultAlbumBuilder extends AbstractAlbumBuilder {

	private final Database database;
	private final Sone sone;
	private final String parentId;

	public DefaultAlbumBuilder(Database database, Sone sone, String parentId) {
		this.database = database;
		this.sone = sone;
		this.parentId = parentId;
	}

	@Override
	public Album build() throws IllegalStateException {
		validate();
		DefaultAlbum album = new DefaultAlbum(database, getId(), sone, parentId);
		database.storeAlbum(album);
		return album;
	}

}

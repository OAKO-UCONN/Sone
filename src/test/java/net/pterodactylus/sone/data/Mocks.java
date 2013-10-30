/*
 * Sone - Mocks.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sone.data;

import static com.google.common.base.Optional.of;
import static com.google.common.collect.ArrayListMultimap.create;
import static com.google.common.collect.Ordering.from;
import static com.google.common.collect.Sets.newHashSet;
import static net.pterodactylus.sone.data.Post.TIME_COMPARATOR;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.impl.DefaultPostBuilder;
import net.pterodactylus.sone.data.impl.DefaultPostReplyBuilder;
import net.pterodactylus.sone.database.Database;
import net.pterodactylus.sone.database.PostReplyBuilder;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multimap;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocks reusable in multiple tests.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Mocks {

	private final Multimap<Sone, Post> sonePosts = create();
	private final Collection<Sone> sones = newHashSet();
	public final Database database;
	public final Core core;

	public Mocks() {
		database = mockDatabase();
		core = mockCore(database);
		when(core.getLocalSones()).then(new Answer<Collection<Sone>>() {
			@Override
			public Collection<Sone> answer(InvocationOnMock invocation) throws Throwable {
				return FluentIterable.from(sones).filter(Sone.LOCAL_SONE_FILTER).toList();
			}
		});
	}

	private static Core mockCore(Database database) {
		Core core = mock(Core.class);
		when(core.getDatabase()).thenReturn(database);
		when(core.getSone(anyString())).thenReturn(Optional.<Sone>absent());
		return core;
	}

	private static Database mockDatabase() {
		Database database = mock(Database.class);
		when(database.getSone(anyString())).thenReturn(Optional.<Sone>absent());
		when(database.getPost(anyString())).thenReturn(Optional.<Post>absent());
		when(database.getPostReply(anyString())).thenReturn(Optional.<PostReply>absent());
		return database;
	}

	public SoneMocker mockSone(String id) {
		return new SoneMocker(id);
	}

	public Post mockPost(Sone sone, String postId) {
		Post post = mock(Post.class);
		when(post.getId()).thenReturn(postId);
		when(post.getSone()).thenReturn(sone);
		when(database.getPost(eq(postId))).thenReturn(of(post));
		sonePosts.put(sone, post);
		return post;
	}

	public PostReply mockPostReply(Sone sone, String replyId) {
		PostReply postReply = mock(PostReply.class);
		when(postReply.getId()).thenReturn(replyId);
		when(postReply.getSone()).thenReturn(sone);
		when(database.getPostReply(eq(replyId))).thenReturn(of(postReply));
		return postReply;
	}

	public class SoneMocker {

		private final Sone mockedSone = mock(Sone.class);
		private final String id;
		private boolean local;
		private Profile profile = new Profile(mockedSone);

		private SoneMocker(String id) {
			this.id = id;
		}

		public SoneMocker local() {
			local = true;
			return this;
		}

		public Sone create() {
			when(mockedSone.getId()).thenReturn(id);
			when(mockedSone.isLocal()).thenReturn(local);
			when(mockedSone.getProfile()).thenReturn(profile);
			if (local) {
				when(mockedSone.newPostBuilder()).thenReturn(new DefaultPostBuilder(database, id));
				when(mockedSone.newPostReplyBuilder(anyString())).then(new Answer<PostReplyBuilder>() {
					@Override
					public PostReplyBuilder answer(InvocationOnMock invocation) throws Throwable {
						return new DefaultPostReplyBuilder(database, id, (String) invocation.getArguments()[0]);
					}
				});
			} else {
				when(mockedSone.newPostBuilder()).thenThrow(IllegalStateException.class);
				when(mockedSone.newPostReplyBuilder(anyString())).thenThrow(IllegalStateException.class);
			}
			when(core.getSone(eq(id))).thenReturn(of(mockedSone));
			when(database.getSone(eq(id))).thenReturn(of(mockedSone));
			when(mockedSone.getPosts()).then(new Answer<List<Post>>() {
				@Override
				public List<Post> answer(InvocationOnMock invocationOnMock) throws Throwable {
					return from(TIME_COMPARATOR).sortedCopy(sonePosts.get(mockedSone));
				}
			});
			when(mockedSone.toString()).thenReturn(String.format("Sone[%s]", id));
			sones.add(mockedSone);
			return mockedSone;
		}

	}

}
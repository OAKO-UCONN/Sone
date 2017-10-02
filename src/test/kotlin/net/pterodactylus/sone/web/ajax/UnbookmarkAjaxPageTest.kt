package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.test.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [UnbookmarkAjaxPage].
 */
class UnbookmarkAjaxPageTest : JsonPageTest("unbookmark.ajax", requiresLogin = false, needsFormPassword = true, pageSupplier = ::UnbookmarkAjaxPage) {

	@Test
	fun `request without post id results in invalid-post-id`() {
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-post-id"))
	}

	@Test
	fun `request with empty post id results in invalid-post-id`() {
		addRequestParameter("post", "")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-post-id"))
	}

	@Test
	fun `request with invalid post id does not unbookmark anything but succeeds`() {
		addRequestParameter("post", "invalid")
		assertThat(json.isSuccess, equalTo(true))
		verify(core, never()).unbookmarkPost(any())
	}

	@Test
	fun `request with valid post id does not unbookmark anything but succeeds`() {
		val post = mock<Post>()
		addPost(post, "post-id")
		addRequestParameter("post", "post-id")
		assertThat(json.isSuccess, equalTo(true))
		verify(core).unbookmarkPost(eq(post))
	}

}

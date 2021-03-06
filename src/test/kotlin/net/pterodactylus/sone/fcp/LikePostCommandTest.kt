package net.pterodactylus.sone.fcp

import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.freenet.fcp.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [LikePostCommand].
 */
class LikePostCommandTest : SoneCommandTest() {

	private val post = createPost("PostId", mock(), null, 1000, "Text")

	override fun createCommand(core: Core) = LikePostCommand(core)

	@Before
	fun setupPostAndSones() {
		whenever(core.getPost("PostId")).thenReturn(post)
		whenever(core.getSone("RemoteSoneId")).thenReturn(remoteSone)
		whenever(core.getSone("LocalSoneId")).thenReturn(localSone)
	}

	@Test
	fun `command requires write access`() {
		assertThat(command.requiresWriteAccess, equalTo(true))
	}

	@Test
	fun `request without parameters results in FCP exception`() {
		requestWithoutAnyParameterResultsInFcpException()
	}

	@Test
	fun `request with invalid post id results in FCP exception`() {
		parameters += "Post" to "InvalidPostId"
		expectedException.expect(FcpException::class.java)
		command.execute(parameters)
	}

	@Test
	fun `request with missing local sone results in FCP exception`() {
		parameters += "Post" to "PostId"
		expectedException.expect(FcpException::class.java)
		command.execute(parameters)
	}

	@Test
	fun `request with invalid sone results in FCP exception`() {
		parameters += "Post" to "PostId"
		parameters += "Sone" to "InvalidSoneId"
		expectedException.expect(FcpException::class.java)
		command.execute(parameters)
	}

	@Test
	fun `request with valid remote sone results in FCP exception`() {
		parameters += "Post" to "PostId"
		parameters += "Sone" to "RemoteSoneId"
		expectedException.expect(FcpException::class.java)
		command.execute(parameters)
	}

	@Test
	fun `request with valid parameters adds post to liked posts for sone`() {
		whenever(core.getLikes(post)).thenReturn(setOf(mock(), mock(), mock()))
		parameters += "Post" to "PostId"
		parameters += "Sone" to "LocalSoneId"
		val replyParameters = command.execute(parameters).replyParameters
		assertThat(replyParameters["Message"], equalTo("PostLiked"))
		assertThat(replyParameters["LikeCount"], equalTo("3"))
		verify(localSone).addLikedPostId("PostId")
	}

}

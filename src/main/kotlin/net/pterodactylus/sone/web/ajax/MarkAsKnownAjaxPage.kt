package net.pterodactylus.sone.web.ajax

import com.google.common.base.Optional
import net.pterodactylus.sone.utils.mapPresent
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * AJAX page that lets the user mark a number of [net.pterodactylus.sone.data.Sone]s,
 * [net.pterodactylus.sone.data.Post]s, or [net.pterodactylus.sone.data.Reply]s as known.
 */
class MarkAsKnownAjaxPage(webInterface: WebInterface) : JsonPage("markAsKnown.ajax", webInterface) {

	override val requiresLogin = false

	override fun createJsonObject(request: FreenetRequest) = when (request.parameters["type"]) {
		"sone" -> processIds(request, core::getSone, core::markSoneKnown)
		"post" -> processIds(request, core::getPost, core::markPostKnown)
		"reply" -> processIds(request, core::getPostReply, core::markReplyKnown)
		else -> createErrorJsonObject("invalid-type")
	}

	private fun <T> processIds(request: FreenetRequest, getter: (String) -> Optional<T>, marker: (T) -> Unit) =
			request.parameters["id"]
					?.split(Regex(" +"))
					?.mapPresent(getter)
					?.onEach(marker)
					.let { createSuccessJsonObject() }

}

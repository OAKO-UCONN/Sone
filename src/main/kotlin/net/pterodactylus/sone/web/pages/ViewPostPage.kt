package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.template.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import java.net.*
import javax.inject.*

/**
 * This page lets the user view a post and all its replies.
 */
class ViewPostPage @Inject constructor(template: Template, webInterface: WebInterface):
		SoneTemplatePage("viewPost.html", webInterface, template, "Page.ViewPost.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		templateContext["post"] = soneRequest.parameters["post"]?.let(soneRequest.core::getPost)
		templateContext["raw"] = soneRequest.parameters["raw"] == "true"
	}

	override fun isLinkExcepted(link: URI) = true

	override fun getPageTitle(soneRequest: SoneRequest) =
			(soneRequest.parameters["post"]?.let(soneRequest.core::getPost)?.let {
				if (it.text.length > 20) {
					it.text.substring(0..19) + "…"
				} else {
					it.text
				} + " - ${SoneAccessor.getNiceName(it.sone)} - "
			} ?: "") + super.getPageTitle(soneRequest)

}

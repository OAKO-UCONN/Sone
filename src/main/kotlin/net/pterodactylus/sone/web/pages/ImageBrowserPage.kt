package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import java.net.*
import javax.inject.*

/**
 * The image browser page is the entry page for the image management.
 */
@MenuName("ImageBrowser")
@TemplatePath("/templates/imageBrowser.html")
@ToadletPath("imageBrowser.html")
class ImageBrowserPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("Page.ImageBrowser.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if ("album" in soneRequest.parameters) {
			templateContext["albumRequested"] = true
			templateContext["album"] = soneRequest.core.getAlbum(soneRequest.parameters["album"]!!)
			templateContext["page"] = soneRequest.parameters["page"]
		} else if ("image" in soneRequest.parameters) {
			templateContext["imageRequested"] = true
			templateContext["image"] = soneRequest.core.getImage(soneRequest.parameters["image"])
		} else if (soneRequest.parameters["mode"] == "gallery") {
			templateContext["galleryRequested"] = true
			soneRequest.core.sones
					.flatMap(Sone::allAlbums)
					.filterNot(Album::isEmpty)
					.sortedBy(Album::getTitle)
					.also { albums ->
						albums.paginate(soneRequest.core.preferences.imagesPerPage)
								.turnTo(soneRequest.parameters["page"]?.toIntOrNull() ?: 0)
								.also { pagination ->
									templateContext["albumPagination"] = pagination
									templateContext["albums"] = pagination.items
								}
					}
		} else {
			templateContext["soneRequested"] = true
			templateContext["sone"] = soneRequest.core.getSone(soneRequest.httpRequest.getParam("sone")) ?: currentSone
		}
	}

	override fun isLinkExcepted(link: URI) = true

}

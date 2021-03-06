/*
 * Sone - FreenetTemplatePage.kt - Copyright © 2010–2020 David Roden
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

package net.pterodactylus.sone.web.page

import freenet.clients.http.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.util.template.*
import net.pterodactylus.util.web.*
import java.lang.String.*
import java.net.*
import java.util.logging.*
import java.util.logging.Logger.*

/**
 * Base class for all [Page]s that are rendered with [Template]s and
 * fit into Freenet’s web interface.
 */
open class FreenetTemplatePage(
		private val templateRenderer: TemplateRenderer,
		loaders: Loaders,
		private val invalidFormPasswordRedirectTarget: String
) : FreenetPage, LinkEnabledCallback {

	private val pageMakerInteractionFactory: PageMakerInteractionFactory = DefaultPageMakerInteractionFactory()
	open val styleSheets: Collection<String> = emptySet()
	open val shortcutIcon: String? get() = null
	open val isFullAccessOnly get() = false

	override fun getPath() = toadletPath

	open fun getPageTitle(request: FreenetRequest) = ""

	override fun isPrefixPage() = false

	open fun getRedirectTarget(request: FreenetRequest): String? = null

	open fun getAdditionalLinkNodes(request: FreenetRequest): List<Map<String, String>> = emptyList()

	override fun isLinkExcepted(link: URI) = false

	override fun isEnabled(toadletContext: ToadletContext) = !isFullAccessOnly

	private val template = templatePath?.let(loaders::loadTemplate) ?: Template()

	override fun handleRequest(request: FreenetRequest, response: Response): Response {
		getRedirectTarget(request)?.let { redirectTarget -> return RedirectResponse(redirectTarget) }

		if (isFullAccessOnly && !request.toadletContext.isAllowedFullAccess) {
			return response.setStatusCode(401).setStatusText("Not authorized").setContentType("text/html")
		}
		val toadletContext = request.toadletContext
		if (request.method == Method.POST) {
			/* require form password. */
			val formPassword = request.httpRequest.getPartAsStringFailsafe("formPassword", 32)
			if (formPassword != toadletContext.container.formPassword) {
				return RedirectResponse(invalidFormPasswordRedirectTarget)
			}
		}

		val pageMakerInteraction = pageMakerInteractionFactory.createPageMaker(toadletContext, getPageTitle(request))
		styleSheets.forEach(pageMakerInteraction::addStyleSheet)
		getAdditionalLinkNodes(request).forEach(pageMakerInteraction::addLinkNode)
		shortcutIcon?.let(pageMakerInteraction::addShortcutIcon)

		val output = try {
			val start = System.nanoTime()
			templateRenderer.render(template) { templateContext ->
				processTemplate(request, templateContext)
			}.also {
				val finish = System.nanoTime()
				logger.log(Level.FINEST, format("Template was rendered in %.2fms.", (finish - start) / 1000000.0))
			}
		} catch (re1: RedirectException) {
			return RedirectResponse(re1.target ?: "")
		}

		pageMakerInteraction.setContent(output)

		return response.setStatusCode(200).setStatusText("OK").setContentType("text/html").write(pageMakerInteraction.renderPage())
	}

	open fun processTemplate(request: FreenetRequest, templateContext: TemplateContext) {
		/* do nothing. */
	}

	fun redirectTo(target: String?): Nothing =
			throw RedirectException(target)

	class RedirectException(val target: String?) : Exception() {
		override fun toString(): String = format("RedirectException{target='%s'}", target)
	}

}

private val logger: Logger = getLogger(FreenetTemplatePage::class.java.name)

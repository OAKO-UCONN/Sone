package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*

@TemplatePath("/templates/invalid.html")
@ToadletPath("invalid.html")
class InvalidPage(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) : SoneTemplatePage(webInterface, loaders, templateRenderer, "Page.Invalid.Title")

@TemplatePath("/templates/noPermission.html")
@ToadletPath("noPermission.html")
class NoPermissionPage(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) : SoneTemplatePage(webInterface, loaders, templateRenderer, "Page.NoPermission.Title")

@TemplatePath("/templates/emptyImageTitle.html")
@ToadletPath("emptyImageTitle.html")
class EmptyImageTitlePage(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) : SoneTemplatePage(webInterface, loaders, templateRenderer, "Page.EmptyImageTitle.Title")

@TemplatePath("/templates/emptyAlbumTitle.html")
@ToadletPath("emptyAlbumTitle.html")
class EmptyAlbumTitlePage(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) : SoneTemplatePage(webInterface, loaders, templateRenderer, "Page.EmptyAlbumTitle.Title")

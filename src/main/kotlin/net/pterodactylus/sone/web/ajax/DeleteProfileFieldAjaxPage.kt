package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * AJAX page that lets the user delete a profile field.
 */
class DeleteProfileFieldAjaxPage(webInterface: WebInterface) : JsonPage("deleteProfileField.ajax", webInterface) {

	override fun createJsonObject(request: FreenetRequest) =
			getCurrentSone(request.toadletContext)!!.let { currentSone ->
				currentSone.profile.let { profile ->
					request.parameters["field"]
							?.let(profile::getFieldById)
							?.let { field ->
								createSuccessJsonObject().also {
									profile.removeField(field)
									currentSone.profile = profile
									webInterface.core.touchConfiguration()
								}
							} ?: createErrorJsonObject("invalid-field-id")
				}
			}

}

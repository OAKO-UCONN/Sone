/*
 * Sone - DismissNotificationAjaxPage.java - Copyright © 2010–2016 David Roden
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

package net.pterodactylus.sone.web.ajax;

import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.notify.Notification;

import com.google.common.base.Optional;

/**
 * AJAX page that lets the user dismiss a notification.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DismissNotificationAjaxPage extends JsonPage {

	/**
	 * Creates a new “dismiss notification” AJAX handler.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public DismissNotificationAjaxPage(WebInterface webInterface) {
		super("dismissNotification.ajax", webInterface);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonReturnObject createJsonObject(FreenetRequest request) {
		String notificationId = request.getHttpRequest().getParam("notification");
		Optional<Notification> notification = webInterface.getNotification(notificationId);
		if (!notification.isPresent()) {
			return createErrorJsonObject("invalid-notification-id");
		}
		if (!notification.get().isDismissable()) {
			return createErrorJsonObject("not-dismissable");
		}
		notification.get().dismiss();
		return createSuccessJsonObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean requiresLogin() {
		return false;
	}

}

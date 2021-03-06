/**
 * Sone - NotificationHandler.kt - Copyright © 2019–2020 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.web.notification

import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.text.*
import javax.inject.*

/**
 * Container that causes notification handlers to be created and (more importantly) registered
 * on creation with the event bus.
 */
@Suppress("UNUSED_PARAMETER")
class NotificationHandler @Inject constructor(
		markPostKnownDuringFirstStartHandler: MarkPostKnownDuringFirstStartHandler,
		markPostReplyKnownDuringFirstStartHandler: MarkPostReplyKnownDuringFirstStartHandler,
		newSoneHandler: NewSoneHandler,
		newRemotePostHandler: NewRemotePostHandler,
		remotePostReplyHandler: RemotePostReplyHandler,
		soneLockedOnStartupHandler: SoneLockedOnStartupHandler,
		soneLockedHandler: SoneLockedHandler,
		localPostHandler: LocalPostHandler,
		localReplyHandler: LocalReplyHandler,
		newVersionHandler: NewVersionHandler,
		imageInsertHandler: ImageInsertHandler,
		firstStartHandler: FirstStartHandler,
		configNotReadHandler: ConfigNotReadHandler,
		startupHandler: StartupHandler,
		webOfTrustPinger: WebOfTrustPinger,
		webOfTrustHandler: WebOfTrustHandler,
		soneMentionDetector: SoneMentionDetector,
		soneMentionedHandler: SoneMentionedHandler,
		soneInsertHandler: SoneInsertHandler
)

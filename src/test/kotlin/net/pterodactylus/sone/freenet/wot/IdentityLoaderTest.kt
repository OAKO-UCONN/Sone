/*
 * Sone - IdentityLoaderTest.kt - Copyright © 2013–2020 David Roden
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

package net.pterodactylus.sone.freenet.wot

import net.pterodactylus.sone.test.createIdentity
import net.pterodactylus.sone.test.createOwnIdentity
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.junit.Test

/**
 * Unit test for [IdentityLoader].
 */
class IdentityLoaderTest {

	private val ownIdentities = createOwnIdentities()
	private val webOfTrustConnector = object : TestWebOfTrustConnector() {
		override fun loadAllOwnIdentities() = ownIdentities.toSet()
		override fun loadTrustedIdentities(ownIdentity: OwnIdentity, context: String?) =
				when (ownIdentity) {
					ownIdentities[0] -> createTrustedIdentitiesForFirstOwnIdentity()
					ownIdentities[1] -> createTrustedIdentitiesForSecondOwnIdentity()
					ownIdentities[2] -> createTrustedIdentitiesForThirdOwnIdentity()
					ownIdentities[3] -> createTrustedIdentitiesForFourthOwnIdentity()
					else -> throw RuntimeException()
				}
	}

	@Test
	fun loadingIdentities() {
		val identityLoader = IdentityLoader(webOfTrustConnector, Context("Test"))
		val identities = identityLoader.loadTrustedIdentities()
		assertThat(identities.keys, hasSize(4))
		assertThat(identities.keys, containsInAnyOrder(ownIdentities[0], ownIdentities[1], ownIdentities[2], ownIdentities[3]))
		verifyIdentitiesForOwnIdentity(identities, ownIdentities[0], createTrustedIdentitiesForFirstOwnIdentity())
		verifyIdentitiesForOwnIdentity(identities, ownIdentities[1], createTrustedIdentitiesForSecondOwnIdentity())
		verifyIdentitiesForOwnIdentity(identities, ownIdentities[2], emptySet())
		verifyIdentitiesForOwnIdentity(identities, ownIdentities[3], createTrustedIdentitiesForFourthOwnIdentity())
	}

	@Test
	fun loadingIdentitiesWithoutContext() {
		val identityLoaderWithoutContext = IdentityLoader(webOfTrustConnector)
		val identities = identityLoaderWithoutContext.loadTrustedIdentities()
		assertThat(identities.keys, hasSize(4))
		assertThat(identities.keys, containsInAnyOrder(ownIdentities[0], ownIdentities[1], ownIdentities[2], ownIdentities[3]))
		verifyIdentitiesForOwnIdentity(identities, ownIdentities[0], createTrustedIdentitiesForFirstOwnIdentity())
		verifyIdentitiesForOwnIdentity(identities, ownIdentities[1], createTrustedIdentitiesForSecondOwnIdentity())
		verifyIdentitiesForOwnIdentity(identities, ownIdentities[2], createTrustedIdentitiesForThirdOwnIdentity())
		verifyIdentitiesForOwnIdentity(identities, ownIdentities[3], createTrustedIdentitiesForFourthOwnIdentity())
	}

	private fun verifyIdentitiesForOwnIdentity(identities: Map<OwnIdentity, Collection<Identity>>, ownIdentity: OwnIdentity, trustedIdentities: Set<Identity>) {
		assertThat(identities[ownIdentity], equalTo<Collection<Identity>>(trustedIdentities))
	}

	@Test
	fun `loading all identities merges remote identities’ trust values`() {
		val ownIdentity1 = createOwnIdentity("o1")
		val ownIdentity2 = createOwnIdentity("o2")
		val webOfTrustConnector = object : TestWebOfTrustConnector() {
			override fun loadAllOwnIdentities() = setOf(ownIdentity1, ownIdentity2)
			override fun loadAllIdentities(ownIdentity: OwnIdentity, context: String?) = when (ownIdentity) {
				ownIdentity1 -> setOf(createIdentity().setTrust(ownIdentity1, Trust(100, 50, 2)))
				else -> setOf(createIdentity().setTrust(ownIdentity2, Trust(80, 40, 2)))
			}
		}
		val identityLoader = IdentityLoader(webOfTrustConnector)
		val allIdentities = identityLoader.loadAllIdentities()
		assertThat(allIdentities[ownIdentity1]!!.first().trust[ownIdentity2], notNullValue())
	}

	@Test
	fun `loading trusted identities merges remote identities’ trust values`() {
		val ownIdentity1 = createOwnIdentity("o1")
		val ownIdentity2 = createOwnIdentity("o2")
		val webOfTrustConnector = object : TestWebOfTrustConnector() {
			override fun loadAllOwnIdentities() = setOf(ownIdentity1, ownIdentity2)
			override fun loadTrustedIdentities(ownIdentity: OwnIdentity, context: String?) = when (ownIdentity) {
				ownIdentity1 -> setOf(createIdentity().setTrust(ownIdentity1, Trust(100, 50, 2)))
				else -> setOf(createIdentity().setTrust(ownIdentity2, Trust(80, 40, 2)))
			}
		}
		val identityLoader = IdentityLoader(webOfTrustConnector)
		val allIdentities = identityLoader.loadTrustedIdentities()
		assertThat(allIdentities[ownIdentity1]!!.first().trust[ownIdentity2], notNullValue())
	}

}

private fun createOwnIdentities() = listOf(
		createOwnIdentity("O1", "ON1", "OR1", "OI1", setOf("Test", "Test2"), mapOf("KeyA" to "ValueA", "KeyB" to "ValueB")),
		createOwnIdentity("O2", "ON2", "OR2", "OI2", setOf("Test"), mapOf("KeyC" to "ValueC")),
		createOwnIdentity("O3", "ON3", "OR3", "OI3", setOf("Test2"), mapOf("KeyE" to "ValueE", "KeyD" to "ValueD")),
		createOwnIdentity("O4", "ON4", "OR$", "OI4", setOf("Test"), mapOf("KeyA" to "ValueA", "KeyD" to "ValueD"))
)

private fun createTrustedIdentitiesForFirstOwnIdentity() = setOf(
		createIdentity("I11", "IN11", "IR11", setOf("Test"), mapOf("KeyA" to "ValueA"))
)

private fun createTrustedIdentitiesForSecondOwnIdentity() = setOf(
		createIdentity("I21", "IN21", "IR21", setOf("Test", "Test2"), mapOf("KeyB" to "ValueB"))
)

private fun createTrustedIdentitiesForThirdOwnIdentity() = setOf(
		createIdentity("I31", "IN31", "IR31", setOf("Test", "Test3"), mapOf("KeyC" to "ValueC"))
)

private fun createTrustedIdentitiesForFourthOwnIdentity(): Set<Identity> = emptySet()

private open class TestWebOfTrustConnector : WebOfTrustConnector {

	override fun loadAllOwnIdentities() = emptySet<OwnIdentity>()
	override fun loadTrustedIdentities(ownIdentity: OwnIdentity, context: String?) = emptySet<Identity>()
	override fun loadAllIdentities(ownIdentity: OwnIdentity, context: String?) = emptySet<Identity>()
	override fun addContext(ownIdentity: OwnIdentity, context: String) = Unit
	override fun removeContext(ownIdentity: OwnIdentity, context: String) = Unit
	override fun setProperty(ownIdentity: OwnIdentity, name: String, value: String) = Unit
	override fun removeProperty(ownIdentity: OwnIdentity, name: String) = Unit
	override fun ping() = Unit

}

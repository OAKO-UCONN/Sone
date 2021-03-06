package net.pterodactylus.sone.core

import com.codahale.metrics.*
import com.google.common.base.*
import com.google.common.base.Optional
import com.google.common.eventbus.*
import com.google.common.util.concurrent.MoreExecutors.*
import freenet.keys.*
import net.pterodactylus.sone.core.SoneInserter.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.*
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.hamcrest.MockitoHamcrest.*
import org.mockito.stubbing.*
import java.lang.System.*
import java.util.*
import kotlin.test.Test

/**
 * Unit test for [SoneInserter] and its subclasses.
 */
class SoneInserterTest {

	private val metricRegistry = MetricRegistry()
	private val core = mock<Core>()
	private val eventBus = mock<EventBus>()
	private val freenetInterface = mock<FreenetInterface>()
	private val soneUriCreator = object : SoneUriCreator() {
		override fun getInsertUri(sone: Sone): FreenetURI = expectedInsertUri
	}

	@Before
	fun setupCore() {
		val updateChecker = mock<UpdateChecker>()
		whenever(core.updateChecker).thenReturn(updateChecker)
		whenever(core.getSone(anyString())).thenReturn(null)
	}

	@Test
	fun `insertion delay is forwarded to sone inserter`() {
		val eventBus = AsyncEventBus(directExecutor())
		eventBus.register(SoneInserter(core, eventBus, freenetInterface, metricRegistry, soneUriCreator, "SoneId"))
		eventBus.post(InsertionDelayChangedEvent(15))
		assertThat(SoneInserter.getInsertionDelay().get(), equalTo(15))
	}

	private fun createSone(insertUri: FreenetURI, fingerprint: String = "fingerprint"): Sone {
		val ownIdentity = DefaultOwnIdentity("", "", "", insertUri.toString())
		val sone = mock<Sone>()
		whenever(sone.identity).thenReturn(ownIdentity)
		whenever(sone.fingerprint).thenReturn(fingerprint)
		whenever(sone.rootAlbum).thenReturn(mock())
		whenever(core.getSone(anyString())).thenReturn(sone)
		return sone
	}

	@Test
	fun `isModified is true if modification detector says so`() {
		val soneModificationDetector = mock<SoneModificationDetector>()
		whenever(soneModificationDetector.isModified).thenReturn(true)
		val soneInserter = SoneInserter(core, eventBus, freenetInterface, metricRegistry, soneUriCreator, "SoneId", soneModificationDetector, 1)
		assertThat(soneInserter.isModified, equalTo(true))
	}

	@Test
	fun `isModified is false if modification detector says so`() {
		val soneModificationDetector = mock<SoneModificationDetector>()
		val soneInserter = SoneInserter(core, eventBus, freenetInterface, metricRegistry, soneUriCreator, "SoneId", soneModificationDetector, 1)
		assertThat(soneInserter.isModified, equalTo(false))
	}

	@Test
	fun `last fingerprint is stored correctly`() {
		val soneInserter = SoneInserter(core, eventBus, freenetInterface, metricRegistry, soneUriCreator, "SoneId")
		soneInserter.lastInsertFingerprint = "last-fingerprint"
		assertThat(soneInserter.lastInsertFingerprint, equalTo("last-fingerprint"))
	}

	@Test
	fun `sone inserter stops when it should`() {
		val soneInserter = SoneInserter(core, eventBus, freenetInterface, metricRegistry, soneUriCreator, "SoneId")
		soneInserter.stop()
		soneInserter.serviceRun()
	}

	@Test
	fun `sone inserter inserts a sone if it is eligible`() {
		val finalUri = mock<FreenetURI>()
		val sone = createSone(insertUri)
		val soneModificationDetector = mock<SoneModificationDetector>()
		whenever(soneModificationDetector.isEligibleForInsert).thenReturn(true)
		whenever(freenetInterface.insertDirectory(eq(expectedInsertUri), any<HashMap<String, Any>>(), eq("index.html"))).thenReturn(finalUri)
		val soneInserter = SoneInserter(core, eventBus, freenetInterface, metricRegistry, soneUriCreator, "SoneId", soneModificationDetector, 1)
		doAnswer {
			soneInserter.stop()
			null
		}.whenever(core).touchConfiguration()
		soneInserter.serviceRun()
		val soneEvents = ArgumentCaptor.forClass(SoneEvent::class.java)
		verify(freenetInterface).insertDirectory(eq(expectedInsertUri), any<HashMap<String, Any>>(), eq("index.html"))
		verify(eventBus, times(2)).post(soneEvents.capture())
		assertThat(soneEvents.allValues[0], instanceOf(SoneInsertingEvent::class.java))
		assertThat(soneEvents.allValues[0].sone, equalTo(sone))
		assertThat(soneEvents.allValues[1], instanceOf(SoneInsertedEvent::class.java))
		assertThat(soneEvents.allValues[1].sone, equalTo(sone))
	}

	@Test
	fun `sone inserter bails out if it is stopped while inserting`() {
		val finalUri = mock<FreenetURI>()
		val sone = createSone(insertUri)
		val soneModificationDetector = mock<SoneModificationDetector>()
		whenever(soneModificationDetector.isEligibleForInsert).thenReturn(true)
		val soneInserter = SoneInserter(core, eventBus, freenetInterface, metricRegistry, soneUriCreator, "SoneId", soneModificationDetector, 1)
		whenever(freenetInterface.insertDirectory(eq(expectedInsertUri), any<HashMap<String, Any>>(), eq("index.html"))).thenAnswer {
			soneInserter.stop()
			finalUri
		}
		soneInserter.serviceRun()
		val soneEvents = ArgumentCaptor.forClass(SoneEvent::class.java)
		verify(freenetInterface).insertDirectory(eq(expectedInsertUri), any<HashMap<String, Any>>(), eq("index.html"))
		verify(eventBus, times(2)).post(soneEvents.capture())
		assertThat(soneEvents.allValues[0], instanceOf(SoneInsertingEvent::class.java))
		assertThat(soneEvents.allValues[0].sone, equalTo(sone))
		assertThat(soneEvents.allValues[1], instanceOf(SoneInsertedEvent::class.java))
		assertThat(soneEvents.allValues[1].sone, equalTo(sone))
		verify(core, never()).touchConfiguration()
	}

	@Test
	fun `sone inserter does not insert sone if it is not eligible`() {
		createSone(insertUri)
		val soneModificationDetector = mock<SoneModificationDetector>()
		val soneInserter = SoneInserter(core, eventBus, freenetInterface, metricRegistry, soneUriCreator, "SoneId", soneModificationDetector, 1)
		Thread(Runnable {
			try {
				Thread.sleep(500)
			} catch (ie1: InterruptedException) {
				throw RuntimeException(ie1)
			}

			soneInserter.stop()
		}).start()
		soneInserter.serviceRun()
		verify(freenetInterface, never()).insertDirectory(eq(expectedInsertUri), any<HashMap<String, Any>>(), eq("index.html"))
		verify(eventBus, never()).post(argThat(org.hamcrest.Matchers.any(SoneEvent::class.java)))
	}

	@Test
	fun `sone inserter posts aborted event if an exception occurs`() {
		val sone = createSone(insertUri)
		val soneModificationDetector = mock<SoneModificationDetector>()
		whenever(soneModificationDetector.isEligibleForInsert).thenReturn(true)
		val soneInserter = SoneInserter(core, eventBus, freenetInterface, metricRegistry, soneUriCreator, "SoneId", soneModificationDetector, 1)
		val soneException = SoneException(Exception())
		whenever(freenetInterface.insertDirectory(eq(expectedInsertUri), any<HashMap<String, Any>>(), eq("index.html"))).thenAnswer {
			soneInserter.stop()
			throw soneException
		}
		soneInserter.serviceRun()
		val soneEvents = ArgumentCaptor.forClass(SoneEvent::class.java)
		verify(freenetInterface).insertDirectory(eq(expectedInsertUri), any<HashMap<String, Any>>(), eq("index.html"))
		verify(eventBus, times(2)).post(soneEvents.capture())
		assertThat(soneEvents.allValues[0], instanceOf(SoneInsertingEvent::class.java))
		assertThat(soneEvents.allValues[0].sone, equalTo(sone))
		assertThat(soneEvents.allValues[1], instanceOf(SoneInsertAbortedEvent::class.java))
		assertThat(soneEvents.allValues[1].sone, equalTo(sone))
		verify(core, never()).touchConfiguration()
	}

	@Test
	fun `sone inserter exits if sone is unknown`() {
		val soneModificationDetector = mock<SoneModificationDetector>()
		val soneInserter = SoneInserter(core, eventBus, freenetInterface, metricRegistry, soneUriCreator, "SoneId", soneModificationDetector, 1)
		whenever(soneModificationDetector.isEligibleForInsert).thenReturn(true)
		whenever(core.getSone("SoneId")).thenReturn(null)
		soneInserter.serviceRun()
	}

	@Test
	fun `sone inserter catches exception and continues`() {
		val soneModificationDetector = mock<SoneModificationDetector>()
		val soneInserter = SoneInserter(core, eventBus, freenetInterface, metricRegistry, soneUriCreator, "SoneId", soneModificationDetector, 1)
		val stopInserterAndThrowException = Answer<Optional<Sone>> {
			soneInserter.stop()
			throw NullPointerException()
		}
		whenever(soneModificationDetector.isEligibleForInsert).thenAnswer(stopInserterAndThrowException)
		soneInserter.serviceRun()
	}

	@Test
	fun `template is rendered correctly for manifest element`() {
		val soneProperties = HashMap<String, Any>()
		soneProperties["id"] = "SoneId"
		val manifestCreator = ManifestCreator(core, soneProperties)
		val now = currentTimeMillis()
		whenever(core.startupTime).thenReturn(now)
		val manifestElement = manifestCreator.createManifestElement("test.txt", "plain/text; charset=utf-8", "sone-inserter-manifest.txt")
		assertThat(manifestElement!!.name, equalTo("test.txt"))
		assertThat(manifestElement.mimeTypeOverride, equalTo("plain/text; charset=utf-8"))
		val templateContent = String(manifestElement.data.inputStream.readBytes(), Charsets.UTF_8)
		assertThat(templateContent, containsString("Sone Version: ${SonePlugin.getPluginVersion()}\n"))
		assertThat(templateContent, containsString("Core Startup: $now\n"))
		assertThat(templateContent, containsString("Sone ID: SoneId\n"))
	}

	@Test
	fun `invalid template returns a null manifest element`() {
		val soneProperties = HashMap<String, Any>()
		val manifestCreator = ManifestCreator(core, soneProperties)
		assertThat(manifestCreator.createManifestElement("test.txt",
				"plain/text; charset=utf-8",
				"sone-inserter-invalid-manifest.txt"),
				nullValue())
	}

	@Test
	fun `error while rendering template returns a null manifest element`() {
		val soneProperties = HashMap<String, Any>()
		val manifestCreator = ManifestCreator(core, soneProperties)
		whenever(core.toString()).thenThrow(NullPointerException::class.java)
		assertThat(manifestCreator.createManifestElement("test.txt",
				"plain/text; charset=utf-8",
				"sone-inserter-faulty-manifest.txt"),
				nullValue())
	}

	@Test
	fun `successful insert updates metrics`() {
		val finalUri = mock<FreenetURI>()
		createSone(insertUri)
		val soneModificationDetector = mock<SoneModificationDetector>()
		whenever(soneModificationDetector.isEligibleForInsert).thenReturn(true)
		whenever(freenetInterface.insertDirectory(eq(expectedInsertUri), any<HashMap<String, Any>>(), eq("index.html"))).thenReturn(finalUri)
		val soneInserter = SoneInserter(core, eventBus, freenetInterface, metricRegistry, soneUriCreator, "SoneId", soneModificationDetector, 1)
		doAnswer {
			soneInserter.stop()
			null
		}.whenever(core).touchConfiguration()
		soneInserter.serviceRun()
		val histogram = metricRegistry.histogram("sone.insert.duration")
		assertThat(histogram.count, equalTo(1L))
	}

	@Test
	fun `unsuccessful insert does not update histogram but records error`() {
		createSone(insertUri)
		val soneModificationDetector = mock<SoneModificationDetector>()
		whenever(soneModificationDetector.isEligibleForInsert).thenReturn(true)
		val soneInserter = SoneInserter(core, eventBus, freenetInterface, metricRegistry, soneUriCreator, "SoneId", soneModificationDetector, 1)
		whenever(freenetInterface.insertDirectory(eq(expectedInsertUri), any<HashMap<String, Any>>(), eq("index.html"))).thenAnswer {
			soneInserter.stop()
			throw SoneException(Exception())
		}
		soneInserter.serviceRun()
		val histogram = metricRegistry.histogram("sone.insert.duration")
		assertThat(histogram.count, equalTo(0L))
		val meter = metricRegistry.meter("sone.insert.errors")
		assertThat(meter.count, equalTo(1L))
	}

}

val insertUri = createInsertUri
val expectedInsertUri = createInsertUri

package pp.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.quarkus.test.junit.QuarkusTest
import jakarta.websocket.ClientEndpoint
import jakarta.websocket.ContainerProvider
import jakarta.websocket.OnMessage
import jakarta.websocket.OnOpen
import jakarta.websocket.Session
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pp.api.data.ChangeName
import pp.api.data.ChatMessage
import pp.api.data.ClientBroadcast
import pp.api.data.GamePhase.CARDS_REVEALED
import pp.api.data.GamePhase.PLAYING
import pp.api.data.LogEntry
import pp.api.data.LogLevel
import pp.api.data.PlayCard
import pp.api.data.RevealCards
import pp.api.data.StartNewRound
import pp.api.data.UserRequest
import pp.api.data.UserType.PARTICIPANT
import pp.api.dto.RoomDto
import pp.api.dto.UserDto
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS

private val mapper = jacksonObjectMapper()

/**
 * An integration test for the websocket connection.
 */
@QuarkusTest
class RealWebSocketIntegrationTest {
    @Test
    @Suppress(
        "LongMethod",
        "TOO_LONG_FUNCTION",
        "What can you do?",
    )
    fun testWebSocketCommunication() {
        val client = TestWebSocketClient()

        val username = "testuser"

        val container = ContainerProvider.getWebSocketContainer()
        val session = container.connectToServer(
            client,
            URI("ws://localhost:8081/rooms/test-room?user=$username&userType=PARTICIPANT"),
        )

        try {
            assertTrue(client.connectionFuture.get(5, SECONDS), "Connection timed out")

            client.sendMessageAndAwaitUpdate(2, ChatMessage("Hello from integration test")) {
                assertEquals(4L, version)
                assertTrue {
                    log.contains(
                        LogEntry(
                            level = LogLevel.CHAT,
                            message = "[$username]: Hello from integration test",
                        ),
                    )
                }
            }

            client.sendMessageAndAwaitUpdate(1, PlayCard("5")) {
                assertEquals(5L, version)
                assertTrue { users == listOf(UserDto(username, PARTICIPANT, true, "5")) }
            }

            client.sendMessageAndAwaitUpdate(1, ChangeName("Test User")) {
                assertEquals(6L, version)
                assertTrue { users == listOf(UserDto("Test User", PARTICIPANT, true, "5")) }
                assertTrue {
                    log.contains(
                        LogEntry(
                            level = LogLevel.INFO,
                            message = "User $username changed name to Test User",
                        ),
                    )
                }
            }

            client.sendMessageAndAwaitUpdate(1, RevealCards()) {
                assertEquals(8, version)
                assertEquals(gamePhase, CARDS_REVEALED)
            }

            client.sendMessageAndAwaitUpdate(1, StartNewRound()) {
                assertEquals(10, version)
                assertEquals(gamePhase, PLAYING)
                assertTrue { users == listOf(UserDto("Test User", PARTICIPANT, true, "")) }
            }

            client.sendMessageAndAwaitUpdate(1, ClientBroadcast("Broadcast message")) {
                assertEquals(11, version)
                assertTrue {
                    log.contains(
                        LogEntry(
                            level = LogLevel.CLIENT_BROADCAST,
                            message = "Broadcast message",
                        ),
                    )
                }
            }
        } finally {
            session.close()
        }
    }
}

/**
 * A WebSocket client for testing.
 */
@ClientEndpoint
class TestWebSocketClient {
    val receivedUpdates: MutableList<RoomDto> = mutableListOf()
    val connectionFuture: CompletableFuture<Boolean> = CompletableFuture()
    var latch = CountDownLatch(1)
    private lateinit var session: Session

    @OnOpen
    @Suppress("UNUSED", "Sometimes, intellij can't cope")
    fun onOpen(session: Session) {
        connectionFuture.complete(true)
        this.session = session
    }

    @OnMessage
    @Suppress("UNUSED", "Sometimes, intellij can't cope")
    fun onMessage(message: String) {
        val dto: RoomDto = mapper.readValue(message)
        receivedUpdates.add(dto)
        latch.countDown()
    }

    fun sendMessageAndAwaitUpdate(
        numExpectedResponses: Int,
        request: UserRequest,
        action: RoomDto.() -> Unit,
    ) {
        val countBefore = receivedUpdates.size
        latch = CountDownLatch(numExpectedResponses)
        session.basicRemote.sendText(mapper.writeValueAsString(request))
        latch.await(5, SECONDS)
        val countAfter = receivedUpdates.size
        check(countBefore < countAfter) {
            "No update received"
        }
        action(receivedUpdates.last())
    }
}

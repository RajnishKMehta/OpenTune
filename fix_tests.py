import sys

test_path = 'app/src/test/kotlin/com/arturo254/opentune/together/TogetherGuestPlaybackPlannerTest.kt'
content = open(test_path).read()

old_test = '''    @Test
    fun planPlayTrackNow_addsAndSkips_whenTrackMissingAndAddAllowed() {
        val roomState =
            TogetherRoomState(
                sessionId = "sid",
                hostId = "hid",
                settings = TogetherRoomSettings(allowGuestsToControlPlayback = true, allowGuestsToAddTracks = true),
                queue = listOf(TogetherTrack(id = "a", title = "A")),
                currentIndex = 0,
                isPlaying = true,
            )
        val ops = TogetherGuestPlaybackPlanner.planPlayTrackNow(roomState, TogetherTrack(id = "b", title = "B"), 0L, true)
        assertEquals(
            listOf(
                TogetherGuestOp.AddTrack(TogetherTrack(id = "b", title = "B"), AddTrackMode.PLAY_NEXT),
                TogetherGuestOp.Control(ControlAction.SkipNext),
            ),
            ops,
        )
    }'''

new_test = '''    @Test
    fun planPlayTrackNow_addsAndSeeks_whenTrackMissingAndAddAllowed() {
        val roomState =
            TogetherRoomState(
                sessionId = "sid",
                hostId = "hid",
                settings = TogetherRoomSettings(allowGuestsToControlPlayback = true, allowGuestsToAddTracks = true),
                queue = listOf(TogetherTrack(id = "a", title = "A")),
                currentIndex = 0,
                isPlaying = true,
            )
        val ops = TogetherGuestPlaybackPlanner.planPlayTrackNow(roomState, TogetherTrack(id = "b", title = "B"), 0L, true)
        assertEquals(
            listOf(
                TogetherGuestOp.AddTrack(TogetherTrack(id = "b", title = "B"), AddTrackMode.PLAY_NEXT),
                TogetherGuestOp.Control(ControlAction.SeekToTrack(trackId = "b", positionMs = 0L)),
            ),
            ops,
        )
    }'''

content = content.replace(old_test, new_test)

with open(test_path, 'w') as f:
    f.write(content)

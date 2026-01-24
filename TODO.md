# In Progress

- [ ] Either prevent user to start game configuration until someone joined or handle the first move. Right now when creating a game, if user tries to set a configuration and publish, the following exception occur:
```agsl
 com.google.protobuf.UninitializedMessageException: Message missing required fields: game_configuration.white.id
   at com.google.protobuf.AbstractMessage$Builder.newUninitializedMessageException(AbstractMessage.java:466)
   at com.cauchymop.goblob.proto.PlayGameData$GameData$Builder.build(PlayGameData.java:2217)
   at com.cauchymop.goblob.model.GoGameController.commitConfiguration(GoGameController.kt:162)
```
- [ ] Check if change in GameRepository.fillLocalStates is good. There is still a problem with state not correctly refreshed in menu and lobby game.turn out of date/stale and the fix that fixes
  in game is breaking the fix (commented out) that was fixing the bug in configuration mode.
- [ ] Fix selection on start etc
- [ ] Turn change does not work as expected and seems out of sync between lobby and GameData
- [ ] State is not published correctly after creating a new game (stays on Please Wait instead of selecting game)
- [ ] When picking a game waiting for opponent, call joinGame etc
- [ ] Re-integrate Google Login from a menu and link it to LobbyClient

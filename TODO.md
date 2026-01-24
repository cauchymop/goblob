# In Progress

- [ ] Understand why when other player plays, I receive COMMAND_ADD_OR_UPDATE_GAME which triggers a addOrUpdateGame/onAddOrUpdateLobbyGame call but I don't receive the callbacks for messageForGame/onLobbyGameDataChanged callback as if playGame was not called
- [ ] Turn change does not work as expected and seems out of sync between lobby and GameData
- [ ] State is not published correctly after creating a new game (stays on Please Wait instead of selecting game)
- [ ] When picking a game waiting for opponent, call joinGame etc
- [ ] Re-integrate Google Login from a menu and link it to LobbyClient

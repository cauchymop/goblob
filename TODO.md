# In Progress
- [ ] Test game without handicap (probably a bug there where player2 stays stuck on Waiting Screen)
- [ ] Possibly rename Leave Game to "Resign" but we already have a resign to find the best way to address this. Probably having only one resign menu that does one or both depending on game phase.
- [ ] In GameExtensions use getName instead of .toString which means also getting the type correct for players
- [ ] Check if change in GameRepository.fillLocalStates is good. There is still a problem with state not correctly refreshed in menu and lobby game.turn out of date/stale and the fix that fixes
  in game is breaking the fix (commented out) that was fixing the bug in configuration mode.
- [ ] Fix selection on start etc
- [ ] Turn change does not work as expected and seems out of sync between lobby and GameData
- [ ] State is not published correctly after creating a new game (stays on Please Wait instead of selecting game)
- [ ] When picking a game waiting for opponent, call joinGame etc
- [ ] Re-integrate Google Login from a menu and link it to LobbyClient

# In Progress

- [ ] Fix this crash:
```
Process: com.cauchymop.goblob, PID: 24544
com.google.protobuf.UninitializedMessageException: Message missing required fields: game_configuration.white.id
	at com.google.protobuf.AbstractMessage$Builder.newUninitializedMessageException(AbstractMessage.java:466)
	at com.cauchymop.goblob.proto.PlayGameData$GameData$Builder.build(PlayGameData.java:2217)
	at com.cauchymop.goblob.model.GoGameController.commitConfiguration(GoGameController.kt:162)
	at com.cauchymop.goblob.presenter.ConfigurationViewEventProcessor.onConfigurationValidationEvent(ConfigurationViewEventProces
	at com.cauchymop.goblob.ui.GameConfigurationViewAndroid.fireConfigurationValidationEvent(GameConfigurationViewAndroid.java:14
	at com.cauchymop.goblob.ui.GameConfigurationViewAndroid.lambda$setConfigurationModel$1(GameConfigurationViewAndroid.java:105)
	at com.cauchymop.goblob.ui.GameConfigurationViewAndroid.$r8$lambda$EszTCdxivC1TAPxZVPElkaoxI2I(Unknown Source:0)
	at com.cauchymop.goblob.ui.GameConfigurationViewAndroid$$ExternalSyntheticLambda1.onClick(D8$$SyntheticClass:0)
	at android.view.View.performClick(View.java:8083)
	at android.view.View.performClickInternal(View.java:8060)
	at android.view.View.-$$Nest$mperformClickInternal(Unknown Source:0)
	at android.view.View$PerformClick.run(View.java:31549)
	at android.os.Handler.handleCallback(Handler.java:995)
	at android.os.Handler.dispatchMessage(Handler.java:103)
	at android.os.Looper.loopOnce(Looper.java:248)
	at android.os.Looper.loop(Looper.java:338)
	at android.app.ActivityThread.main(ActivityThread.java:9067)

                                                                                                    	at java.lang.reflect.Method.invoke(Native Method)
	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:593)
	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:932)                                                                                      	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:932)
```
- [ ] Turn change does not work as expected and seems out of sync between lobby and GameData
- [ ] State is not published correctly after creating a new game (stays on Please Wait instead of selecting game)
- [ ] When picking a game waiting for opponent, call joinGame etc
- [ ] Re-integrate Google Login from a menu and link it to LobbyClient

package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameType;

import org.junit.Test;

import dagger.Lazy;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests for {@link GameDatas}.
 */
public class GameDatasTest {
  private static GameDatas gameDatas = new GameDatas(new Lazy<String>() {
    @Override
    public String get() {
      return "Pipo";
    }
  }, "Bimbo", null);

  @Test
  public void testCreateLocalGame() {
    PlayGameData.GameData localGame = gameDatas.createLocalGame();
    assertThat(localGame.getGameConfiguration().getBoardSize()).isEqualTo(9);
    assertThat(localGame.getGameConfiguration().getGameType()).isEqualTo(GameType.LOCAL);
  }
}

package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameType;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests for {@link GameDatas}.
 */
public class GameDatasTest {
  private static GameDatas gameDatas = new GameDatas();

  @Test
  public void testCreateLocalGame() {
    PlayGameData.GoPlayer black = gameDatas.createGamePlayer("pipo", "player1", true);
    PlayGameData.GoPlayer white = gameDatas.createGamePlayer("bimbo", "player2", true);
    PlayGameData.GameData localGame = gameDatas.createNewGameData("pizza", GameType.LOCAL, black, white);
    assertThat(localGame.getGameConfiguration().getBoardSize()).isEqualTo(9);
    assertThat(localGame.getGameConfiguration().getGameType()).isEqualTo(GameType.LOCAL);
  }
}

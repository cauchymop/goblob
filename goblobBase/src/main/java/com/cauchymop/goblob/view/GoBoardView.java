package com.cauchymop.goblob.view;

import com.cauchymop.goblob.model.BoardViewModel;
import com.cauchymop.goblob.presenter.MovePlayedListener;

public interface GoBoardView {
  void setMovePlayedListener(MovePlayedListener movePlayedListener);
  void setBoard(BoardViewModel boardViewModel);
}

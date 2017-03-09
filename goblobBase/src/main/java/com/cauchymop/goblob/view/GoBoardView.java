package com.cauchymop.goblob.view;

import com.cauchymop.goblob.model.BoardViewModel;
import com.cauchymop.goblob.presenter.BoardEventListener;

public interface GoBoardView {
  void setBoardEventListener(BoardEventListener boardEventListener);
  void setBoard(BoardViewModel boardViewModel);
}

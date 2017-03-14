package com.cauchymop.goblob.view;

import com.cauchymop.goblob.model.BoardViewModel;

public interface GoBoardView {
  void setBoardEventListener(BoardEventListener boardEventListener);
  void setBoard(BoardViewModel boardViewModel);

  interface BoardEventListener {
    void onIntersectionSelected(int x, int y);
  }
}

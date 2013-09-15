package com.cauchymop.goblob.ui;

import android.util.Log;

import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.common.collect.Sets;
import com.google.common.primitives.UnsignedBytes;

import java.util.Set;

/**
 * A {@link RealTimeMessageReceivedListener} which encodes and sends messages to send, and decodes
 * and dispatches received messages.
 */
public class MessageManager implements RealTimeMessageReceivedListener {

  private static final String TAG = MessageManager.class.getName();
  private Set<MovePlayedListener> movePlayedListeners = Sets.newHashSet();
  private MainActivity mainActivity;

  public MessageManager(MainActivity mainActivity) {
    this.mainActivity = mainActivity;
  }

  @Override
  public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
    Log.d(TAG, "onRealTimeMessageReceived(" + realTimeMessage + ")");
    for (MovePlayedListener listener : movePlayedListeners) {
      listener.play(getMove(realTimeMessage));
    }
  }

  public void addMovePlayedListener(MovePlayedListener movePlayedListener) {
    movePlayedListeners.add(movePlayedListener);
  }

  private int getMove(RealTimeMessage realTimeMessage) {
    byte[] messageData = realTimeMessage.getMessageData();
    return UnsignedBytes.toInt(messageData[0]) * 256 + UnsignedBytes.toInt(messageData[1]);
  }

  public void sendMove(int move) {
    mainActivity.sendMessage(getMoveMessage(move));
  }

  private byte[] getMoveMessage(int move) {
    return new byte[] {UnsignedBytes.checkedCast(move / 256),
        UnsignedBytes.checkedCast(move % 256)};
  }

  public static interface MovePlayedListener {
    public void play(int move);
  }
}

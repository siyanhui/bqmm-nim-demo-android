package com.netease.nim.demo.chatroom.viewholder;

import android.graphics.Color;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.melink.bqmmsdk.widget.BQMMMessageText;
import com.netease.nim.demo.R;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.session.emoji.MoonUtil;
import com.netease.nim.uikit.session.module.input.InputPanel;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderText;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hzxuwen on 2016/1/18.
 */
public class ChatRoomViewHolderText extends MsgViewHolderText {
    @Override
    protected boolean isShowBubble() {
        return false;
    }

    @Override
    protected boolean isShowHeadImage() {
        return false;
    }

    @Override
    public void setNameTextView() {
        nameContainer.setPadding(ScreenUtil.dip2px(6), 0, 0, 0);
        ChatRoomViewHolderHelper.setNameTextView((ChatRoomMessage) message, nameTextView, nameIconView, context);
    }

    @Override
    protected void bindContentView() {
        BQMMMessageText bodyTextView = findViewById(com.netease.nim.uikit.R.id.nim_message_item_text_body);
        bodyTextView.setTextColor(Color.BLACK);
        layoutDirection();
        bodyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        bodyTextView.setOnLongClickListener(longClickListener);
        String msgType = "";
        JSONArray jsonArray = null;
        if (message.getRemoteExtension() != null && message.getRemoteExtension().containsKey(InputPanel.TXT_MSGTYPE) && message.getRemoteExtension().containsKey(InputPanel.MSG_DATA)) {
            HashMap<String, Object> hashMap = (HashMap<String, Object>) message.getRemoteExtension();
            msgType = (String) hashMap.get(InputPanel.TXT_MSGTYPE);
            ArrayList<String> arrayList = (ArrayList<String>) hashMap.get(InputPanel.MSG_DATA);
            jsonArray = new JSONArray(arrayList);
            if (TextUtils.equals(msgType, InputPanel.FACETYPE)) {
                bodyTextView.setStickerSize(view.getContext().getResources().getDimensionPixelOffset(R.dimen.bqmm_sticker_size));
                bodyTextView.setBackgroundResource(0);
            } else {
                bodyTextView.setEmojiSize(view.getContext().getResources().getDimensionPixelOffset(R.dimen.bqmm_emoji_size));
            }
        }
        bodyTextView.showMessage(getDisplayText(), msgType, jsonArray);
    }

    private void layoutDirection() {
        BQMMMessageText bodyTextView = findViewById(com.netease.nim.uikit.R.id.nim_message_item_text_body);
        bodyTextView.setPadding(ScreenUtil.dip2px(6), 0, 0, 0);
    }
}

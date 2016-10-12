package com.netease.nim.uikit.session.viewholder;

import android.graphics.Color;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;

import com.melink.baseframe.utils.DensityUtils;
import com.melink.bqmmsdk.widget.BQMMMessageText;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.session.module.input.InputPanel;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zhoujianghua on 2015/8/4.
 */
public class MsgViewHolderText extends MsgViewHolderBase {

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_text;
    }

    @Override
    protected void inflateContentView() {
    }

    @Override
    protected void bindContentView() {
        layoutDirection();

        BQMMMessageText bodyTextView = findViewById(R.id.nim_message_item_text_body);
        bodyTextView.setTextColor(isReceivedMessage() ? Color.BLACK : Color.WHITE);
        bodyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick();
            }
        });

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
                bodyTextView.setBackgroundResource(0);
            }
        }
        bodyTextView.showMessage(getDisplayText(), msgType, jsonArray);
    }

    private void layoutDirection() {
        BQMMMessageText bodyTextView = findViewById(R.id.nim_message_item_text_body);
        bodyTextView.setStickerSize(DensityUtils.dip2px(100));
        bodyTextView.setEmojiSize(DensityUtils.dip2px(20));
        if (isReceivedMessage()) {
            bodyTextView.setBackgroundResource(R.drawable.nim_message_item_left_selector);
            bodyTextView.setPadding(ScreenUtil.dip2px(15), ScreenUtil.dip2px(8), ScreenUtil.dip2px(10), ScreenUtil.dip2px(8));
        } else {
            bodyTextView.setBackgroundResource(R.drawable.nim_message_item_right_selector);
            bodyTextView.setPadding(ScreenUtil.dip2px(10), ScreenUtil.dip2px(8), ScreenUtil.dip2px(15), ScreenUtil.dip2px(8));
        }
    }

    @Override
    protected int leftBackground() {
        return 0;
    }

    @Override
    protected int rightBackground() {
        return 0;
    }

    protected String getDisplayText() {
        return message.getContent();
    }
}

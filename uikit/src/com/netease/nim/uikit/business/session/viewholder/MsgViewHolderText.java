package com.netease.nim.uikit.business.session.viewholder;

import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;

import com.melink.bqmmsdk.widget.BQMMMessageText;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.BQMMConstants;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Map;

/**
 * Created by zhoujianghua on 2015/8/4.
 */
public class MsgViewHolderText extends MsgViewHolderBase {

    /*
     * BQMM集成 更改类型为BQMMMessageText
     */
    protected BQMMMessageText bodyTextView;

    public MsgViewHolderText(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_text;
    }

    @Override
    protected void inflateContentView() {
        bodyTextView = findViewById(R.id.nim_message_item_text_body);
        /*
         * BQMM集成
         * BQMMMessageText并不是一个TextView，因此原先写在xml中的TextView相关属性是无法生效的。
         * 我们将原先那些属性移入了一个style中，并利用以下函数设置到View中去
         */
        bodyTextView.setTextTheme(R.style.bqmm_message_text_style);
        //另有一些属性需要调用函数设置
        bodyTextView.setMaxWidthDip(222);
        final Resources res = bodyTextView.getContext().getResources();
        bodyTextView.setEmojiSize(res.getDimensionPixelSize(R.dimen.bqmm_emoji_size));
        bodyTextView.setStickerSize(res.getDimensionPixelSize(R.dimen.bqmm_sticker_size));
    }

    @Override
    protected void bindContentView() {
        layoutDirection();
        bodyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick();
            }
        });
        /*
         * BQMM集成 含有表情的消息存储在RemoteExtension中，我们在这里对它进行解析和加载
         */
        String messageType = "";
        JSONArray messageData = null;
        JSONObject gifMessageData = null;
        final Map<String, Object> extension = message.getRemoteExtension();
        if (extension != null && extension.containsKey(BQMMConstants.BQMM_MSG_TYPE)) {
            messageType = (String) extension.get(BQMMConstants.BQMM_MSG_TYPE);
            if (TextUtils.equals(messageType, BQMMConstants.BQMM_MSG_TYPE_GIF)) {
                gifMessageData = new JSONObject((Map) extension.get(BQMMConstants.BQMM_MSG_DATA));
                bodyTextView.setBackgroundResource(0);
            } else {
                messageData = new JSONArray((Collection) extension.get(BQMMConstants.BQMM_MSG_DATA));
                if (TextUtils.equals(messageType, BQMMConstants.BQMM_MSG_TYPE_STICKER)) {
                    bodyTextView.setBackgroundResource(0);
                }
            }
        }
        boolean shownAsGif = false;
        if (gifMessageData != null) {
            try {
                bodyTextView.showBQMMGif(gifMessageData.getString("data_id"), gifMessageData.getString("sticker_url"), gifMessageData.optInt("w", 80), gifMessageData.optInt("h", 80), gifMessageData.optInt("is_gif", 0));
                shownAsGif = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (!shownAsGif) bodyTextView.showMessage(getDisplayText(), messageType, messageData);

        bodyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        bodyTextView.setOnLongClickListener(longClickListener);
    }

    private void layoutDirection() {
        if (isReceivedMessage()) {
            bodyTextView.setBackgroundResource(NimUIKitImpl.getOptions().messageLeftBackground);
            bodyTextView.setTextColor(Color.BLACK);
            bodyTextView.setPadding(ScreenUtil.dip2px(15), ScreenUtil.dip2px(8), ScreenUtil.dip2px(10), ScreenUtil.dip2px(8));
        } else {
            bodyTextView.setBackgroundResource(NimUIKitImpl.getOptions().messageRightBackground);
            bodyTextView.setTextColor(Color.WHITE);
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

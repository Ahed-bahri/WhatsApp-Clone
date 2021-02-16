package com.bahri.whatsappclone.interfaces;

import com.bahri.whatsappclone.model.chat.Chats;

import java.util.List;

public interface OnReadChatCallBack {

    void onReadSuccess(List<Chats> list);
    void onReadFailed();
}

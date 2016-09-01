package com.lorent.chat.message;

public class GroupChatExtType {
    public final static String IMG_TOKEN_START = "<img>";
    public final static String IMG_TOKEN_END = "</img>";
    public final static String FILE_TOKEN_START = "<file>";
    public final static String FILE_TOKEN_END = "</file>";
    private static final String tag = "GroupChatExtType";

    public enum ExtType {
        GroupChatNormal,
        GroupChatExtImage,
        GroupChatExtFile,
        GrouupChatExtVoice
    }

    private static String getExtContent(String body, String tokenStart, String tokenEnd) {

        int start = body.indexOf(tokenStart) + tokenStart.length();
        int end = body.indexOf(tokenEnd);

        if (start < 0 || end < 0)
            return null;

        //true body
        String str = body.substring(start, end);
        if (str == null || str.isEmpty())
            return null;
        else
            return str;
    }

    public static ExtType getExtType(String messageBody) {
        if (getExtContent(messageBody, IMG_TOKEN_START, IMG_TOKEN_END) != null)
            return ExtType.GroupChatExtImage;
        else if (getExtContent(messageBody, FILE_TOKEN_START, FILE_TOKEN_END) != null)
            return ExtType.GroupChatExtFile;

        return ExtType.GroupChatNormal;
    }
}

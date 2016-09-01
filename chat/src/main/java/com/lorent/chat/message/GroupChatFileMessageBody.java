package com.lorent.chat.message;

/*********
 * 
 * 群发文件消息Body格式如下:
 * 	  //通过http推送文件上服务器时，带以下参数
 *    <file>
 *    	<roomJid>201@wanke.yqpc</roomJid>
 *    	<fileName>XXXXX</fileName>
 *    	<uuid>XXXX</uuid>
 *    	<date>yyyy-MM-dd HH:mm:ss</date>
 *    	<uri>http://xxx.xxx.xxx/xxx/xxx.x</uri>
 *    </file>
 * 	
 * @author wuyaoquan
 *
 */

public class GroupChatFileMessageBody implements GroupChatExtMessageBody {

private static final String tag = "GroupChatFileMessageBody";
	/*	private final static String FILE_TOKEN_START = "<file>";
	private final static String FILE_TOKEN_END = "</file>";
*/	
	private String fileName;
	private String fileUuid;
	private String date;
	private String uri;
	
/*
 	GroupChatFileMessageBody gcfm = new GroupChatFileMessageBody("1.jpg",
			"312ACAEF93123",
			"2016-07-22 11:45:59",
			"http://cdn.duitang.com/uploads/item/201407/30/20140730125858_APsPN.thumb.700_0.jpeg");

	Log.e(tag, gcfm.getMessageBody());
*/	
	public GroupChatFileMessageBody()
	{
		
	}
	/**
	 * 
	 * @param fileName, 文件名
	 * @param fileUuid, 唯一uuid
	 * @param date,		上传时间
	 * @param uri,		下载地址
	 */
	public GroupChatFileMessageBody(String fileName, String fileUuid, String date, String uri)
	{
		this.fileName = fileName;
		this.fileUuid = fileUuid;
		this.date = date;
		this.uri = uri;
	}
	public void setFileName(String fileName){
		
	}
	
	public String getFileName()
	{
		return this.fileName;
	}
	
	public void setFileUuid(String fileUuid)
	{
		this.fileUuid = fileUuid;
	}
	
	public String getFileUuid()
	{
		return this.fileUuid;	
	}
	
	public void setDate(String date)
	{
		this.date = date;
	}
	
	public String getDate()
	{
		return this.date;
	}
	
	public void setUri(String uri)
	{
		this.uri = uri;
	}
	
	public String getUri()
	{
		return this.uri;
	}
	
	private String element(String name, String content)
	{
		String strEle = "";
		strEle = "<" + name + ">"
				+ content
				+ "</" + name + ">\r\n";
		return strEle;
	}
	
	private String content(String element, String name)
	{
		String tokeStart = "<" + name + ">";
		String tokenEnd = "</" + name + ">";
		int start = element.indexOf(tokeStart)+tokeStart.length();
		int end = element.indexOf(tokenEnd);

		//image true body
		String str = element.substring(start, end);
		if (str == null || str.isEmpty())
			return null;
				
		return str;		
	}
	
	@Override
	public boolean setMessageBody(String body) {
		// TODO Auto-generated method stub
		String tokeStart = GroupChatExtType.FILE_TOKEN_START;
		String tokenEnd = GroupChatExtType.FILE_TOKEN_END;
		int start = body.indexOf(tokeStart)+tokeStart.length();
		int end = body.indexOf(tokenEnd);
		//file true body

		String str = body.substring(start, end);
		if (str == null || str.isEmpty())
			return false;

		this.fileName = new String(content(str, "fileName"));
		this.fileUuid = new String(content(str, "uuid"));
		this.date = new String(content(str, "date"));
		this.uri = new String(content(str, "uri"));
		
		return true;
	}

	@Override
	public String getMessageBody() {
		// TODO Auto-generated method stub
		String body = null;
		if(fileName == null || fileName.isEmpty()
				|| fileUuid == null || fileUuid.isEmpty()
				|| date == null || date.isEmpty()
				|| uri == null || uri.isEmpty())
			
			return null;

		body = element("fileName", fileName)
			+ element("uuid", fileUuid)
			+ element("date", date)
			+ element("uri", uri);

		return element("file", body);
	}

}

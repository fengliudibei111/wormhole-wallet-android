package core.core;

import com.blankj.utilcode.util.LogUtils;

import org.json.me.JSONArray;

public class RequestQueueItem {
	private String method;
	private Object[] params;
	private int id;
	public RequestQueueItem(String method, Object[] params, int id) {
		this.method = method;
		this.params = params;
		this.id = id;
	}
	public String toJsonString() {
		StringBuffer string = new StringBuffer("{");
		string.append("\"id\": \""+id);
		string.append("\", \"method\": \""+method);
		JSONArray array = new JSONArray();
		for(int i = 0;i<params.length;i++) {
			array.put(params[i]);
		}
		string.append("\", \"params\": "+ array.toString());
		return string.append("}").toString();
	}
	public static void main(String[] args) {
		RequestQueueItem item = new RequestQueueItem("hello", new String[] {"hello"}, 1);
		LogUtils.d(item.toJsonString());
	}
	public Integer getId() {
		return new Integer(id);
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public Object[] getParams() {
		return params;
	}
	public void setParams(Object[] params) {
		this.params = params;
	}
	
}

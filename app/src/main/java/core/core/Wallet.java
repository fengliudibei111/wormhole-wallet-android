package core.core;

import org.json.me.JSONException;
import org.json.me.JSONObject;

import core.util.BigInteger;

public class Wallet {
 
	private Storage storage;
	private static int localBlockHeight;
	public Wallet (Storage storage){ 
		this.storage = storage;
	}
	  
	public String getXPRV() throws JSONException {
		JSONObject keystore = storage.get("keystore", new JSONObject());
		return keystore.getString("xprv");
	}
	
	public static BigInteger getLocalBlockHeight() {
//		return new BigInteger("1253090");
		  return new BigInteger(String.valueOf(localBlockHeight));
	}
	public static void setLocalBlockHeight(int blockHeight) {
		
		  localBlockHeight = blockHeight;
	}
	
	
} // end class
		
		
		 
 
 
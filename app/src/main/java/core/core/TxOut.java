package core.core;

import core.util.BigInteger;

public class TxOut {

 private String addr;
 private BigInteger amount;

	public String getRawScript() {
		return rawScript;
	}

	public void setRawScript(String rawScript) {
		this.rawScript = rawScript;
	}

	private String rawScript;
    
	public TxOut (String zaddr, BigInteger zamount)
	
	{ 
		addr=zaddr;
		amount=zamount;		
		
	}
	
	public void setAddr(String zaddr) {
		addr=zaddr;
	}
	
	public void setAmount(BigInteger zamount) {
		amount=zamount;
	}
	
	public BigInteger getAmount() {
		return amount;
	}
	
	public String getAddr() {
		return addr;
	}
	
	  
} // end class
		
		
		 
 
 
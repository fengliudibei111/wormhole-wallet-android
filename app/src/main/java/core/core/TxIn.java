package core.core;

import core.util.BigInteger;

public class TxIn {

 private String prevout_hash;
 private int prevout_n;
 private BigInteger satoshiAmount;
 private String script;
 private WalletAddress addr;
 private String signature;
    
	public TxIn (WalletAddress addr, String prevout_hash, int prevout_n, BigInteger amount)
	
	{ 
		 this.prevout_hash=prevout_hash;
		 this.prevout_n=prevout_n;
		 this.satoshiAmount=amount;
		 this.addr=addr;
		
	}
	
	public void setHash(String zhash) {
		prevout_hash=zhash;
	}
	
	public void setAmount(BigInteger zamount) {
		satoshiAmount=zamount;
	}

	public void setN(int n) {
		prevout_n=n;
	}
	
	public void setScript(String script) {
		this.script=script;
	}
	
	public void setAddr(WalletAddress addr) {
		this.addr=addr;
	}
	
	public void setSignature (String sig) {
		this.signature=sig;
	}
	
	public String getHash() {
		return prevout_hash;
	}
	 
	public WalletAddress getAddr() {
		return addr;
	}
	
	public BigInteger getAmount () {
		return satoshiAmount;
	}
	
	public int getN() {
		return prevout_n;
	}
	
	public String getScript() {
		return script;
	}
	
	public String getSignature() {
		return signature;
	}
	public String toString() {
		return "prevout:"+prevout_hash+",prevout_n: "+prevout_n+", amount: "+satoshiAmount+", walletAdd:"+addr;
	}
} // end class
		
		
		 
 
 
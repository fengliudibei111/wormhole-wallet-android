package core.core;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;

import java.io.IOException;
import java.util.Vector;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import core.java.util.Arrays;
import core.java.util.HashMap;
import core.java.util.Iterator;
import core.java.util.Map;
import core.util.BigInteger;
import core.util.StringUtils;
import core.util.Files;

public class BlockchainsUtil {
	public static final int CHECKPOINT = 0;
	private static Map blockchains;
	
	public static final BigInteger MAX_BITS = new BigInteger("1d00ffff",16);
	
	public static BigInteger bits_to_work(BigInteger bits) {
	    return (BigInteger.ONE.shiftLeft(256)).divide(bits_to_target(bits).add(BigInteger.ONE));
	}
	public static Map read_blockchain() {
		
		blockchains = new HashMap();
		blockchains.put(new Integer(CHECKPOINT), new Blockchain(CHECKPOINT,null));

		try {
			Files.getOrCreateDir(PathUtils.getInternalAppDataPath() + "/whc_wallet1/forks");
			Vector filterDirList = Files.listFilterDir(PathUtils.getInternalAppDataPath() + "/whc_wallet1/forks/", "fork_");
			//TODO- Sorting on filter dir
			for(int i=0;i < filterDirList.size();i++) {
				String[] arr = StringUtils.split(filterDirList.elementAt(i).toString(), "_");
				Integer checkpoint = Integer.valueOf(arr[2]);
				Integer	parent_id = Integer.valueOf(arr[1]);
			    blockchains.put(checkpoint,new Blockchain(checkpoint.intValue(), parent_id));
			}
			return blockchains;
		}
		catch(IOException ioE) {
			ioE.printStackTrace();
		}
		return null;
	}
	public static BigInteger bits_to_target(BigInteger bits) {
	    if(bits.equals(new BigInteger("0")))
	        return new BigInteger("0");
	    BigInteger size = bits.shiftRight(24);
	    //assert size.intValue() <= 0x1d;
	    BigInteger word = bits.and(new BigInteger("00ffffff",16));
	    //assert 0x8000 <= word.intValue() && word.longValue() <= 0x7fffff;
	    if(size.intValue() <= 3)
	        return word.shiftRight(8 * (3 - size.intValue()));
	    else
	        return word.shiftLeft(8 * (size.intValue() - 3));
	}
	
	
	public static BigInteger target_to_bits(BigInteger target) {
		if(target.equals(new BigInteger("0")))
	        return new BigInteger("0");
		
		target = target.min(bits_to_target(MAX_BITS));
		int size = (target.bitLength() + 7) / 8;
	    BigInteger mask64 = new BigInteger("ffffffffffffffff",16);
	    BigInteger compact;
	    if(size <= 3) {
	        compact = target.and(mask64).shiftLeft(8 * (3 - size));
	    }
	    else {
	    	BigInteger bi = target.shiftRight(8 * (size - 3));
	        compact = bi.and(mask64);
	    }
	    if(!compact.and(new BigInteger("00800000",16)).equals(new BigInteger("0"))) {
	        compact = compact.shiftRight(8);
	        size += 1;
	    }
	    //assert compact.equals(compact.and(new BigInteger("007fffff",16)));
	    //assert size < 256;
	    return compact.or(new BigInteger(String.valueOf(size)).shiftLeft(24));
	}
	
	public static String serializeHeader(JSONObject res) throws NumberFormatException, JSONException {
		return int_to_hex(res.optInt("version"), 4)
				+ revHex(res.get("prev_block_hash").toString())
				+ revHex(res.get("merkle_root").toString())
				+ int_to_hex(res.optInt("timestamp"), 4)
				+ int_to_hex(res.optInt("bits"), 4)
				+ int_to_hex(res.optString("nonce"), 4);
	}
	
	public static String hash_header(JSONObject header) throws JSONException {
		if(header == null) {
			return StringUtils.multiply("0", 64);
		}
		if(header.getString("prev_block_hash") == null)
			header.put("prev_block_hash", StringUtils.multiply("00", 32));
		String s = serializeHeader(header);
		return hashEncode(Hash(bfh(s)));
	}
	public static JSONObject deserializeHeader(byte[] h, int height) throws JSONException {
		JSONObject map = new JSONObject();
		map.put("version", hexToInt(Arrays.slice(h, 0, 4)));
		map.put("prev_block_hash", hashEncode(Arrays.slice(h, 4, 36)));
		map.put("merkle_root", hashEncode(Arrays.slice(h, 36, 68)));
		map.put("timestamp", hexToInt(Arrays.slice(h, 68, 72)));
		map.put("bits", hexToInt(Arrays.slice(h, 72, 76)));
		map.put("nonce", hexToIntNonce(Arrays.slice(h, 76, 80)));
		map.put("block_height", new Integer(height));
		return map;
	}
	public static int hexToInt(byte[] hex) {
		return Integer.parseInt(bh2u(Arrays.reverse(hex)),16);
	}

	public static BigInteger hexToIntNonce(byte[] hex) {
		return new BigInteger(bh2u(Arrays.reverse(hex)),16);
	}
	
	public static String hashEncode(byte[] b) {
		return bh2u(Arrays.reverse(b));
	}
	public static byte[] hashDecode(String hex) {
		return Arrays.reverse(bfh(hex));
	}
	public static String bh2u(byte[] b) {
		return Hex.toHexString(b);
	}
	public static byte[] bfh(String hex) {
		return Hex.decode(hex);
	}
	public static String int_to_hex(int i,int length) {
		String dec = Integer.toHexString(i);
		dec = StringUtils.multiply("0",(2 * length - dec.length())) +dec;
		return revHex(dec);
	}
	public static String int_to_hex(String i,int length) {
		String dec = new BigInteger(i).toString(16);
		dec = StringUtils.multiply("0",(2 * length - dec.length())) +dec;
		return revHex(dec);
	}
	public static String int_to_hex(BigInteger i,int length) {
		String dec = i.toString(16);
		dec = StringUtils.multiply("0",(2 * length - dec.length())) +dec;
		return revHex(dec);
	}
	public static String revHex(String hex) {
		return bh2u(Arrays.reverse(bfh(hex)));
	}
	
	public static byte[] Hash(byte[] xBytes) {
		// PERFORM DOUBLE SHA256 HASH
		SHA256Digest digest = new SHA256Digest();
		digest.update(xBytes, 0, xBytes.length);
		byte[] output = new byte[digest.getDigestSize()];
		digest.doFinal(output, 0);
		digest.update(output, 0, output.length);
		output = new byte[digest.getDigestSize()];
		digest.doFinal(output, 0);
		return output;
	}

	public static byte[] HashSingle(byte[] xBytes) {
		// PERFORM DOUBLE SHA256 HASH
		SHA256Digest digest = new SHA256Digest();
		digest.update(xBytes, 0, xBytes.length);
		byte[] output = new byte[32];
		digest.doFinal(output, 0);
		return output;
	}
	
	public static Blockchain check_header(JSONObject header) throws JSONException {
		if(header.length() == 0) {
			return null;
		}
		LogUtils.d("blockchain size "+blockchains.size());
		Iterator it = blockchains.values().iterator();
		while(it.hasNext()) {
			Blockchain blockchain = (Blockchain)it.next();
			if(blockchain.check_header(header)) {
				return blockchain;
			}
		}
		// TODO Auto-generated method stub
		return null;
	}
	public static Blockchain can_connect(JSONObject header) throws JSONException {
		if(header.length() == 0) {
			return null;
		}
		Iterator it = blockchains.values().iterator();
		while(it.hasNext()) {
			Blockchain blockchain = (Blockchain)it.next();
			if(blockchain.can_connect(header, true)) {
				return blockchain;
			}
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}

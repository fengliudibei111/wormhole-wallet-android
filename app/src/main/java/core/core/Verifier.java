package core.core;

import com.blankj.utilcode.util.LogUtils;

import org.bouncycastle.util.Arrays;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import core.java.util.HashMap;

public class Verifier {
	
	public HashMap merkle_roots = new HashMap();
	
	public VerifiedTx  verify_merkle(JSONObject r, Network network) throws JSONException {
//        if(r.has("error")){
//            LogUtils.d("get error "+r);
//            return null;
//        }
//        JSONArray params = r.getJSONArray("params");
//        JSONObject merkle = r.getJSONObject("result");
//        //Verify the hash of the server-provided merkle branch to a
//        //transaction matches the merkle root of its block
//        String tx_hash = params.getString(0);
//        int tx_height = merkle.getInt("block_height");
//        int pos = merkle.getInt("pos");
//        String merkle_root = hash_merkle_root(merkle.getJSONArray("merkle"), tx_hash, pos);
//        //HACK(qiaochuanbei)
////        JSONObject header = network.blockchain().read_header(tx_height);
////        if(header== null || !header.get("merkle_root").equals(merkle_root)) {
////            //FIXME: we should make a fresh connection to a server to
////            //recover from this, as this TX will now never verify
////            LogUtils.d("merkle verification failed for "+ tx_hash);
////            return null;
////        }
//        //we passed all the tests
//        merkle_roots.put(tx_hash, merkle_root);
//        return new VerifiedTx(tx_hash,tx_height,1234564754,pos);
////        return new VerifiedTx(tx_hash,tx_height,header.getInt("timestamp"),pos);


        if(r.has("error")){
            LogUtils.d("get error "+r);
            return null;
        }
        JSONArray params = r.getJSONArray("params");
        JSONObject merkle = r.getJSONObject("result");
        //Verify the hash of the server-provided merkle branch to a
        //transaction matches the merkle root of its block
        String tx_hash = params.getString(0);
        int tx_height = merkle.getInt("block_height");
        int pos = merkle.getInt("pos");
        String merkle_root = hash_merkle_root(merkle.getJSONArray("merkle"), tx_hash, pos);
        JSONObject header = network.blockchain().read_header(tx_height);
        if(header== null || !header.get("merkle_root").equals(merkle_root)) {
            //FIXME: we should make a fresh connection to a server to
            //recover from this, as this TX will now never verify
            LogUtils.d("merkle verification failed for "+ tx_hash);
            return null;
        }
        //we passed all the tests
        merkle_roots.put(tx_hash, merkle_root);
        return new VerifiedTx(tx_hash,tx_height,header.getInt("timestamp"),pos);

	}
	
	
	
    public String hash_merkle_root(JSONArray merkle_s,String target_hash,int pos) throws JSONException {
    	byte[] h = BlockchainsUtil.hashDecode(target_hash);
     	for(int i=0; i < merkle_s.length();i++){
            String item = merkle_s.getString(i);
            if (((pos >> i) & 1) !=0) {
            	h = BlockchainsUtil.Hash(Arrays.concatenate(BlockchainsUtil.hashDecode(item),h));
            }
            else {
            	h = BlockchainsUtil.Hash(Arrays.concatenate(h ,BlockchainsUtil.hashDecode(item)));
            }
        }
        return BlockchainsUtil.hashEncode(h);
    }
}

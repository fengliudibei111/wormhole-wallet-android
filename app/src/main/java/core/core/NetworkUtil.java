package core.core;

import com.blankj.utilcode.util.ResourceUtils;

import java.io.IOException;
import java.util.Enumeration;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import core.java.util.ArrayList;
import core.java.util.List;
import core.java.util.Set;
import core.util.BitcoinMainnet;
import core.util.Constants;
import core.util.Files;
import core.util.Server;
import core.util.StringUtils;

import java.util.Random;

public class NetworkUtil {
	public static Server deserialize_server(String server_str){
		String[] split = StringUtils.split(server_str,":");
		String host = split[0];
		int port = 0;
		String protocol = split[2];
		try {
			port = Integer.parseInt(split[1]);
			if(!(protocol.equals("s") || protocol.equals("t"))) {
				throw new Exception("Invalid protocol");
			}
		}
		catch(NumberFormatException nme) {
			throw new NumberFormatException();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return new Server(host, port, protocol);
	}
	
	public static JSONObject parse_servers(JSONArray result) throws JSONException {
		JSONObject servers = new JSONObject();
		for(int i=0;i< result.length();i++) {
			JSONObject out = new JSONObject();
			JSONArray array = result.getJSONArray(i);
			String host = array.getString(1);
			String pruning_level="-";
			String version="";
			if(array.length() > 2) {
				JSONArray child = array.getJSONArray(2);
				for(int j=0; j<child.length() ;j++) {
					String item = child.getString(j);
					if(item.startsWith("s") || item.startsWith("t")) {
						String protocol = item.substring(0, 1);
						String port = item.substring(1);
						if(port.trim().equals("")) {
							port = BitcoinMainnet.getDefaultPorts().get(protocol).toString();
						}
						out.put(protocol, port);
					}
					else if(item.startsWith("v")) {
						version = item.substring(1);
					}
					else if(item.startsWith("p")) {
						pruning_level= item.substring(1);
						if(pruning_level.trim().equals("")) {
							pruning_level = "0";
						}

					}
				}
			}
			out.put("pruning",pruning_level);
			out.put("version",version);
			servers.put(host, out);
		}
		return servers;
	}
	
	public static void init_header_file(Network network, Blockchain b) throws JSONException {
		if(b.get_hash(BlockchainsUtil.CHECKPOINT).equals(Constants.TEST_NET ? BitcoinMainnet.GENESIS_TESTNET : BitcoinMainnet.GENESIS)) {
			network.setDownloadingHeaders(false);
			return;
		}

		String filename = b.getPath();
		ResourceUtils.copyFileFromAssets("blockchain_headers", filename);
//		network.setDownloadingHeaders(true);
//		BitcoinHeadersDownload download = new BitcoinHeadersDownload(filename, network);
//		download.start();
	}
	
	public static Server pick_random_server(JSONObject default_servers, String protocol, Set exclude, String default_protocol) throws JSONException {
		if(default_servers == null) {
			default_servers = BitcoinMainnet.getDefaultServers();
		}

		List eligible = new ArrayList();
		Enumeration enumeration = default_servers.keys();
		while(enumeration.hasMoreElements()) {
			String server = (String)enumeration.nextElement();
			JSONObject item = default_servers.getJSONObject(server);
			if(item.has(protocol)) {
				int port = item.getInt(protocol);
				Server sObject = new Server(server, port, default_protocol);
				if(!exclude.contains(sObject)) {
					eligible.add(sObject);
				}
			}
		}

		return (Server)eligible.get(new Random().nextInt(eligible.size()));
	}
	
	public static JSONArray read_recent_servers() throws JSONException, IOException {
		if(!Files.isExist("recent-servers"))
			return new JSONArray();
		String json = new String(Files.read("recent-servers"));
		return new JSONArray(json);

	}
}

package player_sync_journeymap;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlayersyncJourneymapClient implements ClientModInitializer {
	private static int tick_counter = 0;
	public static final String MODID = "player_sync_journeymap";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static ServerCommunication server_communication;
	private static String IP_ADDRESS = "127.0.0.1";
	private static final int PORT = 8118;
	public static boolean SHOW_DEBUG_INFO = false;
	private static boolean SYNC_ENABLED = true;
	private static int ERROR_COUNT = 0;
	private static MinecraftClient client = MinecraftClient.getInstance();
	public static PlayersyncPlugin playersyncPlugin;
	public static List<JSONObject> PLAYER_LIST;
	@Override
	public void onInitializeClient() {
		server_communication = new ServerCommunication();
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientTickEvents.END_WORLD_TICK.register((ClientWorld world)->{
			if(tick_counter == 19) {
				tick_counter = 0;
				handle_connection(world);
			}else{
				tick_counter++;
			}
		});

		// This is the command handling system
		ClientSendMessageEvents.ALLOW_CHAT.register((String message) -> {
			if(message.startsWith("%")){
				switch (message){
					case "%clear":
						try {
							server_communication.startConnection(IP_ADDRESS, PORT);
							server_communication.sendMessage("{clear}\n");
							client.player.sendMessage(Text.literal("§b[§dPlayerSync§b] §fPlayer list reset!"));
						} catch (IOException e) {
							LOGGER.error("Failed to send message to server: "+e);
							client.player.sendMessage(Text.literal("§b[§dPlayerSync§b] §fFailed to reset player list"));
						}
						break;
					case "%help":
						client.player.sendMessage(Text.literal("§b[§dPlayerSync§b] §fHere is a list of all commands:"));
						client.player.sendMessage(Text.literal("§a%help §f- Display this message"));
						client.player.sendMessage(Text.literal("§a%clear §f- Clear the server from old player"));
						client.player.sendMessage(Text.literal("§a%debug on §f- Activate the debug channel"));
						client.player.sendMessage(Text.literal("§a%debug off §f- Hide the debug channel"));
						client.player.sendMessage(Text.literal("§a%ip §f- Show the current sync-server ip"));
						client.player.sendMessage(Text.literal("§a%ip [ip or address] §f- set the current sync-server ip"));
						client.player.sendMessage(Text.literal("§a%sync on §f- Activate the Player-sync"));
						client.player.sendMessage(Text.literal("§a%sync off §f- Disable the Player-sync"));
						break;
					case "%debug on":
						client.player.sendMessage(Text.literal("§b[§dPlayerSync§b] §fActivating the debug channel"));
						SHOW_DEBUG_INFO = true;
						break;
					case "%debug off":
						client.player.sendMessage(Text.literal("§b[§dPlayerSync§b] §fHiding the debug channel"));
						SHOW_DEBUG_INFO = false;
						break;
					case "%sync on":
						client.player.sendMessage(Text.literal("§b[§dPlayerSync§b] §fActivating the Player-sync"));
						SYNC_ENABLED = true;
						break;
					case "%sync off":
						client.player.sendMessage(Text.literal("§b[§dPlayerSync§b] §fDisabling the Player-sync"));
						SYNC_ENABLED = false;
						break;
					default:
						if(message.startsWith("%ip")){
							if(message.length()<5){
								client.player.sendMessage(Text.literal("§b[§dPlayerSync§b] §fThe sync server ip is: §d"+IP_ADDRESS));
							}else {
								IP_ADDRESS = message.replace("%ip ", "");
								client.player.sendMessage(Text.literal("§b[§dPlayerSync§b] §fThe sync server ip was set to: §d"+IP_ADDRESS));
								SYNC_ENABLED =true;
							}
						}else{
							client.player.sendMessage(Text.literal("§b[§dPlayerSync§b] §cInvalid command, use %help to see a list of all commands"));
						}
				}
				return false;
			}else{
				return true;
			}
		});
	}

	private void handle_connection(ClientWorld world){
		if(!SYNC_ENABLED){return;}
		LOGGER.info("hello there!");
		try {
			server_communication.startConnection(IP_ADDRESS, PORT);
		} catch (IOException e) {
			LOGGER.error("Failed to establish connection: "+e);
			ERROR_COUNT++;
			if(ERROR_COUNT>5){
				SYNC_ENABLED=false;
				client.player.sendMessage(Text.literal("§b[§dPlayerSync§b]§f Could not connect to server disabling Player-Sync, use the §d%sync on§f command to turn sync on again"));
			}
			return;
		}
		double player_x = client.player.getX();
		double player_z = client.player.getZ();
		JSONObject message = new JSONObject();
		message.put("name", client.player.getName().getString());
		message.put("x",player_x);
		message.put("z",player_z);
		message.put("dimension",world.getRegistryKey().getValue());
		try {
			String response = server_communication.sendMessage(message.toString()+"\n");
			if(SHOW_DEBUG_INFO == true){
				client.player.sendMessage(Text.literal(response));
			}
			JSONArray player_array = new JSONObject(response).getJSONArray("player_list");
			PLAYER_LIST = new ArrayList<JSONObject>();
			for (int i = 0, size = player_array.length(); i < size; i++){
				PLAYER_LIST.add(player_array.getJSONObject(i));
			}
			playersyncPlugin = PlayersyncPlugin.getInstance();
			playersyncPlugin.markerFactory.update_map(playersyncPlugin.jmAPI, PLAYER_LIST);
		} catch (IOException e) {
			LOGGER.error("Failed to send message to server: "+e);
		}
	}
}
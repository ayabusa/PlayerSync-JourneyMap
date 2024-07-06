package player_sync_journeymap;

import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.MarkerOverlay;
import journeymap.client.api.model.MapImage;
import journeymap.client.api.model.TextProperties;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.Logger;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.level.*;
import org.json.JSONObject;

import java.util.List;

public class MarkerFactory {
    List<MarkerOverlay> player_marker_list;
    public void create_sprite(IClientAPI jmAPI, BlockPos pos){
        Identifier steve_image = new Identifier("player_sync_journeymap:big-steve-face.png");
        int iconSize = 600;
        MapImage icon = new MapImage(steve_image, iconSize, iconSize).setDisplayHeight(20).setDisplayWidth(20);
        icon.centerAnchors();
        MarkerOverlay markerOverlay = new MarkerOverlay(PlayersyncJourneymapClient.MODID, "my_first_marker", pos, icon);
        markerOverlay.setDimension(World.OVERWORLD)
                .setTitle("Marker Overlay Wow")
                .setLabel("Salut les zebis")
                .setTextProperties(new TextProperties().setOffsetY(30));
        try
        {
            jmAPI.show(markerOverlay);
        }
        catch (Exception e)
        {
            PlayersyncJourneymapClient.LOGGER.error("Can't add marker overlay", e);
        }
    }
    public void update_map(IClientAPI jmAPI, List<JSONObject> player_list){
        jmAPI.removeAll(PlayersyncJourneymapClient.MODID, DisplayType.Marker);
        for(int i = 0, size = player_list.size(); i < size; i++){
            JSONObject player = player_list.get(i);
            BlockPos pos = new BlockPos(((Double)player.get("x")).intValue(), 0, ((Double)player.get("z")).intValue());

            Identifier steve_image = new Identifier("player_sync_journeymap:big-steve-face.png");
            int iconSize = 600;
            MapImage icon = new MapImage(steve_image, iconSize, iconSize).setDisplayHeight(20).setDisplayWidth(20);
            icon.centerAnchors();
            MarkerOverlay markerOverlay = new MarkerOverlay(PlayersyncJourneymapClient.MODID, "marker_"+player.get("name"), pos, icon);
            markerOverlay.setDimension(World.OVERWORLD)
                    .setTitle("Synced player")
                    .setLabel((String) player.get("name"))
                    .setTextProperties(new TextProperties().setOffsetY(30));
            try
            {
                jmAPI.show(markerOverlay);
            }
            catch (Exception e)
            {
                PlayersyncJourneymapClient.LOGGER.error("Can't add marker overlay", e);
            }
        }
    }
}

package player_sync_journeymap;

import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.event.ClientEvent;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

import static journeymap.client.api.event.ClientEvent.Type.*;

public class PlayersyncPlugin implements IClientPlugin {

    public IClientAPI jmAPI;
    public MarkerFactory markerFactory = new MarkerFactory();;

    private static PlayersyncPlugin INSTANCE;

    public PlayersyncPlugin(){
        INSTANCE = this;
    }

    public static PlayersyncPlugin getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void initialize(final IClientAPI jmClientApi) {
        this.jmAPI = jmClientApi;
        PlayersyncJourneymapClient.LOGGER.info("Plugin connected!");
        if (this.jmAPI == null){
            PlayersyncJourneymapClient.LOGGER.error("jmAPI is null");
        }
        //this.jmAPI.subscribe(getModId(), EnumSet.of(MAPPING_STARTED, MAP_CLICKED));
    }

    @Override
    public String getModId() {
        return PlayersyncJourneymapClient.MODID;
    }

    @Override
    public void onEvent(ClientEvent event) {

    }

    /*private void onMappingStarted(ClientEvent event){
        PlayersyncJourneymapClient.LOGGER.info("Started Mapping");
        BlockPos pos = new BlockPos(5,5,5);
        markerFactory = new MarkerFactory();
        markerFactory.create_sprite(this.jmAPI, pos);
    }*/
}

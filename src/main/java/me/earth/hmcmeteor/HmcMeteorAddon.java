package me.earth.hmcmeteor;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import org.slf4j.Logger;

public class HmcMeteorAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        // TODO: I think this is a bit frail, the only reason the specifics come first is because h comes before m?
        LOG.info("Initializing HMC-Meteor Addon");
        try {
            HmcMeteorAddonInitializer.initialize();
        } catch (Throwable throwable) {
            LOG.error("Failed to initialize.", throwable);
        }
    }

    @Override
    public String getPackage() {
        return "me.earth.hmcmeteor";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("3arthqu4ke", "hmc-meteor");
    }

}

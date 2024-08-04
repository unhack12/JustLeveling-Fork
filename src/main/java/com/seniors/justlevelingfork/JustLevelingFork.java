package com.seniors.justlevelingfork;

import com.mojang.logging.LogUtils;
import com.seniors.justlevelingfork.config.EAptitude;
import com.seniors.justlevelingfork.config.LockItem;
import com.seniors.justlevelingfork.handler.*;
import com.seniors.justlevelingfork.network.ServerNetworking;
import com.seniors.justlevelingfork.registry.*;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mod(JustLevelingFork.MOD_ID)
public class JustLevelingFork {
    public static final String MOD_ID = "justlevelingfork";
    public static final String MOD_NAME = "just_leveling_fork";

    private static final Logger LOGGER = LogUtils.getLogger();

    public static Logger getLOGGER() {
        return LOGGER;
    }

    public JustLevelingFork() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::attributeSetup);

        RegistryItems.load(eventBus);
        RegistryAptitudes.load(eventBus);
        RegistryPassives.load(eventBus);
        RegistrySkills.load(eventBus);
        RegistryTitles.load(eventBus);
        RegistryAttributes.load(eventBus);
        RegistrySounds.load(eventBus);
        RegistryArguments.load(eventBus);

        HandlerCommonConfig.HANDLER.load();
        HandlerLockItemsConfig.HANDLER.load();
        if (!HandlerCommonConfig.HANDLER.instance().usingNewConfig){
            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HandlerConfigCommon.SPEC, "just_leveling-common.toml");
        } else { // To avoid issues with players changing values on the old config file, let's delete the old file.
            File oldConfigFile = FMLPaths.CONFIGDIR.get().resolve("just_leveling-common.toml").toFile();
            if (oldConfigFile.exists()){
                oldConfigFile.delete();
            }
        }
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, HandlerConfigClient.SPEC, "just_leveling-client.toml");
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new RegistryCommonEvents());
        if (HandlerCurios.isModLoaded())
            MinecraftForge.EVENT_BUS.register(new HandlerCurios());
        ServerNetworking.init();
    }

    private void attributeSetup(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            event.add(type, RegistryAttributes.CRITICAL_DAMAGE.get());
            event.add(type, RegistryAttributes.MAGIC_RESIST.get());
            event.add(type, RegistryAttributes.BREAK_SPEED.get());
            event.add(type, RegistryAttributes.PROJECTILE_DAMAGE.get());
            event.add(type, RegistryAttributes.BENEFICIAL_EFFECT.get());
        }
    }

    @SubscribeEvent
    public void onServerStarting(final ServerStartingEvent event){ // Let's migrate the config on server start to it runs on client and server.
        File oldConfigFile = FMLPaths.CONFIGDIR.get().resolve("just_leveling-common.toml").toFile();

        if (!HandlerCommonConfig.HANDLER.instance().usingNewConfig && oldConfigFile.exists()) {
            JustLevelingFork.getLOGGER().info("Configuration not migrated yet, starting migration...");
            JustLevelingFork.migrateOldConfig();
        }
        else if (!HandlerCommonConfig.HANDLER.instance().usingNewConfig && !oldConfigFile.exists()){
            HandlerCommonConfig.HANDLER.instance().usingNewConfig = true;
            HandlerCommonConfig.HANDLER.save();
        }
    }

    static void migrateOldConfig() {
        List<? extends String> configList = HandlerConfigCommon.lockItemList.get();

        List<LockItem> items = new ArrayList<>();
        for (String value : configList) {
            String[] values = value.split("#");
            if (values.length != 2) {
                continue;
            }
            LockItem lockItem = new LockItem(values[0]);

            String getResource = values[0];
            if (getResource.split(":").length != 2) {
                continue;
            }
            String aptitudeValue = values[1];
            String getAptitude = aptitudeValue.contains("<droppable>") ? aptitudeValue.split("<droppable>")[0] : aptitudeValue;
            String[] aptitudeList = getAptitude.split(";");

            List<LockItem.Aptitude> aptitudes = new ArrayList<>();

            for (String getMultipleSkill : aptitudeList) {
                if (getMultipleSkill.isEmpty()) {
                    continue;
                }

                if (getMultipleSkill.contains("#") || getMultipleSkill.contains(",")) {
                    continue;
                }

                String[] aptitudeValues = getMultipleSkill.split(":");

                String aptitudePath = aptitudeValues[0];
                if (aptitudePath.equals("defence")) {
                    aptitudePath = "defense";
                }
                Aptitude aptitudeName = RegistryAptitudes.getAptitude(aptitudePath);
                if (aptitudeName == null) {
                    continue;
                }

                LockItem.Aptitude aptitude = new LockItem.Aptitude();
                aptitude.Aptitude = EAptitude.valueOf(WordUtils.capitalizeFully(aptitudePath));
                aptitude.Level = Integer.parseInt(aptitudeValues[1]);

                aptitudes.add(aptitude);
            }
            if (aptitudes.isEmpty()) {
                continue;
            }

            lockItem.Aptitudes = aptitudes;
            items.add(lockItem);
        }

        items.forEach((item) -> {
            if (HandlerLockItemsConfig.HANDLER.instance().lockItemList.stream().noneMatch((lockItem -> lockItem.Item.equalsIgnoreCase(item.Item)))){
                HandlerLockItemsConfig.HANDLER.instance().lockItemList.add(item);
            }
        });

        HandlerLockItemsConfig.HANDLER.save();
        HandlerCommonConfig.HANDLER.instance().usingNewConfig = true;
        HandlerCommonConfig.HANDLER.save();
    }
}

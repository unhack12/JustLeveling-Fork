package com.seniors.justlevelingfork.registry.aptitude;

import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.handler.HandlerConfigCommon;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class Aptitude {
    public final int index;
    public final ResourceLocation key;
    public final ResourceLocation[] lockedTexture;
    public final ResourceLocation background;
    public List<Skill> list = new ArrayList<>();

    public Aptitude(int index, ResourceLocation key, ResourceLocation[] lockedTexture, ResourceLocation background) {
        this.index = index;
        this.key = key;
        this.lockedTexture = lockedTexture;
        this.background = background;
    }

    public Aptitude get() {
        return this;
    }

    public String getName() {
        return this.key.getPath();
    }

    public String getKey() {
        return "aptitude." + this.key.toLanguageKey();
    }

    public String getDescription() {
        return getKey() + ".description";
    }

    public void setList(List<Skill> list) {
        this.list = list;
    }

    public List<Skill> getSkills(Aptitude aptitude) {
        List<Skill> list = new ArrayList<>();
        for (int i = 0; i < RegistrySkills.SKILLS_REGISTRY.get().getValues().stream().toList().size(); i++) {
            Skill skill = RegistrySkills.SKILLS_REGISTRY.get().getValues().stream().toList().get(i);
            if (skill.aptitude == aptitude) list.add(skill);
        }
        return list;
    }

    public List<Passive> getPassives(Aptitude aptitude) {
        List<Passive> list = new ArrayList<>();
        for (int i = 0; i < RegistryPassives.PASSIVES_REGISTRY.get().getValues().stream().toList().size(); i++) {
            Passive passive = RegistryPassives.PASSIVES_REGISTRY.get().getValues().stream().toList().get(i);
            if (passive.aptitude == aptitude) list.add(passive);
        }
        return list;
    }


    public int getLevel() {
        return AptitudeCapability.get().getAptitudeLevel(this);
    }

    public int getLevel(Player player) {
        return AptitudeCapability.get(player).getAptitudeLevel(this);
    }

    public MutableComponent getRank(int aptitudeLevel) {
        MutableComponent rank = Component.translatable("aptitude.justlevelingfork.rank.0");
        for (int i = 0; i < 9; i++) {
            if (aptitudeLevel >= (HandlerConfigCommon.aptitudeMaxLevel.get() / 8) * i) {
                rank = Component.translatable("aptitude.justlevelingfork.rank." + i);
            }
        }
        return rank;
    }

    public ResourceLocation getLockedTexture(int fromLevel) {
        int index = 0;
        int size = HandlerConfigCommon.aptitudeMaxLevel.get();
        int textureListSize = this.lockedTexture.length;
        for (int i = 0; i < size; i++) {
            if (i <= fromLevel) {
                index = (i - i % size / textureListSize) / size / textureListSize;
            }
        }
        return this.lockedTexture[index];
    }

    public ResourceLocation getLockedTexture() {
        int index = 0;
        int size = HandlerConfigCommon.aptitudeMaxLevel.get();
        int textureListSize = this.lockedTexture.length;
        for (int i = 0; i < size; i++) {
            if (i <= getLevel()) {
                index = (i - i % size / textureListSize) / size / textureListSize;
            }
        }
        return this.lockedTexture[index];
    }
}


package de.m_marvin.industria.core.contraptions.engine.types;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.valkyrienskies.core.api.ships.ServerShip;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.contraptions.engine.ContraptionAttachment;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class ServerContraption extends Contraption {
	
	protected ServerShip ship;
	protected MinecraftServer server;
	
	public ServerContraption(MinecraftServer server, ServerShip ship) {
		this.ship = ship;
		this.server = server;
	}
	
	@Override
	public ServerShip getShip() {
		return this.ship;
	}
	
	@Override
	public ServerLevel getLevel() {
		return this.server.getLevel(ResourceKey.create(Registries.DIMENSION, this.getPosition().getDimension()));
	}
	
	public void setTags(Set<String> tags) {
		getHandler().getContraptionTags().put(getId(), tags);
	}
	
	public void addTag(String tag) {
		getTags().add(tag);
	}
	
	public void removeTag(String tag) {
		getTags().remove(tag);
	}
	
	public void setName(String name) {
		getShip().setSlug(name);
	}
	
	public <T extends ContraptionAttachment> T getAttachment(Class<T> attachmentClass) {
		return getShip().getAttachment(attachmentClass);
	}
	
	public <T extends ContraptionAttachment> void removeAttachment(Class<T> attachmentClass) {
		getShip().saveAttachment(attachmentClass, null);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ContraptionAttachment> void setAttachment(T attachment) {
		attachment.setContrapion(this);
		getShip().saveAttachment((Class<T>) attachment.getClass(), attachment);
	}
	
	public <T extends ContraptionAttachment> T getOrCreateAttachment(Class<T> attachmentClass) {
		T attachment = getAttachment(attachmentClass);
		if (attachment == null) {
			try {
				attachment = attachmentClass.getConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				IndustriaCore.LOGGER.warn("could not instantiate contraption attachment of type %s", attachmentClass.getSimpleName(), e);
				return null;
			}
			setAttachment(attachment);
		}
		return attachment;
	}

	public double getMass() {
		return getShip().getInertiaData().getMass();
	}

	public boolean isStatic() {
		return getShip().isStatic();
	}

	public void setStatic(boolean isStatic) {
		getShip().setStatic(isStatic);
	}

	public void setNameStr(String name) {
		getShip().setSlug(name);
	}

	public void getTags(String tag) {
		Set<String> tags = getHandler().getContraptionTags().get(getId());
		if (tags == null) {
			tags = new HashSet<>();
			getHandler().getContraptionTags().put(getId(), tags);
		}
		tags.add(tag);
	}

}

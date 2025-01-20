package de.m_marvin.industria.core.client.electrics.events;

import de.m_marvin.industria.core.electrics.engine.ElectricNetwork;
import de.m_marvin.industria.core.util.types.PowerNetState;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

public class ElectricNetworkEvent extends Event {

	private final Level level;
    private final ElectricNetwork network;
    
    public ElectricNetworkEvent(Level level, ElectricNetwork network) {
    	this.level = level;
    	this.network = network;
	}
    
    public Level getLevel() {
		return level;
	}
    
    public ElectricNetwork getNetwork() {
		return network;
	}
    
    public static class FuseTripedEvent extends ElectricNetworkEvent {
    	
		public FuseTripedEvent(Level level, ElectricNetwork network) {
			super(level, network);
		}
		
    }
    
    public static class StateChangeEvent extends ElectricNetworkEvent {

    	private final PowerNetState state;
    	
		public StateChangeEvent(Level level, ElectricNetwork network, PowerNetState newState) {
			super(level, network);
			this.state = newState;
		}
		
		public PowerNetState getNewState() {
			return state;
		}
    	
    }
    
}

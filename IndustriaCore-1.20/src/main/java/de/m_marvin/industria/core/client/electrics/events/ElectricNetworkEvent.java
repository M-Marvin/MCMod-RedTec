package de.m_marvin.industria.core.client.electrics.events;

import de.m_marvin.industria.core.electrics.engine.ElectricNetwork;
import net.minecraftforge.eventbus.api.Event;

public class ElectricNetworkEvent extends Event {

    private final ElectricNetwork network;
    
    public ElectricNetworkEvent(ElectricNetwork network) {
    	this.network = network;
	}
    
    public ElectricNetwork getNetwork() {
		return network;
	}
    
    public static class FuseTripedEvent extends ElectricNetworkEvent {
    	
		public FuseTripedEvent(ElectricNetwork network) {
			super(network);
		}
		
    }
    
    public static class StateChangeEvent extends ElectricNetworkEvent {

    	private final ElectricNetwork.State state;
    	
		public StateChangeEvent(ElectricNetwork network, ElectricNetwork.State newState) {
			super(network);
			this.state = newState;
		}
		
		public ElectricNetwork.State getNewState() {
			return state;
		}
    	
    }
    
}

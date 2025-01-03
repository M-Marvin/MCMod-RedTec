package de.m_marvin.industria.core.kinetics.types.blockentities;

public interface IKineticBlockEntity {
	
	public void setRPM(int rpm);
	public int getRPM();
	
	public default int getTorque() {
		return 0;
	}
	
}
 
package de.budschie.bmorph.morph.functionality.data_transformers;

import de.budschie.bmorph.network.DataTransformerSynchronizer.DataTransfomerSynchronizerPacket;
import de.budschie.bmorph.util.DynamicRegistry;

public class DynamicDataTransformerRegistry extends DynamicRegistry<DataTransformer, DataTransfomerSynchronizerPacket>
{
	@Override
	public DataTransfomerSynchronizerPacket getPacket()
	{
		return new DataTransfomerSynchronizerPacket(entries.values());
	}
}

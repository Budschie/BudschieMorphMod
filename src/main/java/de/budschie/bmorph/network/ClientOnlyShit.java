package de.budschie.bmorph.network;

import java.util.UUID;

import de.budschie.bmorph.render_handler.RenderHandler;

public class ClientOnlyShit
{	
	public static void disposePlayerMorphData(UUID playerToDispose)
	{
		RenderHandler.disposePlayerMorphData(playerToDispose);
	}
}

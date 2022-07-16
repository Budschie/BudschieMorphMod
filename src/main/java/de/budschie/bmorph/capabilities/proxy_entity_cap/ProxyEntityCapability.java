package de.budschie.bmorph.capabilities.proxy_entity_cap;

public class ProxyEntityCapability implements IProxyEntityCapability
{
	private boolean proxy = false;
	
	@Override
	public boolean isProxyEntity()
	{
		return proxy;
	}

	@Override
	public void setProxyEntity(boolean value)
	{
		this.proxy = value;
	}
}

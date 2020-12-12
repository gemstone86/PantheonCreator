package cosmos;

import gods.*;

public abstract class place {


	public abstract String getName();
	
	public abstract void setName();
	
	public abstract place addConnection(place newConnection);
	
	public abstract void removeConnection(int index);
	
	public abstract place getConnections();
	
	public abstract void setOwner(deity deity);
	
	public abstract void removeOwner();
	
}

package team_orange.intaxi;

/**
 * Created by Martin on 16.5.2017.
 */

public class Request {

    public int Id;
    public double CustomerLatitude;
    public double CustomerLongitude;
    public int TravellersCount;
    public String DestinationLocation;
    public String OtherRequests;

    public Request(int id,double CustomerLatitude, double CustomerLongitude, int TravellersCount, String DestinationLocation,
                   String OtherRequests){
        this.Id=id;
        this.CustomerLatitude=CustomerLatitude;
        this.CustomerLongitude=CustomerLongitude;
        this.TravellersCount=TravellersCount;
        this.DestinationLocation=DestinationLocation;
        this.OtherRequests=OtherRequests;
    }

}

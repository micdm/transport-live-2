package micdm.transportlive2.data.loaders.remote;

public class GetStationResponse {

    public final String PathwayMilestoneId;
    public final String LegalName;
    public final String Description;
    public final double Latitude;
    public final double Longitude;

    GetStationResponse(String pathwayMilestoneId, String legalName, String description, double latitude, double longitude) {
        PathwayMilestoneId = pathwayMilestoneId;
        LegalName = legalName;
        Description = description;
        Latitude = latitude;
        Longitude = longitude;
    }
}

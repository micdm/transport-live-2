package micdm.transportlive2.data.loaders.remote;

public class GetRoutesResponse {

    public static class Group {

        public final String PathwayGroupId;
        public final String Name;

        Group(String pathwayGroupId, String name) {
            PathwayGroupId = pathwayGroupId;
            Name = name;
        }
    }

    public final String PathwayId;
    public final String ItineraryFrom;
    public final String ItineraryTo;
    public final String Number;
    public final Group PathwayGroup;

    GetRoutesResponse(String pathwayId, String itineraryFrom, String itineraryTo, String number, Group pathwayGroup) {
        PathwayId = pathwayId;
        ItineraryFrom = itineraryFrom;
        ItineraryTo = itineraryTo;
        Number = number;
        PathwayGroup = pathwayGroup;
    }
}

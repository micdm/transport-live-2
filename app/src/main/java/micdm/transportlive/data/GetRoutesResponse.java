package micdm.transportlive.data;

class GetRoutesResponse {

    static class Group {

        final String PathwayGroupId;
        final String Name;

        Group(String pathwayGroupId, String name) {
            PathwayGroupId = pathwayGroupId;
            Name = name;
        }
    }

    final String PathwayId;
    final String ItineraryFrom;
    final String ItineraryTo;
    final String Number;
    final Group PathwayGroup;

    GetRoutesResponse(String pathwayId, String itineraryFrom, String itineraryTo, String number, Group pathwayGroup) {
        PathwayId = pathwayId;
        ItineraryFrom = itineraryFrom;
        ItineraryTo = itineraryTo;
        Number = number;
        PathwayGroup = pathwayGroup;
    }
}

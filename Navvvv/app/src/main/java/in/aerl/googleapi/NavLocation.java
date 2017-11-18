package in.aerl.googleapi;

public class NavLocation {
    public Integer Nav_Id;
    public Double Start_Lat;
    public Double Start_Lng;
    public Double End_Lat;
    public Double End_Lng;
    public String html_instructions;


    public NavLocation(Integer Nav_Id, Double Start_Lat, Double Start_Lng, Double End_Lat, Double End_Lng, String html_instructions ) {
        this.Nav_Id = Nav_Id;
        this.Start_Lat = Start_Lat;
        this.Start_Lng = Start_Lng;
        this.End_Lat = End_Lat;
        this.End_Lng = End_Lng;
        this.html_instructions = html_instructions;
    }

}
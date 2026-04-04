package com.example.ebus.booking.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;

@Document(collection = "seat_layouts")
public class SeatLayoutDocument {

    @Id
    private String id;

    private Long busId;

    private int rows;

    private int seatsPerRow;

    private Map<String, String> seatMap;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getBusId() { return busId; }
    public void setBusId(Long busId) { this.busId = busId; }
    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }
    public int getSeatsPerRow() { return seatsPerRow; }
    public void setSeatsPerRow(int seatsPerRow) { this.seatsPerRow = seatsPerRow; }
    public Map<String, String> getSeatMap() { return seatMap; }
    public void setSeatMap(Map<String, String> seatMap) { this.seatMap = seatMap; }
}

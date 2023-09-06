package uk.gov.dwp.uc.pairtest.domain;

public class TicketInformation {
    int totalTicketPrice;
    int totalSeatBooked;

    public int getTotalTicketPrice() {
        return totalTicketPrice;
    }

    public void setTotalTicketPrice(int totalTicketPrice) {
        this.totalTicketPrice = totalTicketPrice;
    }

    public int getTotalSeatBooked() {
        return totalSeatBooked;
    }

    public void setTotalSeatBooked(int totalSeatBooked) {
        this.totalSeatBooked = totalSeatBooked;
    }
}

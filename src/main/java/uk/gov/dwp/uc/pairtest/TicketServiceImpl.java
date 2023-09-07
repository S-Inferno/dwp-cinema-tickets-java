package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketInformation;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class TicketServiceImpl implements TicketService {

    /**
     * Global variable for maximum ticket limit. Can be variable in Db or Config file.
     */
    private final int MAX_TICKET_ALLOWED_COUNT = 20;

    /**
     * Global variable for all the ticket price. Can be variable in Db or Config file.
     */
    private final Map<TicketTypeRequest.Type, Integer> TICKET_PRICE = Map.ofEntries(
            entry(TicketTypeRequest.Type.INFANT, 0),
            entry(TicketTypeRequest.Type.CHILD, 10),
            entry(TicketTypeRequest.Type.ADULT, 20)
    );

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        //Converting java varargs into ArrayList.
        List<TicketTypeRequest> ticketTypeRequestList = Arrays.asList(ticketTypeRequests);

        // Check for invalid account if account id is below 0.
        if (accountId < 1) {
            throw new InvalidPurchaseException("Invalid Account.");
        }

        //Check if at least one entry should be present for any ticket type.
        ticketTypeRequestList.stream().findAny()
                .orElseThrow(() -> new InvalidPurchaseException("Please add at least 1 Adult ticket."));

        checkMaxTicketLimit(ticketTypeRequestList);
        checkIfAdult(ticketTypeRequestList);
        TicketInformation ticketInformation = calculatePriceAndSeat(ticketTypeRequestList);
        handlePaymentAndBooking(accountId, ticketInformation);

    }

    /**
     * This method count the total ticket user trying to book and if the count exceeds
     * the maximum limit then throws the exception.
     *
     * @param ticketTypeRequestList input list of all the ticket with count and type.
     */
    private void checkMaxTicketLimit(final List<TicketTypeRequest> ticketTypeRequestList) {
        int ticketCount = ticketTypeRequestList.stream().mapToInt(TicketTypeRequest::getNoOfTickets).sum();
        if (ticketCount > MAX_TICKET_ALLOWED_COUNT) {
            throw new InvalidPurchaseException("You trying to purchase " + ticketCount +
                    " tickets. Max ticket allowed to be purchased is " + MAX_TICKET_ALLOWED_COUNT + ".");
        }
    }

    /**
     * This method count checks if Adult ticket type is present in the request. If not
     * methods throws the exception.
     *
     * @param ticketTypeRequestList input list of all the ticket with count and type.
     */
    private void checkIfAdult(final List<TicketTypeRequest> ticketTypeRequestList) {
        ticketTypeRequestList.stream()
                .filter(ticketTypeRequest -> ticketTypeRequest.getTicketType().equals(TicketTypeRequest.Type.ADULT))
                .findAny()
                .orElseThrow(() -> new InvalidPurchaseException("Child and Infant tickets cannot be purchased without purchasing an Adult ticket."));
    }

    /**
     * This method calculate the total ticket price and total number of ticket booked
     * based on the business logic.
     * Created a new TicketInformation class with variable totalTicketPrice
     * and totalSeatBooked to store the information for this method.
     *
     * @param ticketTypeRequestList input list of all the ticket with count and type.
     * @return TicketInformation which has total ticket price and total seat booked.
     * @see TicketInformation
     */
    private TicketInformation calculatePriceAndSeat(final List<TicketTypeRequest> ticketTypeRequestList) {
        int totalTicketPrice = 0;
        int totalSeatBooked = 0;
        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequestList) {
            switch (ticketTypeRequest.getTicketType()) {
                case ADULT -> {
                    totalTicketPrice = totalTicketPrice
                            + (ticketTypeRequest.getNoOfTickets() * TICKET_PRICE.get(TicketTypeRequest.Type.ADULT));
                    totalSeatBooked += ticketTypeRequest.getNoOfTickets();
                }
                case CHILD -> {
                    totalTicketPrice = totalTicketPrice
                            + (ticketTypeRequest.getNoOfTickets() * TICKET_PRICE.get(TicketTypeRequest.Type.CHILD));
                    totalSeatBooked += ticketTypeRequest.getNoOfTickets();
                }
                case INFANT -> {
                    totalTicketPrice += 0;
                    totalSeatBooked += 0;
                }
                default -> throw new InvalidPurchaseException("Sorry, there is some problem with the server.");
            }
        }

        TicketInformation ticketInformation = new TicketInformation();
        ticketInformation.setTotalTicketPrice(totalTicketPrice);
        ticketInformation.setTotalSeatBooked(totalSeatBooked);
        return ticketInformation;
    }

    /**
     * This method calls third party payment and seat reservation service and completes the booking.
     *
     * @param accountId         - User account information
     * @param ticketInformation which has total ticket price and total seat booked.
     */
    private void handlePaymentAndBooking(final Long accountId, final TicketInformation ticketInformation) {
        //Just to print the for logs what is total ticket price and total seat reserved.
        System.out.println("Total ticket price: " + ticketInformation.getTotalTicketPrice());
        System.out.println("Total seat book: " + ticketInformation.getTotalSeatBooked());

        //Just to handle exception if any.
        try {
            TicketPaymentService ticketPaymentService = new TicketPaymentServiceImpl();
            ticketPaymentService.makePayment(accountId, ticketInformation.getTotalTicketPrice());

            SeatReservationService seatReservationService = new SeatReservationServiceImpl();
            seatReservationService.reserveSeat(accountId, ticketInformation.getTotalSeatBooked());
        } catch (Exception e) {
            throw new InvalidPurchaseException("Sorry, there is some problem with the server.");
        }

    }
}

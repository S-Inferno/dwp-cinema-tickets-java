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
     * Should only have private methods other than the one below.
     */
    private final int MAX_TICKET_ALLOWED_COUNT = 20;
    private final Map<TicketTypeRequest.Type, Integer> TICKET_PRICE = Map.ofEntries(
            entry(TicketTypeRequest.Type.INFANT, 0),
            entry(TicketTypeRequest.Type.CHILD, 10),
            entry(TicketTypeRequest.Type.ADULT, 20)
    );

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        List<TicketTypeRequest> ticketTypeRequestList = Arrays.asList(ticketTypeRequests);

        if (accountId < 1) {
            throw new InvalidPurchaseException("Invalid Account.");
        }

        ticketTypeRequestList.stream().findAny()
                .orElseThrow(() -> new InvalidPurchaseException("Please add at least 1 Adult ticket."));

        checkMaxTicketLimit(ticketTypeRequestList);
        checkIfAdult(ticketTypeRequestList);
        TicketInformation ticketInformation = calculatePriceAndSeat(ticketTypeRequestList);
        handlePayment(accountId, ticketInformation);

    }

    private void checkMaxTicketLimit(final List<TicketTypeRequest> ticketTypeRequestList) {
        int ticketCount = ticketTypeRequestList.stream().mapToInt(TicketTypeRequest::getNoOfTickets).sum();
        if (ticketCount > MAX_TICKET_ALLOWED_COUNT) {
            throw new InvalidPurchaseException("You trying to purchase " + ticketCount +
                    " tickets. Max ticket allowed to be purchased is " + MAX_TICKET_ALLOWED_COUNT + ".");
        }
    }

    private void checkIfAdult(final List<TicketTypeRequest> ticketTypeRequestList) {
        ticketTypeRequestList.stream()
                .filter(ticketTypeRequest -> ticketTypeRequest.getTicketType().equals(TicketTypeRequest.Type.ADULT))
                .findAny()
                .orElseThrow(() -> new InvalidPurchaseException("Child and Infant tickets cannot be purchased without purchasing an Adult ticket."));
    }

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
                default -> throw new InvalidPurchaseException("Sorry there is some problem with the server.");
            }
        }

        TicketInformation ticketInformation = new TicketInformation();
        ticketInformation.setTotalTicketPrice(totalTicketPrice);
        ticketInformation.setTotalSeatBooked(totalSeatBooked);
        return ticketInformation;
    }

    private void handlePayment(final Long accountId, final TicketInformation ticketInformation) {
        System.out.println("Total ticket price: " + ticketInformation.getTotalTicketPrice());
        System.out.println("Total seat book: " + ticketInformation.getTotalSeatBooked());
        TicketPaymentService ticketPaymentService = new TicketPaymentServiceImpl();
        ticketPaymentService.makePayment(accountId, ticketInformation.getTotalTicketPrice());

        SeatReservationService seatReservationService = new SeatReservationServiceImpl();
        seatReservationService.reserveSeat(accountId, ticketInformation.getTotalSeatBooked());
    }
}

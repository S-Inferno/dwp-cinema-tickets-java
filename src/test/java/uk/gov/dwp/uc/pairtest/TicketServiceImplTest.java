package uk.gov.dwp.uc.pairtest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImplTest {
    private TicketServiceImpl ticketService;

    @Before
    public void setup() {
        ticketService = new TicketServiceImpl();
    }

    @Test()
    public void test_purchaseTickets_HappyPath() {
        TicketTypeRequest ticketTypeRequestAdult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        TicketTypeRequest ticketTypeRequestChild = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5);
        TicketTypeRequest ticketTypeRequestInfant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);

        ticketService.purchaseTickets(1L, ticketTypeRequestAdult, ticketTypeRequestChild, ticketTypeRequestInfant);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void test_purchaseTicket_onlyInfant() {
        TicketTypeRequest ticketTypeRequestInfant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);
        ticketService.purchaseTickets(1L, ticketTypeRequestInfant);
    }

    @Test
    public void test_purchaseTicket_onlyChild() {
        TicketTypeRequest ticketTypeRequestChild = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5);
        InvalidPurchaseException invalidPurchaseException = Assert.assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, ticketTypeRequestChild)
        );
        Assert.assertEquals(invalidPurchaseException.getMessage(),
                "Child and Infant tickets cannot be purchased without purchasing an Adult ticket.");
    }

    @Test
    public void test_purchaseTicket_ChildAndInfant() {
        TicketTypeRequest ticketTypeRequestChild = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5);
        TicketTypeRequest ticketTypeRequestInfant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);

        InvalidPurchaseException invalidPurchaseException = Assert.assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, ticketTypeRequestChild, ticketTypeRequestInfant)
        );
        Assert.assertEquals(invalidPurchaseException.getMessage(),
                "Child and Infant tickets cannot be purchased without purchasing an Adult ticket.");
    }

    @Test
    public void test_purchaseTicket_ChildAndInfant_MaxTicket() {
        TicketTypeRequest ticketTypeRequestChild = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5);
        TicketTypeRequest ticketTypeRequestInfant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 20);

        InvalidPurchaseException invalidPurchaseException = Assert.assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, ticketTypeRequestChild, ticketTypeRequestInfant)
        );
        Assert.assertTrue(invalidPurchaseException.getMessage().contains("Max ticket allowed to be purchased is 20."));
    }

    @Test
    public void test_purchaseTicket_onlyAdult() {
        TicketTypeRequest ticketTypeRequestAdult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        ticketService.purchaseTickets(1L, ticketTypeRequestAdult);
    }

    @Test
    public void test_purchaseTicket_onlyAdult_MaxTicket() {
        TicketTypeRequest ticketTypeRequestAdult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 50);
        InvalidPurchaseException invalidPurchaseException = Assert.assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, ticketTypeRequestAdult)
        );
        Assert.assertTrue(invalidPurchaseException.getMessage().contains("Max ticket allowed to be purchased is 20."));
    }

    @Test
    public void test_purchaseTicket_MaxTicket() {
        TicketTypeRequest ticketTypeRequestAdult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        TicketTypeRequest ticketTypeRequestChild = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 50);
        TicketTypeRequest ticketTypeRequestInfant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);
        InvalidPurchaseException invalidPurchaseException = Assert.assertThrows(InvalidPurchaseException.class,
                () ->         ticketService.purchaseTickets(1L, ticketTypeRequestAdult, ticketTypeRequestChild, ticketTypeRequestInfant)
        );
        Assert.assertTrue(invalidPurchaseException.getMessage().contains("Max ticket allowed to be purchased is 20."));
    }

    @Test
    public void test_purchaseTicket_SameType() {
        TicketTypeRequest ticketTypeRequestA = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest ticketTypeRequestB = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest ticketTypeRequestC = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        ticketService.purchaseTickets(1L, ticketTypeRequestA, ticketTypeRequestB, ticketTypeRequestC);
    }
    @Test
    public void test_purchaseTicket_SameType_MaxTicket() {
        TicketTypeRequest ticketTypeRequestA = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20);
        TicketTypeRequest ticketTypeRequestB = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest ticketTypeRequestC = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        InvalidPurchaseException invalidPurchaseException = Assert.assertThrows(InvalidPurchaseException.class,
                () ->         ticketService.purchaseTickets(1L, ticketTypeRequestA, ticketTypeRequestB, ticketTypeRequestC)
        );
        Assert.assertTrue(invalidPurchaseException.getMessage().contains("Max ticket allowed to be purchased is 20."));
    }
}

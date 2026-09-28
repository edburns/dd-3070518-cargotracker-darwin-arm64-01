package org.eclipse.cargotracker.interfaces.booking.facade.internal;

import org.eclipse.cargotracker.application.BookingService;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class DefaultBookingServiceFacadeTest {

    @Test
    public void changeDeadlineDelegatesTrackingIdAndDate() throws Exception {
        RecordingBookingService bookingService = new RecordingBookingService();
        DefaultBookingServiceFacade facade = new DefaultBookingServiceFacade();
        setBookingService(facade, bookingService);
        Date arrivalDeadline = new Date();

        facade.changeDeadline("ABC123", arrivalDeadline);

        assertEquals(1, bookingService.changeDeadlineCalls);
        assertEquals(new TrackingId("ABC123"), bookingService.trackingId);
        assertSame(arrivalDeadline, bookingService.arrivalDeadline);
    }

    private void setBookingService(DefaultBookingServiceFacade facade,
                                   BookingService bookingService) throws Exception {
        Field field = DefaultBookingServiceFacade.class
                .getDeclaredField("bookingService");
        field.setAccessible(true);
        field.set(facade, bookingService);
    }

    private static class RecordingBookingService implements BookingService {

        private int changeDeadlineCalls;
        private TrackingId trackingId;
        private Date arrivalDeadline;

        @Override
        public TrackingId bookNewCargo(UnLocode origin, UnLocode destination,
                                       Date arrivalDeadline) {
            return null;
        }

        @Override
        public List<Itinerary> requestPossibleRoutesForCargo(
                TrackingId trackingId) {
            return null;
        }

        @Override
        public void assignCargoToRoute(Itinerary itinerary,
                                       TrackingId trackingId) {
        }

        @Override
        public void changeDestination(TrackingId trackingId,
                                      UnLocode unLocode) {
        }

        @Override
        public void changeDeadline(TrackingId trackingId, Date deadline) {
            changeDeadlineCalls++;
            this.trackingId = trackingId;
            arrivalDeadline = deadline;
        }
    }
}

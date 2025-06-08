//package vn.edu.iuh.fit.tourmanagement.schedulers;
//
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.Query;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import vn.edu.iuh.fit.tourmanagement.enums.TourStatus;
//import vn.edu.iuh.fit.tourmanagement.models.Tour;
//import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;
//import vn.edu.iuh.fit.tourmanagement.listeners.TourCanceledEvent;
//
//import java.util.List;
//
//@Component
//public class TourStatusChangeScheduler {
//
//    @Autowired
//    private EntityManager entityManager;
//
//    @Autowired
//    private ApplicationEventPublisher eventPublisher;
//
//    @Autowired
//    private TourRepository tourRepository;
//
//    @Scheduled(fixedRate = 60000) // Chạy mỗi 60 giây
//    public void checkTourStatusChanges() {
//        Query query = entityManager.createNativeQuery("SELECT tour_id FROM tour_status_changes WHERE processed = false");
//        List<Long> tourIds = query.getResultList();
//
//        for (Long tourId : tourIds) {
//            Tour tour = tourRepository.findById(tourId).orElse(null);
//            if (tour != null && tour.getStatus() == TourStatus.CANCELED) {
//                eventPublisher.publishEvent(new TourCanceledEvent(tour));
//            }
//            // Đánh dấu là đã xử lý
//            entityManager.createNativeQuery("UPDATE tour_status_changes SET processed = true WHERE tour_id = :tourId")
//                    .setParameter("tourId", tourId)
//                    .executeUpdate();
//        }
//    }
//}
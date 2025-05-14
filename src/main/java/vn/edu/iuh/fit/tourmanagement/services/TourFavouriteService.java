package vn.edu.iuh.fit.tourmanagement.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.tourmanagement.dto.TourFavouriteRequestDTO;
import vn.edu.iuh.fit.tourmanagement.exceptions.CustomerNotFoundException;
import vn.edu.iuh.fit.tourmanagement.exceptions.TourAlreadyFavoritedException;
import vn.edu.iuh.fit.tourmanagement.exceptions.TourNotFavoritedException;
import vn.edu.iuh.fit.tourmanagement.exceptions.TourNotFoundException;
import vn.edu.iuh.fit.tourmanagement.models.Customer;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourFavourite;
import vn.edu.iuh.fit.tourmanagement.repositories.CustomerRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.TourFabouriteRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TourFavouriteService {
    private static final Logger logger = LoggerFactory.getLogger(TourFavouriteService.class);
    private final TourFabouriteRepository tourFavouriteRepository;
    private final CustomerRepository customerRepository;
    private final TourRepository tourRepository;

    public TourFavouriteService(
            TourFabouriteRepository tourFavouriteRepository,
            CustomerRepository customerRepository,
            TourRepository tourRepository
    ) {
        this.tourFavouriteRepository = tourFavouriteRepository;
        this.customerRepository = customerRepository;
        this.tourRepository = tourRepository;
    }

    @Transactional
    public String addTourFavourite(TourFavouriteRequestDTO request) {
        logger.info("Adding tour favourite: customerId={}, tourId={}", request.getCustomerId(), request.getTourId());
        Long customerId = request.getCustomerId();
        Long tourId = request.getTourId();

        if (customerId == null || tourId == null) {
            logger.error("Invalid request: customerId or tourId is null");
            throw new IllegalArgumentException("customerId và tourId không được để trống");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    logger.error("Customer not found: customerId={}", customerId);
                    return new CustomerNotFoundException("Không tìm thấy khách hàng với ID: " + customerId);
                });

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> {
                    logger.error("Tour not found: tourId={}", tourId);
                    return new TourNotFoundException("Không tìm thấy tour với ID: " + tourId);
                });

        if (tourFavouriteRepository.existsByCustomer_CustomerIdAndTour_TourId(customerId, tourId)) {
            logger.warn("Tour already in favorites: customerId={}, tourId={}, email={}", customerId, tourId, customer.getUser().getEmail());
            throw new TourAlreadyFavoritedException("Tour này đã có trong danh sách yêu thích!");
        }

        TourFavourite tourFavourite = TourFavourite.builder()
                .customer(customer)
                .tour(tour)
                .build();

        tourFavouriteRepository.save(tourFavourite);
        logger.info("Successfully added tour favourite: customerId={}, tourId={}, email={}", customerId, tourId, customer.getUser().getEmail());
        return "Thêm tour vào danh sách yêu thích thành công!";
    }

    public List<Tour> getFavouritesByCustomer(Long customerId) {
        logger.info("Fetching favorites for customerId={}", customerId);
        customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    logger.error("Customer not found: customerId={}", customerId);
                    return new CustomerNotFoundException("Không tìm thấy khách hàng với ID: " + customerId);
                });
        List<TourFavourite> favourites = tourFavouriteRepository.findByCustomer_CustomerId(customerId);
        return favourites.stream().map(TourFavourite::getTour).collect(Collectors.toList());
    }

    @Transactional
    public String removeTourFavourite(TourFavouriteRequestDTO request) {
        logger.info("Removing tour favourite: customerId={}, tourId={}", request.getCustomerId(), request.getTourId());
        Long customerId = request.getCustomerId();
        Long tourId = request.getTourId();

        if (customerId == null || tourId == null) {
            logger.error("Invalid request: customerId or tourId is null");
            throw new IllegalArgumentException("customerId và tourId không được để trống");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    logger.error("Customer not found: customerId={}", customerId);
                    return new CustomerNotFoundException("Không tìm thấy khách hàng với ID: " + customerId);
                });
        tourRepository.findById(tourId)
                .orElseThrow(() -> {
                    logger.error("Tour not found: tourId={}", tourId);
                    return new TourNotFoundException("Không tìm thấy tour với ID: " + tourId);
                });

        if (!tourFavouriteRepository.existsByCustomer_CustomerIdAndTour_TourId(customerId, tourId)) {
            logger.warn("Tour not in favorites: customerId={}, tourId={}, email={}", customerId, tourId, customer.getUser().getEmail());
            throw new TourNotFavoritedException("Tour này không có trong danh sách yêu thích!");
        }

        tourFavouriteRepository.deleteByCustomer_CustomerIdAndTour_TourId(customerId, tourId);
        logger.info("Successfully removed tour favourite: customerId={}, tourId={}, email={}", customerId, tourId, customer.getUser().getEmail());
        return "Xóa tour khỏi danh sách yêu thích thành công!";
    }
}
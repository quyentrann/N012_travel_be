package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.models.TourCategory;
import vn.edu.iuh.fit.tourmanagement.repositories.TourCategoryRepository;

import java.util.List;

@Service
public class TourCategoryService {
    @Autowired
    private TourCategoryRepository tourCategoryRepository;

    public List<TourCategory> getListTourCategory() {
        return tourCategoryRepository.findAll();
    }
}

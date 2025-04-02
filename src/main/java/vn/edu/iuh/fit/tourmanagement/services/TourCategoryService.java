package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.models.TourCategory;
import vn.edu.iuh.fit.tourmanagement.repositories.TourCategoryRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;

import java.util.List;

@Service
public class TourCategoryService {
    @Autowired
    private TourCategoryRepository tourCategoryRepository;
    @Autowired
    private TourRepository tourRepository;

    public List<TourCategory> getListTourCategory() {
        return tourCategoryRepository.findAll();
    }

    public TourCategory getTourCategoryById(Long id) {
        return tourCategoryRepository.findById(id).orElse(null);
    }

    public TourCategory createTourCategory(TourCategory tourCategory) {
        if (tourCategoryRepository.existsByCategoryName(tourCategory.getCategoryName())) {
            throw new IllegalArgumentException("Danh mục đã tồn tại!");
        }
        return tourCategoryRepository.save(tourCategory);
    }

public void deleteTourCategory(Long categoryId) {
    // Kiểm tra danh mục có tồn tại không
    TourCategory category = tourCategoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));

    // Kiểm tra xem có tour nào đang dùng danh mục này không
    if (tourRepository.existsByTourcategory(category)) {
        throw new RuntimeException("Danh mục này đang được sử dụng bởi tour, không thể xoá!");
    }

    // Xoá nếu không bị sử dụng
    tourCategoryRepository.delete(category);
}


    public TourCategory updateTourCategory(TourCategory tourCategory) {
        return tourCategoryRepository.save(tourCategory);
    }
}

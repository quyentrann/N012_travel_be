package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.models.TourCategory;
import vn.edu.iuh.fit.tourmanagement.services.TourCategoryService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class TourCategoryController {
    @Autowired
    private TourCategoryService tourCategoryService;

    @GetMapping
    public ResponseEntity<List<TourCategory>> getAllCategories() {
        List<TourCategory> categories = tourCategoryService.getListTourCategory();
        if (categories.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<TourCategory> createCategory(@RequestBody TourCategory category) {
        try {
            TourCategory createdCategory = tourCategoryService.createTourCategory(category);
            return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourCategory> updateCategory(@PathVariable("id") Long id, @RequestBody TourCategory category) {
        TourCategory existingCategory = tourCategoryService.getTourCategoryById(id);
        if (existingCategory == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        category.setCategoryId(id);
        TourCategory updatedCategory = tourCategoryService.updateTourCategory(category);
        return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
//    public ResponseEntity<HttpStatus> deleteCategory(@PathVariable("id") Long id) {
//        if (!tourCategoryService.deleteTourCategory(id)) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            tourCategoryService.deleteTourCategory(id);
            return ResponseEntity.ok(Map.of("message", "Xoá danh mục thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

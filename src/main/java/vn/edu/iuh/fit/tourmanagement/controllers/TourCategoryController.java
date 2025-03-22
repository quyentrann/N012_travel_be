package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.tourmanagement.models.TourCategory;
import vn.edu.iuh.fit.tourmanagement.services.TourCategoryService;

import java.util.List;

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
}

package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.models.Discount;
import vn.edu.iuh.fit.tourmanagement.services.DiscountService;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {
    @Autowired
    private DiscountService discountService;

    @GetMapping
    public ResponseEntity<List<Discount>> getAllDiscounts() {
        List<Discount> discounts = discountService.getListDiscount();
        if (discounts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(discounts, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Discount> createDiscount(@RequestBody Discount discount) {
        try {
            Discount createdDiscount = discountService.createDiscount(discount);
            return new ResponseEntity<>(createdDiscount, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteDiscount(@PathVariable("id") Long id) {
        if (discountService.deleteDiscount(id)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Discount> updateDiscount(@PathVariable("id") Long id, @RequestBody Discount discount) {
        Discount existingDiscount = discountService.getDiscountById(id);
        if (existingDiscount == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        discount.setDiscountId(id);
        Discount updatedDiscount = discountService.updateDiscount(discount);
        return new ResponseEntity<>(updatedDiscount, HttpStatus.OK);
    }
}

package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.models.Discount;
import vn.edu.iuh.fit.tourmanagement.repositories.DiscountRepository;

import java.util.List;

@Service
public class DiscountService {
    @Autowired
    private DiscountRepository discountRepository;

    public List<Discount> getListDiscount() {
        return discountRepository.findAll();
    }

}

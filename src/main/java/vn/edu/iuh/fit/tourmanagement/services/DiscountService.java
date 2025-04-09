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

    public Discount getDiscountById(Long id) {
        return discountRepository.findById(id).orElse(null);
    }

    public Discount createDiscount(Discount discount) {
        return discountRepository.save(discount);
    }

    public boolean deleteDiscount(Long id) {
        Discount discount = discountRepository.findById(id).orElse(null);
        if (discount == null) {
            return false;
        }
        discountRepository.delete(discount);
        return true;
    }


    public Discount updateDiscount(Discount discount) {
        return discountRepository.save(discount);
    }

}

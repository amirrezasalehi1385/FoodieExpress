package org.FoodOrder.server.controllers;

import org.FoodOrder.server.exceptions.NotFoundException;
import org.FoodOrder.server.models.*;
import org.FoodOrder.server.DAO.*;

public class VendorController {
    public static Vendor getVendorById(Long id) throws NotFoundException {
        VendorDao vendorDao = new VendorDao();
        Vendor vendor = vendorDao.findById(id);
        if(vendor == null) {
            throw new NotFoundException("vendor not found", 404);
        }
        return  vendor;
    }
}

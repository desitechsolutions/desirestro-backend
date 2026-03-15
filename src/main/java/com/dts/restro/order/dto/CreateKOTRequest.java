package com.dts.restro.order.dto;

import com.dts.restro.order.entity.KOTItem;
import java.util.List;

public class CreateKOTRequest {
    private List<KOTItem> items;
    public List<KOTItem> getItems() { return items; }
    public void setItems(List<KOTItem> items) { this.items = items; }
}

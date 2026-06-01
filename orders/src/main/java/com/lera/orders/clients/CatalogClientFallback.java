package com.lera.orders.clients;

import com.lera.orders.dto.catalog.GetGoodsListRequest;
import com.lera.orders.dto.catalog.GetGoodsListResponse;

public class CatalogClientFallback implements CatalogClient{

    @Override
    public GetGoodsListResponse getGoodsList(GetGoodsListRequest request) {
        return null;
    }
}

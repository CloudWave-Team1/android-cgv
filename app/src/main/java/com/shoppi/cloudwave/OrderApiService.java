package com.shoppi.cloudwave;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OrderApiService {
    @POST("api/orders")
    Call<Order> placeOrder(@Body Order order);
}



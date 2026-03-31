package com.ga.petadoption.model.request;

import com.ga.showroom.model.Car;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {
    private Car car;
    private Long modelId;
    private Long ownerId;
    private List<Long> options;
}

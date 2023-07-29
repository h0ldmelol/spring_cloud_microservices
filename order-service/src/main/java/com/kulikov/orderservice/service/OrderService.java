package com.kulikov.orderservice.service;

import com.kulikov.orderservice.dto.InventoryResponse;
import com.kulikov.orderservice.dto.OrderLineItemsDto;
import com.kulikov.orderservice.dto.OrderRequest;
import com.kulikov.orderservice.model.Order;
import com.kulikov.orderservice.model.OrderLineItems;
import com.kulikov.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webclientBuilder;

    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);
        List<String> skuCodes = order.getOrderLineItemsList()
                .stream()
                .map(OrderLineItems::getSkuCode)
                .toList();


        InventoryResponse[] inventoryResponses =  webclientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        List<InventoryResponse> notInStockProducts = Arrays.stream(inventoryResponses)
                .filter(inventoryResponse -> !inventoryResponse.isInStock())
                .toList();

        if (notInStockProducts.isEmpty()) {
            orderRepository.save(order);
            return "Order placed";
        } else {
            throw new IllegalArgumentException("Products is not in stock: " + notInStockProducts);
        }

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}

package com.library.app.mapper;

import com.library.app.dto.OrderDto;
import com.library.app.model.Order;

/**
 * Utility class for mapping {@link Order} entities to {@link OrderDto} objects.
 */
public class OrderMapper {
    /**
     * Converts an {@link Order} entity to an {@link OrderDto}.
     *
     * @param order order The order entity to be converted.
     * @return The corresponding order DTO.
     */
    public static OrderDto toDto(Order order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setUsername(order.getUser().getUsername());
        orderDto.setBookTitle(order.getBookCopy().getBook().getTitle());
        orderDto.setInventoryNumber(order.getBookCopy().getInventoryNumber());
        orderDto.setType(order.getType());
        orderDto.setStatus(order.getStatus());
        orderDto.setIssueDate(order.getIssueDate());
        orderDto.setDueDate(order.getDueDate());
        orderDto.setReturnDate(order.getReturnDate());
        orderDto.setAuthorFullName(
                order.getBookCopy().getBook().getAuthorFirstName() + " " +
                order.getBookCopy().getBook().getAuthorLastName()
        );

        return orderDto;
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private OrderMapper() {
    }
}
